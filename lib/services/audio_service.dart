import 'dart:async';
import 'dart:math';

import 'package:audio_service/audio_service.dart';
import 'package:collection/collection.dart';
import 'package:drift/drift.dart' show Value;
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:just_audio/just_audio.dart';
import 'package:pool/pool.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:rxdart/rxdart.dart';
import 'package:synchronized/synchronized.dart';

import '../cache/image_cache.dart';
import '../database/database.dart';
import '../log.dart';
import '../models/music.dart';
import '../models/query.dart';
import '../models/support.dart';
import '../sources/music_source.dart';
import '../state/settings.dart';
import 'cache_service.dart';
import 'settings_service.dart';

part 'audio_service.g.dart';

class QueueSourceItem {
  final MediaItem mediaItem;
  final UriAudioSource audioSource;
  final QueueData queueData;

  const QueueSourceItem({
    required this.mediaItem,
    required this.audioSource,
    required this.queueData,
  });
}

class QueueSlice {
  final QueueSourceItem? prev;
  final QueueSourceItem? current;
  final QueueSourceItem? next;

  const QueueSlice(this.prev, this.current, this.next);
}

@Riverpod(keepAlive: true)
FutureOr<AudioControl> audioControlInit(AudioControlInitRef ref) async {
  final imageCache = ref.watch(imageCacheProvider);

  return AudioService.init(
    builder: () => AudioControl(AudioPlayer(), ref),
    config: const AudioServiceConfig(
      androidNotificationChannelId: 'com.subtracks2.channel.audio',
      androidNotificationChannelName: 'Music playback',
      androidNotificationIcon: 'drawable/ic_stat_name',
    ),
    cacheManager: imageCache,
    cacheKeyResolver: (mediaItem) =>
        mediaItem.data.artCache?.thumbnailArtCacheKey ?? '',
  );
}

@Riverpod(keepAlive: true)
AudioControl audioControl(AudioControlRef ref) {
  return ref.watch(audioControlInitProvider).requireValue;
}

class AudioControl extends BaseAudioHandler with QueueHandler, SeekHandler {
  static const radioLength = 10;

  Stream<Duration> get position => _player.positionStream;

  BehaviorSubject<QueueMode> queueMode = BehaviorSubject.seeded(QueueMode.user);
  BehaviorSubject<List<int>?> shuffleIndicies = BehaviorSubject.seeded(null);
  BehaviorSubject<AudioServiceRepeatMode> repeatMode =
      BehaviorSubject.seeded(AudioServiceRepeatMode.none);

  final Ref _ref;
  final AudioPlayer _player;

  int? _queueLength;
  int? _previousCurrentTrackIndex;

  final _syncPool = Pool(1);
  final _playLock = Lock();
  final _currentIndexIgnore = <int?>[null, 0];

  final ConcatenatingAudioSource _audioSource =
      ConcatenatingAudioSource(children: []);

  SubtracksDatabase get _db => _ref.read(databaseProvider);
  CacheService get _cache => _ref.read(cacheServiceProvider);
  MusicSource get _source => _ref.read(musicSourceProvider);
  int get _sourceId => _ref.read(sourceIdProvider);

