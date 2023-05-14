import 'dart:io';

import 'package:collection/collection.dart';
import 'package:drift/drift.dart';
import 'package:drift/isolate.dart';
import 'package:drift/native.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../log.dart';
import '../models/music.dart';
import '../models/query.dart';
import '../models/settings.dart';
import '../models/support.dart';
import 'converters.dart';
import 'error_logging_database.dart';

part 'database.g.dart';

// don't exceed SQLITE_MAX_VARIABLE_NUMBER (32766 for version >= 3.32.0)
// https://www.sqlite.org/limits.html
const kSqliteMaxVariableNumber = 32766;

@DriftDatabase(include: {'tables.drift'})
class SubtracksDatabase extends _$SubtracksDatabase {
  SubtracksDatabase() : super(_openConnection());
  SubtracksDatabase.connection(QueryExecutor e) : super(e);

  @override
  int get schemaVersion => 1;

  @override
  MigrationStrategy get migration {
    return MigrationStrategy(
      beforeOpen: (details) async {
        await customStatement('PRAGMA foreign_keys = ON');
      },
    );
  }

  /// Runs a database opertion in a background isolate.
  ///
  /// **Only pass top-level functions to [computation]!**
  ///
  /// **Do not use non-serializable data inside [computation]!**
  Future<Ret> background<Ret>(
    FutureOr<Ret> Function(SubtracksDatabase) computation,
  ) async {
    return computeWithDatabase(
      connect: SubtracksDatabase.connection,
      computation: computation,
    );
  }

  MultiSelectable<Album> albumsList(int sourceId, ListQuery opt) {
    return filterAlbums(
      (_) => _filterPredicate('albums', sourceId, opt),
      (_) => _filterOrderBy(opt),
      (_) => _filterLimit(opt),
    );
  }

  MultiSelectable<Album> albumsListDownloaded(int sourceId, ListQuery opt) {
    return filterAlbumsDownloaded(
      (_, __) => _filterPredicate('albums', sourceId, opt),
      (_, __) => _filterOrderBy(opt),
      (_, __) => _filterLimit(opt),
    );
  }

  MultiSelectable<Artist> artistsList(int sourceId, ListQuery opt) {
    return filterArtists(
      (_) => _filterPredicate('artists', sourceId, opt),
      (_) => _filterOrderBy(opt),
      (_) => _filterLimit(opt),
    );
  }

  MultiSelectable<Artist> artistsListDownloaded(int sourceId, ListQuery opt) {
    return filterArtistsDownloaded(
      (_, __, ___) => _filterPredicate('artists', sourceId, opt),
      (_, __, ___) => _filterOrderBy(opt),
      (_, __, ___) => _filterLimit(opt),
    );
  }

  MultiSelectable<Playlist> playlistsList(int sourceId, ListQuery opt) {
    return filterPlaylists(
      (_) => _filterPredicate('playlists', sourceId, opt),
      (_) => _filterOrderBy(opt),
      (_) => _filterLimit(opt),
    );
  }

  MultiSelectable<Playlist> playlistsListDownloaded(
      int sourceId, ListQuery opt) {
    return filterPlaylistsDownloaded(
      (_, __, ___) => _filterPredicate('playlists', sourceId, opt),
      (_, __, ___) => _filterOrderBy(opt),
      (_, __, ___) => _filterLimit(opt),
    );
  }

  MultiSelectable<Song> songsList(int sourceId, ListQuery opt) {
    return filterSongs(
      (_) => _filterPredicate('songs', sourceId, opt),
      (_) => _filterOrderBy(opt),
      (_) => _filterLimit(opt),
    );
  }

  MultiSelectable<Song> songsListDownloaded(int sourceId, ListQuery opt) {
    return filterSongsDownloaded(
      (_) => _filterPredicate('songs', sourceId, opt),
      (_) => _filterOrderBy(opt),
      (_) => _filterLimit(opt),
    );
  }

  Expression<bool> _filterPredicate(String table, int sourceId, ListQuery opt) {
    return opt.filters.map((filter) => buildFilter<bool>(filter)).fold(
          CustomExpression('$table.source_id = $sourceId'),
          (previousValue, element) => previousValue & element,
        );
  }

  OrderBy _filterOrderBy(ListQuery opt) {
    return opt.sort != null
        ? OrderBy([_buildOrder(opt.sort!)])
        : const OrderBy.nothing();
  }

  Limit _filterLimit(ListQuery opt) {
    return Limit(opt.page.limit, opt.page.offset);
  }

