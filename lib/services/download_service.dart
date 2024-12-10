import 'dart:io';
import 'dart:isolate';
import 'dart:ui';

import 'package:collection/collection.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter_downloader/flutter_downloader.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:mime/mime.dart';
import 'package:path/path.dart' as path;
import 'package:path_provider/path_provider.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../database/database.dart';
import '../http/client.dart';
import '../models/music.dart';
import '../models/query.dart';
import '../models/support.dart';
import '../state/music.dart';
import '../state/settings.dart';
import 'cache_service.dart';

part 'download_service.freezed.dart';
part 'download_service.g.dart';

@freezed
class DownloadState with _$DownloadState {
  const factory DownloadState({
    @Default(IListConst([])) IList<Download> downloads,
    @Default(IListConst([])) IList<SourceId> deletes,
    @Default(IListConst([])) IList<SourceId> listDownloads,
    @Default(IListConst([])) IList<SourceId> listCancels,
    required String saveDir,
  }) = _DownloadState;
}

@freezed
class Download with _$Download {
  const Download._();

  const factory Download({
    required String taskId,
    required DownloadTaskStatus status,
    required int progress,
    required String url,
    required String? filename,
    required String savedDir,
    required int timeCreated,
    required bool allowCellular,
  }) = _Download;

  factory Download.fromTask(DownloadTask task) {
    return Download(
      taskId: task.taskId,
      status: task.status,
      progress: task.progress,
      url: task.url,
      filename: task.filename,
      savedDir: task.savedDir,
      timeCreated: task.timeCreated,
      allowCellular: task.allowCellular,
    );
  }

  DownloadTask toTask() {
    return DownloadTask(
      taskId: taskId,
      status: status,
      progress: progress,
      url: url,
      filename: filename,
      savedDir: savedDir,
      timeCreated: timeCreated,
      allowCellular: allowCellular,
    );
  }
}

enum SongDownloadState {
  none,
  inProgress,
  completed,
}

@Riverpod(keepAlive: true)
class DownloadService extends _$DownloadService {
  static final ReceivePort _port = ReceivePort();

  @override
  DownloadState build() {
    return const DownloadState(saveDir: '');
  }

  Future<void> init() async {
    await FlutterDownloader.initialize(
      // debug: true,
      ignoreSsl: true,
    );

    state = state.copyWith(
      saveDir: path.join(
        (await getApplicationDocumentsDirectory()).path,
        'downloads',
      ),
    );

    _bindBackgroundIsolate();
    await _syncDownloadTasks();

    FlutterDownloader.registerCallback(downloadCallback, step: 1);
  }

  Future<void> downloadAlbum(Album album) async {
    return _downloadList(album, () async {
      final cache = ref.read(cacheServiceProvider);
      await Future.wait([
        _cacheArtistArt(album.sourceId, album.artistId, false),
        _cacheArtistArt(album.sourceId, album.artistId, true),
        _cacheImage(cache.albumArt(album, thumbnail: false)),
        _cacheImage(cache.albumArt(album, thumbnail: true)),
      ]);
      if (_isCanceled(album)) return;

      final songs = await _albumSongs(album, SongDownloadState.none);
      if (_isCanceled(album)) return;

      for (var song in songs) {
        await _downloadSong(song);
        if (_isCanceled(album)) return;
      }
    });
  }

  Future<void> downloadPlaylist(Playlist playlist) async {
    return _downloadList(playlist, () async {
      final songs = await _playlistSongs(playlist, SongDownloadState.none);
      if (_isCanceled(playlist)) return;

      final albumIds = songs.map((e) => e.albumId).whereNotNull().toSet();
      final albums =
          await ref.read(albumsInIdsProvider(albumIds.toIList()).future);
      final artistIds = albums.map((e) => e.artistId).whereNotNull().toSet();

      final cache = ref.read(cacheServiceProvider);
      await Future.wait([
        _cacheImage(cache.playlistArt(playlist, thumbnail: true)),
        _cacheImage(cache.playlistArt(playlist, thumbnail: false)),
        ...albums.map((a) => _cacheImage(cache.albumArt(a, thumbnail: true))),
        ...albums.map((a) => _cacheImage(cache.albumArt(a, thumbnail: false))),
        ...artistIds.map(
          (artistId) => _cacheArtistArt(playlist.sourceId, artistId, true),
        ),
        ...artistIds.map(
          (artistId) => _cacheArtistArt(playlist.sourceId, artistId, false),
        ),
      ]);
      if (_isCanceled(playlist)) return;

      for (var song in songs) {
        await _downloadSong(song);
        if (_isCanceled(playlist)) return;
      }
    });
  }