  AudioControl(this._player, this._ref) {
    _player.playbackEventStream.listen(
      (PlaybackEvent event) {
        final playing = _player.playing;
        playbackState.add(playbackState.value.copyWith(
          controls: [
            MediaControl.skipToPrevious,
            if (playing) MediaControl.pause else MediaControl.play,
            MediaControl.stop,
            MediaControl.skipToNext,
          ],
          systemActions: const {
            MediaAction.seek,
          },
          androidCompactActionIndices: const [0, 1, 3],
          processingState: const {
            ProcessingState.idle: AudioProcessingState.idle,
            ProcessingState.loading: AudioProcessingState.loading,
            ProcessingState.buffering: AudioProcessingState.buffering,
            ProcessingState.ready: AudioProcessingState.ready,
            ProcessingState.completed: AudioProcessingState.completed,
          }[_player.processingState]!,
          playing: playing,
          updatePosition: _player.position,
          bufferedPosition: _player.bufferedPosition,
          queueIndex: event.currentIndex,
        ));
      },
      onError: (e, st) => log.warning('Audio playback error', e, st),
      cancelOnError: false,
    );

    shuffleIndicies.listen((value) {
      playbackState.add(playbackState.value.copyWith(
        shuffleMode: value != null
            ? AudioServiceShuffleMode.all
            : AudioServiceShuffleMode.none,
      ));
    });

    repeatMode.listen((value) {
      playbackState.add(playbackState.value.copyWith(repeatMode: value));
    });

    _player.processingStateStream.listen((event) async {
      if (event == ProcessingState.completed) {
        if (_audioSource.length > 0) {
          log.fine('completed');
          await stop();
          await seek(Duration.zero);
        }
      }
    });

    _ref.listen(sourceIdProvider, (_, __) async {
      await _clearAudioSource(true);
      await _db.clearQueue();
    });

    _ref.listen(maxBitrateProvider, (prev, next) async {
      if (prev?.valueOrNull != next.valueOrNull) {
        await _resyncQueue(true);
      }
    });

    _ref.listen(
        settingsServiceProvider.select((value) => value.app.streamFormat),
        (prev, next) async {
      await _resyncQueue(true);
    });

    _player.durationStream.listen((duration) {
      if (mediaItem.valueOrNull == null) return;

      final index = queue.valueOrNull?.indexOf(mediaItem.value!);

      final updated = mediaItem.value!.copyWith(duration: duration);
      mediaItem.add(updated);

      if (index != null) {
        queue.add(queue.value..replaceRange(index, index + 1, [updated]));
      }
    });

    _player.currentIndexStream.listen((index) async {
      if (_currentIndexIgnore.contains(index)) {
        _currentIndexIgnore.remove(index);
        return;
      }
      if (index == null || index >= _audioSource.sequence.length) return;

      final queueIndex = _audioSource.sequence[index].tag;
      if (queueIndex != null) {
        await _db.setCurrentTrack(queueIndex);
      }
    });

    _db.currentTrackIndex().watchSingleOrNull().listen((index) {
      // distict() except for when in loop one mode
      if (repeatMode.value != AudioServiceRepeatMode.one &&
          _previousCurrentTrackIndex == index) {
        return;
      }

      _previousCurrentTrackIndex = index;
      _syncPool.withResource(() => _syncQueue(index));
    });
  }

  Future<void> init() async {
    await _player.setAudioSource(_audioSource, preload: false);

    final last = await _db.getLastAudioState().getSingleOrNull();
    if (last == null) return;

    _queueLength = await _db.queueLength().getSingleOrNull();
    final repeat = {
      RepeatMode.none: AudioServiceRepeatMode.none,
      RepeatMode.all: AudioServiceRepeatMode.all,
      RepeatMode.one: AudioServiceRepeatMode.one,
    }[last.repeat]!;
    repeatMode.add(repeat);
    await repeatMode.firstWhere((e) => e == repeat);

    queueMode.add(last.queueMode);
    await queueMode.firstWhere((e) => e == last.queueMode);

    if (last.shuffleIndicies != null) {
      final indicies = last.shuffleIndicies!.unlock;
      shuffleIndicies.add(indicies);
      await shuffleIndicies.firstWhere((e) => e == indicies);
    }

    final startIndex = await _db.currentTrackIndex().getSingleOrNull();

    if (startIndex != null && _queueLength != null) {
      await _preparePlayer(startIndex);
    }
  }

  Future<void> playSongs({
    QueueMode mode = QueueMode.user,
    required QueueContextType context,
    String? contextId,
    required ListQuery query,
    required FutureOr<Iterable<Song>> Function(ListQuery query) getSongs,
    int? startIndex,
    bool? shuffle,
  }) async {
    if (!_playLock.locked) {
      return _playLock.synchronized(
        () => _playSongs(
          mode: mode,
          context: context,
          contextId: contextId,
          query: query,
          getSongs: getSongs,
          startIndex: startIndex,
          shuffle: shuffle,
        ),
      );
    }
  }

  Future<void> playRadio({
    required QueueContextType context,
    String? contextId,
    ListQuery query = const ListQuery(),
    required FutureOr<Iterable<Song>> Function(ListQuery query) getSongs,
  }) async {
    await playSongs(
      mode: QueueMode.radio,
      context: QueueContextType.library,
      contextId: contextId,
      query: query.copyWith(
        sort: SortBy(
          column: 'SIN(songs.ROWID + ${Random().nextInt(10000)})',
        ),
      ),
      getSongs: getSongs,
      startIndex: 0,
    );
  }