  MultiSelectable<Song> albumSongsList(SourceId sid, ListQuery opt) {
    return listQuery(
      select(songs)
        ..where((tbl) =>
            tbl.sourceId.equals(sid.sourceId) & tbl.albumId.equals(sid.id)),
      opt,
    );
  }

  MultiSelectable<Song> songsByAlbumList(int sourceId, ListQuery opt) {
    return filterSongsByGenre(
      (_, __) => _filterPredicate('songs', sourceId, opt),
      (_, __) => _filterOrderBy(opt),
      (_, __) => _filterLimit(opt),
    );
  }

  MultiSelectable<Song> playlistSongsList(SourceId sid, ListQuery opt) {
    return listQueryJoined(
      select(songs).join([
        innerJoin(
          playlistSongs,
          playlistSongs.sourceId.equalsExp(songs.sourceId) &
              playlistSongs.songId.equalsExp(songs.id),
          useColumns: false,
        ),
      ])
        ..where(playlistSongs.sourceId.equals(sid.sourceId) &
            playlistSongs.playlistId.equals(sid.id)),
      opt,
    ).map((row) => row.readTable(songs));
  }

  Future<void> saveArtists(Iterable<ArtistsCompanion> artists) async {
    await batch((batch) {
      batch.insertAllOnConflictUpdate(this.artists, artists);
    });
  }