  Future<void> cancelAlbum(Album album) async {
    return _cancelList(album, () async {
      final songs = await _albumSongs(album, SongDownloadState.inProgress);
      for (var song in songs) {
        try {
          await FlutterDownloader.cancel(taskId: song.downloadTaskId!);
        } catch (e) {
          //
        }
      }
    });
  }

  Future<void> cancelPlaylist(Playlist playlist) async {
    return _cancelList(playlist, () async {
      final songs =
          await _playlistSongs(playlist, SongDownloadState.inProgress);
      for (var song in songs) {
        await FlutterDownloader.cancel(taskId: song.downloadTaskId!);
      }
    });
  }

  Future<void> deleteAlbum(Album album) async {
    return _deleteList(album, () async {
      final db = ref.read(databaseProvider);
      final songs = await _albumSongs(album, SongDownloadState.completed);

      for (var song in songs) {
        await _tryDeleteFile(song.downloadFilePath!);
        await db.deleteSongDownloadFile(song.sourceId, song.id);
      }
    });
  }

  Future<void> deletePlaylist(Playlist playlist) async {
    return _deleteList(playlist, () async {
      final db = ref.read(databaseProvider);
      final songs = await _playlistSongs(playlist, SongDownloadState.completed);

      for (var song in songs) {
        if (await _tryDeleteFile(song.downloadFilePath!)) {
          await db.deleteSongDownloadFile(song.sourceId, song.id);
        }
      }
    });
  }

  Future<void> deleteAll(int sourceId) async {
    final db = ref.read(databaseProvider);

    final albumIds = await db.albumIdsWithDownloadStatus(sourceId).get();
    for (var id in albumIds) {
      await deleteAlbum(await (db.albumById(sourceId, id)).getSingle());
    }
  }

  Future<void> _downloadList(
    SourceIdentifiable list,
    Future<void> Function() callback,
  ) async {
    final sourceId = SourceId.from(list);
    if (state.listDownloads.contains(sourceId)) {
      return;
    }
    state = state.copyWith(listDownloads: state.listDownloads.add(sourceId));

    try {
      await callback();
    } finally {
      state = state.copyWith(
        listDownloads: state.listDownloads.remove(sourceId),
      );
    }
  }

  Future<void> _cancelList(
    SourceIdentifiable list,
    Future<void> Function() callback,
  ) async {
    final sourceId = SourceId.from(list);
    if (state.listCancels.contains(sourceId)) return;
    state = state.copyWith(
      listCancels: state.listCancels.add(sourceId),
    );

    if (state.listDownloads.contains(sourceId)) {
      var tries = 0;
      while (tries < 60) {
        await Future.delayed(const Duration(seconds: 1));
        if (!state.listDownloads.contains(sourceId)) {
          break;
        }
      }
    }

    try {
      await callback();
    } finally {
      state = state.copyWith(
        listCancels: state.listCancels.remove(sourceId),
      );
    }
  }

  Future<void> _deleteList(
    SourceIdentifiable list,
    Future<void> Function() callback,
  ) async {
    final sourceId = SourceId.from(list);
    if (state.deletes.contains(sourceId)) {
      return;
    }
    state = state.copyWith(deletes: state.deletes.add(sourceId));

    try {
      await callback();
    } finally {
      state = state.copyWith(deletes: state.deletes.remove(sourceId));
    }
  }