  Future<void> _playSongs({
    QueueMode mode = QueueMode.user,
    required QueueContextType context,
    String? contextId,
    required ListQuery query,
    required FutureOr<Iterable<Song>> Function(ListQuery query) getSongs,
    int? startIndex,
    bool? shuffle,
  }) async {
    shuffle = shuffle ?? shuffleIndicies.valueOrNull != null;
    queueMode.add(mode);

    if (mode == QueueMode.radio) {
      if (repeatMode.value != AudioServiceRepeatMode.none) {
        await _loop(AudioServiceRepeatMode.none);
      }
      if (shuffleIndicies.value != null) {
        await _shuffle(unshuffle: true);
      }
    }

    await _clearAudioSource();

    const limit = 500;
    _queueLength = 0;

    if ((startIndex == null || startIndex >= limit) && shuffle) {
      startIndex = Random().nextInt(limit);
    } else {
      startIndex ??= 0;
    }

    // clear the queue and load the initial songs only up to the startIndex
    await _db.transaction(() async {
      await _db.clearQueue();

      while (_queueLength! <= startIndex!) {
        final songs = await getSongs(query.copyWith(
          page: Pagination(limit: limit, offset: _queueLength!),
        ));
        await _loadQueueSongs(songs, _queueLength!, context, contextId);
        if (songs.length < limit) {
          break;
        }
      }
    });

    // if there are less songs than the limit and we're shuffling,
    // choose a new random startIndex
    if (startIndex >= _queueLength!) {
      startIndex = Random().nextInt(_queueLength!);
    }

    await _preparePlayer(startIndex, shuffle);
    await _db.setCurrentTrack(startIndex);
    play();

    const maxLength = 10000;

    // no need to do extra loading if we've already loaded everything
    if (_queueLength! < limit) return;
    while (true) {
      final songs = await getSongs(query.copyWith(
        page: Pagination(limit: limit, offset: _queueLength!),
      ));
      await _loadQueueSongs(songs, _queueLength!, context, contextId);
      if (songs.length < limit || _queueLength! >= maxLength) {
        break;
      }
    }
  }

  Future<void> _loadQueueSongs(
    Iterable<Song> songs,
    int total,
    QueueContextType context,
    String? contextId,
  ) async {
    await _db.insertQueue(songs.mapIndexed(
      (i, song) => QueueCompanion.insert(
        index: Value(i + (_queueLength ?? 0)),
        sourceId: song.sourceId,
        id: song.id,
        context: context,
        contextId: Value(contextId),
      ),
    ));

    _queueLength = (_queueLength ?? 0) + songs.length;
    if (shuffleIndicies.valueOrNull != null) {
      await _generateShuffleIndicies(startIndex: _player.currentIndex);
    }
  }

  Future<void> _preparePlayer(int startIndex, [bool? shuffle]) async {
    if (shuffle == true) {
      await _shuffle(startIndex: startIndex);
    } else if (shuffle == false) {
      await _shuffle(unshuffle: true);
    }

    final slice = await _getQueueSlice(startIndex);
    if (slice == null) {
      throw StateError('Could not get queue slice!');
    }

    final list =
        [slice.prev, slice.current, slice.next].whereNotNull().toList();

    mediaItem.add(slice.current!.mediaItem);
    queue.add(list.map((e) => e.mediaItem).toList());

    log.fine('addAll');
    await _audioSource.addAll(list.map((e) => e.audioSource).toList());
    await _player.seek(Duration.zero, index: list.indexOf(slice.current!));
  }

  Future<void> _syncQueue(int? index) async {
    if (index == null || _queueLength == null) return;

    final slice = await _getQueueSlice(index);
    if (slice == null || slice.current == null) return;

    mediaItem.add(slice.current!.mediaItem);
    queue.add(
      [slice.prev, slice.current, slice.next]
          .map((e) => e?.mediaItem)
          .whereNotNull()
          .toList(),
    );

    final sourceIndex = _player.currentIndex;
    final sourceNeedsNext = sourceIndex == _audioSource.length - 1;
    final sourceNeedsPrev = sourceIndex == 0;

    if (sourceNeedsNext && slice.next != null) {
      log.fine('add');
      await _audioSource.add(slice.next!.audioSource);
    }
    if (sourceNeedsPrev && slice.prev != null) {
      await _insertFirstAudioSource(slice.prev!.audioSource);
    }
  }