  Future<void> deleteArtistsNotIn(int sourceId, Set<String> ids) {
    return transaction(() async {
      final allIds = (await (selectOnly(artists)
                ..addColumns([artists.id])
                ..where(artists.sourceId.equals(sourceId)))
              .map((row) => row.read(artists.id))
              .get())
          .whereNotNull()
          .toSet();
      final downloadIds = (await artistIdsWithDownloadStatus(sourceId).get())
          .whereNotNull()
          .toSet();

      final diff = allIds.difference(downloadIds).difference(ids);
      for (var slice in diff.slices(kSqliteMaxVariableNumber)) {
        await (delete(artists)
              ..where(
                  (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isIn(slice)))
            .go();
      }
    });
  }

  Future<void> saveAlbums(Iterable<AlbumsCompanion> albums) async {
    await batch((batch) {
      batch.insertAllOnConflictUpdate(this.albums, albums);
    });
  }

  Future<void> deleteAlbumsNotIn(int sourceId, Set<String> ids) {
    return transaction(() async {
      final allIds = (await (selectOnly(albums)
                ..addColumns([albums.id])
                ..where(albums.sourceId.equals(sourceId)))
              .map((row) => row.read(albums.id))
              .get())
          .whereNotNull()
          .toSet();
      final downloadIds = (await albumIdsWithDownloadStatus(sourceId).get())
          .whereNotNull()
          .toSet();

      final diff = allIds.difference(downloadIds).difference(ids);
      for (var slice in diff.slices(kSqliteMaxVariableNumber)) {
        await (delete(albums)
              ..where(
                  (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isIn(slice)))
            .go();
      }
    });
  }

  Future<void> savePlaylists(
    Iterable<PlaylistWithSongsCompanion> playlistsWithSongs,
  ) async {
    final playlists = playlistsWithSongs.map((e) => e.playist);
    final playlistSongs = playlistsWithSongs.expand((e) => e.songs);
    final sourceId = playlists.first.sourceId.value;

    await (delete(this.playlistSongs)
          ..where(
            (tbl) =>
                tbl.sourceId.equals(sourceId) &
                tbl.playlistId.isIn(playlists.map((e) => e.id.value)),
          ))
        .go();

    await batch((batch) {
      batch.insertAllOnConflictUpdate(this.playlists, playlists);
      batch.insertAllOnConflictUpdate(this.playlistSongs, playlistSongs);
    });
  }

  Future<void> deletePlaylistsNotIn(int sourceId, Set<String> ids) {
    return transaction(() async {
      final allIds = (await (selectOnly(playlists)
                ..addColumns([playlists.id])
                ..where(playlists.sourceId.equals(sourceId)))
              .map((row) => row.read(playlists.id))
              .get())
          .whereNotNull()
          .toSet();
      final downloadIds = (await playlistIdsWithDownloadStatus(sourceId).get())
          .whereNotNull()
          .toSet();

      final diff = allIds.difference(downloadIds).difference(ids);
      for (var slice in diff.slices(kSqliteMaxVariableNumber)) {
        await (delete(playlists)
              ..where(
                  (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isIn(slice)))
            .go();
        await (delete(playlistSongs)
              ..where((tbl) =>
                  tbl.sourceId.equals(sourceId) & tbl.playlistId.isIn(slice)))
            .go();
      }
    });
  }

  Future<void> savePlaylistSongs(
    int sourceId,
    List<String> ids,
    Iterable<PlaylistSongsCompanion> playlistSongs,
  ) async {
    await (delete(this.playlistSongs)
          ..where(
            (tbl) => tbl.sourceId.equals(sourceId) & tbl.playlistId.isIn(ids),
          ))
        .go();
    await batch((batch) {
      batch.insertAllOnConflictUpdate(this.playlistSongs, playlistSongs);
    });
  }

  Future<void> saveSongs(Iterable<SongsCompanion> songs) async {
    await batch((batch) {
      batch.insertAllOnConflictUpdate(this.songs, songs);
    });
  }

  Future<void> deleteSongsNotIn(int sourceId, Set<String> ids) {
    return transaction(() async {
      final allIds = (await (selectOnly(songs)
                ..addColumns([songs.id])
                ..where(
                  songs.sourceId.equals(sourceId) &
                      songs.downloadFilePath.isNull() &
                      songs.downloadTaskId.isNull(),
                ))
              .map((row) => row.read(songs.id))
              .get())
          .whereNotNull()
          .toSet();

      final diff = allIds.difference(ids);
      for (var slice in diff.slices(kSqliteMaxVariableNumber)) {
        await (delete(songs)
              ..where(
                  (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isIn(slice)))
            .go();
        await (delete(playlistSongs)
              ..where(
                (tbl) => tbl.sourceId.equals(sourceId) & tbl.songId.isIn(slice),
              ))
            .go();
      }
    });
  }

  Selectable<LastBottomNavStateData> getLastBottomNavState() {
    return select(lastBottomNavState)..where((tbl) => tbl.id.equals(1));
  }

  Future<void> saveLastBottomNavState(LastBottomNavStateData update) {
    return into(lastBottomNavState).insertOnConflictUpdate(update);
  }

  Selectable<LastLibraryStateData> getLastLibraryState() {
    return select(lastLibraryState)..where((tbl) => tbl.id.equals(1));
  }

  Future<void> saveLastLibraryState(LastLibraryStateData update) {
    return into(lastLibraryState).insertOnConflictUpdate(update);
  }

  Selectable<LastAudioStateData> getLastAudioState() {
    return select(lastAudioState)..where((tbl) => tbl.id.equals(1));
  }

  Future<void> saveLastAudioState(LastAudioStateCompanion update) {
    return into(lastAudioState).insertOnConflictUpdate(update);
  }

  Future<void> insertQueue(Iterable<QueueCompanion> songs) async {
    await batch((batch) {
      batch.insertAll(queue, songs);
    });
  }

  Future<void> clearQueue() async {
    await delete(queue).go();
  }

  Future<void> setCurrentTrack(int index) async {
    await transaction(() async {
      await (update(queue)..where((tbl) => tbl.index.equals(index).not()))
          .write(const QueueCompanion(currentTrack: Value(null)));
      await (update(queue)..where((tbl) => tbl.index.equals(index)))
          .write(const QueueCompanion(currentTrack: Value(true)));
    });
  }

  Future<void> createSource(
    SourcesCompanion source,
    SubsonicSourcesCompanion subsonic,
  ) async {
    await transaction(() async {
      final count = await sourcesCount().getSingle();
      if (count == 0) {
        source = source.copyWith(isActive: const Value(true));
      }

      final id = await into(sources).insert(source);
      subsonic = subsonic.copyWith(sourceId: Value(id));
      await into(subsonicSources).insert(subsonic);
    });
  }

  Future<void> updateSource(SubsonicSettings source) async {
    await transaction(() async {
      await into(sources).insertOnConflictUpdate(source.toSourceInsertable());
      await into(subsonicSources)
          .insertOnConflictUpdate(source.toSubsonicInsertable());
    });
  }

  Future<void> deleteSource(int sourceId) async {
    await transaction(() async {
      await (delete(subsonicSources)
            ..where((tbl) => tbl.sourceId.equals(sourceId)))
          .go();
      await (delete(sources)..where((tbl) => tbl.id.equals(sourceId))).go();

      await (delete(songs)..where((tbl) => tbl.sourceId.equals(sourceId))).go();
      await (delete(albums)..where((tbl) => tbl.sourceId.equals(sourceId)))
          .go();
      await (delete(artists)..where((tbl) => tbl.sourceId.equals(sourceId)))
          .go();
      await (delete(playlistSongs)
            ..where((tbl) => tbl.sourceId.equals(sourceId)))
          .go();
      await (delete(playlists)..where((tbl) => tbl.sourceId.equals(sourceId)))
          .go();
    });
  }

  Future<void> setActiveSource(int id) async {
    await batch((batch) {
      batch.update(
        sources,
        const SourcesCompanion(isActive: Value(null)),
        where: (t) => t.id.isNotValue(id),
      );
      batch.update(
        sources,
        const SourcesCompanion(isActive: Value(true)),
        where: (t) => t.id.equals(id),
      );
    });
  }

  Future<void> updateSettings(AppSettingsCompanion settings) async {
    await into(appSettings).insertOnConflictUpdate(settings);
  }
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'subtracks.sqlite'));
    // return NativeDatabase.createInBackground(file, logStatements: true);

    return ErrorLoggingDatabase(
      NativeDatabase.createInBackground(file),
      (e, s) => log.severe('SQL Error', e, s),
    );
  });
}