  Future<void> _downloadSong(Song song) async {
    if (song.downloadFilePath != null || song.downloadTaskId != null) {
      return;
    }

    final source = ref.read(musicSourceProvider);
    final db = ref.read(databaseProvider);
    final http = ref.read(httpClientProvider);

    final uri = source.downloadUri(song.id);
    final head = await http.head(uri);

    final contentType = head.headers['content-type'];
    if (contentType == null) {
      throw StateError('Bad HTTP response from HEAD during download');
    }

    final mime = contentType.split(';').first.toLowerCase();
    if (!mime.startsWith('audio') && !mime.startsWith('application')) {
      throw StateError('Download error: MIME-type $mime is not audio');
    }

    String? ext = extensionFromMime(mime);
    if (ext == mime) {
      ext = null;
    }

    final saveDir = Directory(
      path.join(state.saveDir, song.sourceId.toString()),
    );
    await saveDir.create(recursive: true);

    final taskId = await FlutterDownloader.enqueue(
      url: source.downloadUri(song.id).toString(),
      savedDir: saveDir.path,
      fileName: ext != null ? '${song.id}.$ext' : song.id,
      headers: subtracksHeaders,
      openFileFromNotification: false,
      showNotification: false,
    );
    await db.updateSongDownloadTask(taskId, song.sourceId, song.id);
  }

  Future<void> _cacheImage(UriCacheInfo cache) async {
    final cachedFile = await cache.cacheManager.getFileFromCache(
      cache.cacheKey,
      ignoreMemCache: true,
    );
    if (cachedFile == null) {
      try {
        await cache.cacheManager.getSingleFile(
          cache.uri.toString(),
          key: cache.cacheKey,
        );
      } catch (_) {}
    }
  }

  Future<void> _cacheArtistArt(
    int sourceId,
    String? artistId,
    bool thumbnail,
  ) async {
    if (artistId == null) {
      return;
    }

    final cache = ref.read(cacheServiceProvider);
    try {
      final uri = await cache.artistArtUri(artistId, thumbnail: thumbnail);
      if (uri == null) {
        return;
      }

      await _cacheImage(UriCacheInfo(
        uri: uri,
        cacheKey:
            cache.artistArtCacheInfo(artistId, thumbnail: thumbnail).cacheKey,
        cacheManager: cache.imageCache,
      ));
    } catch (_) {}
  }

  bool _isCanceled(SourceIdentifiable item) {
    return state.listCancels.contains(SourceId.from(item));
  }

  List<FilterWith> _downloadFilters(SongDownloadState state) {
    switch (state) {
      case SongDownloadState.none:
        return [
          const FilterWith.isNull(column: 'download_task_id'),
          const FilterWith.isNull(column: 'download_file_path'),
        ];
      case SongDownloadState.completed:
        return [
          const FilterWith.isNull(column: 'download_file_path', invert: true),
        ];
      case SongDownloadState.inProgress:
        return [
          const FilterWith.isNull(column: 'download_task_id', invert: true),
        ];
    }
  }

  Future<List<Song>> _albumSongs(
    Album album,
    SongDownloadState state,
  ) {
    return ref
        .read(databaseProvider)
        .albumSongsList(
          SourceId.from(album),
          ListQuery(
            sort: const SortBy(column: 'disc, track'),
            filters: _downloadFilters(state).lock,
          ),
        )
        .get();
  }

  Future<List<Song>> _playlistSongs(
    Playlist playlist,
    SongDownloadState state,
  ) {
    return ref
        .read(databaseProvider)
        .playlistSongsList(
          SourceId.from(playlist),
          ListQuery(
            sort: const SortBy(column: 'playlist_songs.position'),
            filters: _downloadFilters(state).lock,
          ),
        )
        .get();
  }

  Future<void> _syncDownloadTasks() async {
    final tasks = await FlutterDownloader.loadTasks() ?? [];
    final downloads = tasks.map((e) => Download.fromTask(e)).toIList();

    state = state.copyWith(downloads: downloads);

    final db = ref.read(databaseProvider);
    final songs = await db.songsWithDownloadTasks().get();

    await _deleteTasksNotIn(songs.map((e) => e.downloadTaskId!).toList());

    final deleteTaskStatus = [
      DownloadTaskStatus.canceled,
      DownloadTaskStatus.failed,
      DownloadTaskStatus.undefined,
    ];

    for (var song in songs) {
      final download = downloads.firstWhereOrNull(
        (t) => t.taskId == song.downloadTaskId,
      );

      if (download == null) {
        await db.clearSongDownloadTaskBySong(song.sourceId, song.id);
        continue;
      }

      if (deleteTaskStatus.anyIs(download.status)) {
        await _clearFailedDownload(download);
      } else if (download.status == DownloadTaskStatus.complete) {
        await _completeDownload(download);
      }
    }
  }