  Future<void> _loop(AudioServiceRepeatMode mode) async {
    repeatMode.add(mode);
    await repeatMode.firstWhere((e) => e == mode);

    await _resyncQueue();
  }

  Future<void> _generateShuffleIndicies({
    bool unshuffle = false,
    int? startIndex,
  }) async {
    final indicies = unshuffle
        ? null
        : (List.generate(_queueLength!, (i) => i + 1)
          ..insert(0, 0)
          ..removeLast()
          ..shuffle());

    if (indicies != null && startIndex != null) {
      indicies.removeAt(indicies.indexOf(startIndex));
      indicies.insert(0, startIndex);
    }
    shuffleIndicies.add(indicies);
    await shuffleIndicies.firstWhere((e) => e == indicies);
  }

  Future<void> _shuffle({bool unshuffle = false, int? startIndex}) async {
    await _generateShuffleIndicies(
      unshuffle: unshuffle,
      startIndex: startIndex,
    );

    await _resyncQueue();
  }

  Future<void> _resyncQueue([bool reloadCurrent = false]) async {
    return _syncPool.withResource(() async {
      final currentSource =
          _player.sequenceState?.currentSource as UriAudioSource?;
      if (currentSource == null) return;

      final currentSourceIndex = _player.sequence!.indexOf(currentSource);
      await _pruneAudioSources(currentSourceIndex);

      if (reloadCurrent && !currentSource.uri.isScheme('file')) {
        final position = _player.position;

        final item = (await _getQueueItems([currentSource.tag]))[0];

        await _audioSource.clear();
        await _audioSource.add(item.audioSource);
        await seek(position);
      }

      await _syncQueue(currentSource.tag);
    });
  }

  int _realIndex(int index) {
    if (shuffleIndicies.valueOrNull == null) {
      return index;
    }

    if (index < 0 || index >= shuffleIndicies.value!.length) {
      return -1;
    }

    return shuffleIndicies.value![index];
  }

  int _effectiveIndex(int index) {
    if (shuffleIndicies.valueOrNull == null) {
      return index;
    }

    return shuffleIndicies.value!.indexOf(index);
  }

  Future<void> _insertFirstAudioSource(AudioSource source) {
    log.fine('insert');
    final wait = _audioSource.insert(0, source);
    _currentIndexIgnore.add(1);
    return wait;
  }

  Future<void> _pruneAudioSources(int keepIndex) async {
    if (keepIndex > 0) {
      log.fine('removeRange 0');
      final wait = _audioSource.removeRange(0, keepIndex);
      _currentIndexIgnore.add(0);
      await wait;
    }
    if (_audioSource.length > 1) {
      log.fine('removeRange 1');
      await _audioSource.removeRange(1, _audioSource.length);
    }
  }

  Future<void> _clearAudioSource([bool clearMetadata = false]) async {
    // await _player.stop();
    log.fine('_clearAudioSource');
    await _audioSource.clear();

    if (clearMetadata) {
      mediaItem.add(null);
      queue.add([]);
      queueTitle.add('');
    }
  }

  Future<QueueSlice?> _getQueueSlice(int index) async {
    if (_queueLength == null) {
      return null;
    }

    final effectiveIndex = _effectiveIndex(index);

    int nextIndex;
    int prevIndex;
    if (repeatMode.value == AudioServiceRepeatMode.none) {
      nextIndex = _realIndex(effectiveIndex + 1);
      prevIndex = _realIndex(effectiveIndex - 1);
    } else if (repeatMode.value == AudioServiceRepeatMode.one) {
      nextIndex = index;
      prevIndex = index;
    } else {
      nextIndex = _realIndex(
        effectiveIndex + 1 >= _queueLength! ? 0 : effectiveIndex + 1,
      );
      prevIndex = _realIndex(
        effectiveIndex - 1 < 0 ? _queueLength! - 1 : effectiveIndex - 1,
      );
    }

    final slice = await _getQueueItems([prevIndex, index, nextIndex]);
    final current = slice.firstWhereOrNull(
      (e) => e.queueData.index == index,
    );
    final next = slice.firstWhereOrNull((e) => e.queueData.index == nextIndex);
    final prev = slice.firstWhereOrNull((e) => e.queueData.index == prevIndex);

    return QueueSlice(prev, current, next);
  }