@Riverpod(keepAlive: true)
SubtracksDatabase database(DatabaseRef ref) {
  return SubtracksDatabase();
}

OrderingTerm _buildOrder(SortBy sort) {
  OrderingMode? mode =
      sort.dir == SortDirection.asc ? OrderingMode.asc : OrderingMode.desc;
  return OrderingTerm(
    expression: CustomExpression(sort.column),
    mode: mode,
  );
}

SimpleSelectStatement<T, R> listQuery<T extends HasResultSet, R>(
  SimpleSelectStatement<T, R> query,
  ListQuery opt,
) {
  if (opt.page.limit > 0) {
    query.limit(opt.page.limit, offset: opt.page.offset);
  }

  if (opt.sort != null) {
    OrderingMode? mode = opt.sort != null && opt.sort!.dir == SortDirection.asc
        ? OrderingMode.asc
        : OrderingMode.desc;
    query.orderBy([
      (t) => OrderingTerm(
            expression: CustomExpression(opt.sort!.column),
            mode: mode,
          )
    ]);
  }

  for (var filter in opt.filters) {
    query.where((tbl) => buildFilter(filter));
  }

  return query;
}

JoinedSelectStatement<T, R> listQueryJoined<T extends HasResultSet, R>(
  JoinedSelectStatement<T, R> query,
  ListQuery opt,
) {
  if (opt.page.limit > 0) {
    query.limit(opt.page.limit, offset: opt.page.offset);
  }

  if (opt.sort != null) {
    OrderingMode? mode = opt.sort != null && opt.sort!.dir == SortDirection.asc
        ? OrderingMode.asc
        : OrderingMode.desc;
    query.orderBy([
      OrderingTerm(
        expression: CustomExpression(opt.sort!.column),
        mode: mode,
      )
    ]);
  }

  for (var filter in opt.filters) {
    query.where(buildFilter(filter));
  }

  return query;
}

CustomExpression<T> buildFilter<T extends Object>(
  FilterWith filter,
) {
  return filter.when(
    equals: (column, value, invert) => CustomExpression<T>(
      '$column ${invert ? '<>' : '='} \'$value\'',
    ),
    greaterThan: (column, value, orEquals) => CustomExpression<T>(
      '$column ${orEquals ? '>=' : '>'} $value',
    ),
    isNull: (column, invert) => CustomExpression<T>(
      '$column ${invert ? 'IS NOT' : 'IS'} NULL',
    ),
    betweenInt: (column, from, to) => CustomExpression<T>(
      '$column BETWEEN $from AND $to',
    ),
    isIn: (column, invert, values) => CustomExpression<T>(
      '$column ${invert ? 'NOT IN' : 'IN'} (${values.join(',')})',
    ),
  );
}

class AlbumSongsCompanion {
  final AlbumsCompanion album;
  final Iterable<SongsCompanion> songs;

  AlbumSongsCompanion(this.album, this.songs);
}

class ArtistAlbumsCompanion {
  final ArtistsCompanion artist;
  final Iterable<AlbumsCompanion> albums;

  ArtistAlbumsCompanion(this.artist, this.albums);
}

class PlaylistWithSongsCompanion {
  final PlaylistsCompanion playist;
  final Iterable<PlaylistSongsCompanion> songs;

  PlaylistWithSongsCompanion(this.playist, this.songs);
}