  Future<bool> _tryDeleteFile(String filePath) async {
    try {
      final file = File(filePath);
      await file.delete();
      return true;
    } catch (_) {
      return false;
    }
  }

  Future<void> _deleteTasksNotIn(List<String> taskIds) async {
    if (taskIds.isEmpty) {
      return;
    }
    await FlutterDownloader.loadTasksWithRawQuery(
      query: 'DELETE FROM task WHERE task_id NOT IN '
          '(${taskIds.map((e) => "'$e'").join(',')})',
    );
  }

  Future<void> _deleteTask(String taskId) async {
    await FlutterDownloader.loadTasksWithRawQuery(
      query: 'DELETE FROM task WHERE task_id = \'$taskId\'',
    );
  }

  Future<DownloadTask?> _getTask(String taskId) async {
    return (await FlutterDownloader.loadTasksWithRawQuery(
      query: 'SELECT * FROM task WHERE task_id = \'$taskId\'',
    ))
        ?.firstOrNull;
  }

  Future<void> _completeDownload(Download download) async {
    final db = ref.read(databaseProvider);
    await db.completeSongDownload(
      path.join(download.savedDir, download.filename),
      download.taskId,
    );
    await _deleteTask(download.taskId);

    state = state.copyWith(
      downloads: state.downloads.removeWhere(
        (d) => d.taskId == download.taskId,
      ),
    );
  }

  Future<void> _clearFailedDownload(Download download) async {
    final db = ref.read(databaseProvider);
    await db.clearSongDownloadTask(download.taskId);
    await _deleteTask(download.taskId);
    await _tryDeleteFile(path.join(download.savedDir, download.filename));

    state = state.copyWith(
      downloads: state.downloads.removeWhere(
        (d) => d.taskId == download.taskId,
      ),
    );
  }

  void _bindBackgroundIsolate([retry = 0]) {
    final isSuccess = IsolateNameServer.registerPortWithName(
      _port.sendPort,
      'downloader_send_port',
    );

    if (!isSuccess && retry < 100) {
      _unbindBackgroundIsolate();
      _bindBackgroundIsolate(retry + 1);
      return;
    } else if (retry >= 100) {
      throw StateError('Could not bind background isolate for downloads');
    }

    _port.asyncMap((dynamic data) async {
      final taskId = (data as List<dynamic>)[0] as String;
      final status = DownloadTaskStatus.fromInt(data[1] as int);
      final progress = data[2] as int;

      var download = state.downloads.firstWhereOrNull(
        (task) => task.taskId == taskId,
      );
      if (download == null) {
        final task = await _getTask(taskId);
        if (task == null) {
          return;
        }
        download = Download.fromTask(task);
      }

      download = download.copyWith(status: status, progress: progress);

      state = state.copyWith(
        downloads: state.downloads.replaceFirstWhere(
          (d) => d.taskId == taskId,
          (d) => download!,
          addIfNotFound: true,
        ),
      );

      final deleteTaskStatus = [
        DownloadTaskStatus.canceled,
        DownloadTaskStatus.failed,
        DownloadTaskStatus.undefined,
      ];

      if (status == DownloadTaskStatus.complete) {
        await _completeDownload(download);
      } else if (deleteTaskStatus.anyIs(status)) {
        await _clearFailedDownload(download);
      }
    }).listen((_) {});
  }

  void _unbindBackgroundIsolate() {
    IsolateNameServer.removePortNameMapping('downloader_send_port');
  }

  @pragma('vm:entry-point')
  static void downloadCallback(
    String id,
    int status,
    int progress,
  ) {
    IsolateNameServer.lookupPortByName('downloader_send_port')?.send(
      [id, status, progress],
    );
  }
}