  Future<List<QueueSourceItem>> _getQueueItems(List<int> indexes) async {
    final slice = await _db.queueInIndicies(indexes).get();
    final songs =
        await _db.songsInIds(_sourceId, slice.map((e) => e.id).toList()).get();
    final songMap = {for (var song in songs) song.id: song};

    final albumIds = songs.map((e) => e.albumId).whereNotNull().toSet();
    final albums = await _db.albumsInIds(_sourceId, albumIds.toList()).get();
    final albumArtMap = {
      for (var album in albums) album.id: _mapArtCache(album)
    };

    final queueItems = slice.map(
      (item) => _mapSong(
        songMap[item.id]!,
        MediaItemData(
          sourceId: item.sourceId,
          contextType: item.context,
          contextId: item.contextId,
          artCache: albumArtMap[songMap[item.id]!.albumId],
        ),
        item,
      ),
    );

    return queueItems.toList();
  }

  QueueSourceItem _mapSong(Song song, MediaItemData data, QueueData queueData) {
    final item = MediaItem(
      id: song.id,
      title: song.title,
      artist: song.artist,
      album: song.album,
      duration: song.duration,
      artUri: data.artCache?.thumbnailArtUri ?? _cache.placeholderThumbImageUri,
      extras: {},
    );
    item.data = data;

    return QueueSourceItem(
      mediaItem: item,
      audioSource: song.downloadFilePath != null
          ? AudioSource.file(song.downloadFilePath!, tag: queueData.index)
          : AudioSource.uri(_source.streamUri(song.id), tag: queueData.index),
      queueData: queueData,
    );
  }

  MediaItemArtCache _mapArtCache(Album album) {
    final full = _cache.albumArt(album, thumbnail: false);
    final thumbnail = _cache.albumArt(album, thumbnail: true);
    return MediaItemArtCache(
      fullArtUri: full.uri,
      fullArtCacheKey: full.cacheKey,
      thumbnailArtUri: thumbnail.uri,
      thumbnailArtCacheKey: thumbnail.cacheKey,
    );
  }

  ///
  /// AudioHandler
  ///

  @override
  Future<void> play() async {
    await _player.play();
  }

  @override
  Future<void> pause() async {
    await _player.pause();
  }

  @override
  Future<void> stop() async {
    await _player.stop();
  }

  @override
  Future<void> seek(Duration position) async {
    await _player.seek(position);
  }

  @override
  Future<void> skipToNext() => _player.seekToNext();

  @override
  Future<void> skipToPrevious() => _player.seekToPrevious();

  @override
  Future<void> skipToQueueItem(int index) async {
    if (_player.effectiveIndices == null || _player.effectiveIndices!.isEmpty) {
      return;
    }

    index = _player.effectiveIndices![index];
    if (index < 0 || index >= queue.value.length) {
      return;
    }

    await _player.seek(Duration.zero, index: index);
  }

  @override
  Future<void> setRepeatMode(AudioServiceRepeatMode repeatMode) async {
    if (queueMode.value == QueueMode.radio) {
      switch (repeatMode) {
        case AudioServiceRepeatMode.all:
        case AudioServiceRepeatMode.group:
        case AudioServiceRepeatMode.one:
          return _loop(AudioServiceRepeatMode.one);
        default:
          return _loop(AudioServiceRepeatMode.none);
      }
    }

    return _loop(repeatMode);
  }

  @override
  Future<void> setShuffleMode(AudioServiceShuffleMode shuffleMode) async {
    if (queueMode.value == QueueMode.radio) {
      return;
    }

    switch (shuffleMode) {
      case AudioServiceShuffleMode.all:
      case AudioServiceShuffleMode.group:
        return _shuffle(startIndex: _player.sequenceState?.currentSource?.tag);
      case AudioServiceShuffleMode.none:
        return _shuffle(unshuffle: true);
    }
  }
}