// Future<void> saveArtist(
//   SubtracksDatabase db,
//   ArtistAlbumsCompanion artistAlbums,
// ) async {
//   return db.background((db) async {
//     final artist = artistAlbums.artist;
//     final albums = artistAlbums.albums;

//     await db.batch((batch) {
//       batch.insertAllOnConflictUpdate(db.artists, [artist]);
//       batch.insertAllOnConflictUpdate(db.albums, albums);

//       // remove this artistId from albums not found in source
//       // don't delete them since they coud have been moved to another artist
//       // that we haven't synced yet
//       final albumIds = {for (var a in albums) a.id.value};
//       batch.update(
//         db.albums,
//         const AlbumsCompanion(artistId: Value(null)),
//         where: (tbl) =>
//             tbl.sourceId.equals(artist.sourceId.value) &
//             tbl.artistId.equals(artist.id.value) &
//             tbl.id.isNotIn(albumIds),
//       );
//     });
//   });
// }

// Future<void> saveAlbum(
//   SubtracksDatabase db,
//   AlbumSongsCompanion albumSongs,
// ) async {
//   return db.background((db) async {
//     final album = albumSongs.album.copyWith(synced: Value(DateTime.now()));
//     final songs = albumSongs.songs;

//     final songIds = {for (var a in songs) a.id.value};
//     final hardDeletedSongIds = (await (db.selectOnly(db.songs)
//               ..addColumns([db.songs.id])
//               ..where(
//                 db.songs.sourceId.equals(album.sourceId.value) &
//                     db.songs.albumId.equals(album.id.value) &
//                     db.songs.id.isNotIn(songIds) &
//                     db.songs.downloadFilePath.isNull() &
//                     db.songs.downloadTaskId.isNull(),
//               ))
//             .map((row) => row.read(db.songs.id))
//             .get())
//         .whereNotNull();

//     await db.batch((batch) {
//       batch.insertAllOnConflictUpdate(db.albums, [album]);
//       batch.insertAllOnConflictUpdate(db.songs, songs);

//       // soft delete songs that have been downloaded so that the user
//       // can decide to keep or remove them later
//       // TODO: add a setting to skip soft delete and just remove download too
//       batch.update(
//         db.songs,
//         const SongsCompanion(isDeleted: Value(true)),
//         where: (tbl) =>
//             tbl.sourceId.equals(album.sourceId.value) &
//             tbl.albumId.equals(album.id.value) &
//             tbl.id.isNotIn(songIds) &
//             (tbl.downloadFilePath.isNotNull() | tbl.downloadTaskId.isNotNull()),
//       );

//       // safe to hard delete songs that have not been downloaded
//       batch.deleteWhere(
//         db.songs,
//         (tbl) =>
//             tbl.sourceId.equals(album.sourceId.value) &
//             tbl.id.isIn(hardDeletedSongIds),
//       );

//       // also need to remove these songs from any playlists that contain them
//       batch.deleteWhere(
//         db.playlistSongs,
//         (tbl) =>
//             tbl.sourceId.equals(album.sourceId.value) &
//             tbl.songId.isIn(hardDeletedSongIds),
//       );
//     });
//   });
// }

// Future<void> savePlaylist(
//   SubtracksDatabase db,
//   PlaylistWithSongsCompanion playlistWithSongs,
// ) async {
//   return db.background((db) async {
//     final playlist =
//         playlistWithSongs.playist.copyWith(synced: Value(DateTime.now()));
//     final songs = playlistWithSongs.songs;

//     await db.batch((batch) {
//       batch.insertAllOnConflictUpdate(db.playlists, [playlist]);
//       batch.insertAllOnConflictUpdate(db.songs, songs);

//       batch.insertAllOnConflictUpdate(
//         db.playlistSongs,
//         songs.mapIndexed(
//           (index, song) => PlaylistSongsCompanion.insert(
//             sourceId: playlist.sourceId.value,
//             playlistId: playlist.id.value,
//             songId: song.id.value,
//             position: index,
//           ),
//         ),
//       );

//       // the new playlist could be shorter than the old one, so we delete
//       // playlist songs above our new playlist's length
//       batch.deleteWhere(
//         db.playlistSongs,
//         (tbl) =>
//             tbl.sourceId.equals(playlist.sourceId.value) &
//             tbl.playlistId.equals(playlist.id.value) &
//             tbl.position.isBiggerOrEqualValue(songs.length),
//       );
//     });
//   });
// }
