import 'package:async/async.dart';
import 'package:collection/collection.dart';
import 'package:drift/drift.dart';

import '../database/database.dart';
import '../sources/music_source.dart';

const kSliceSize = 200;

class SyncService {
  SyncService({
    required this.source,
    required this.db,
    required this.sourceId,
  });

  final MusicSource source;
  final SubtracksDatabase db;
  final int sourceId;

  Future<void> sync() async {
    await db.transaction(() async {
      await Future.wait([
        syncArtists(),
        syncAlbums(),
        syncSongs(),
        syncPlaylists(),
        syncPlaylistSongs(),
      ]);
    });
  }

  Future<void> syncArtists() async {
    final sourceIds = <String>{};

    await for (final slice in source.allArtists().slices(kSliceSize)) {
      sourceIds.addAll(slice.map((e) => e.id));

      await db.batch((batch) async {
        batch.insertAllOnConflictUpdate(
          db.artists,
          slice.map((artist) => artist.toDb(sourceId)),
        );
      });
    }

    for (var slice in sourceIds.slices(kSqliteMaxVariableNumber - 1)) {
      await db.artists.deleteWhere(
        (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isNotIn(slice),
      );
    }
  }

  Future<void> syncAlbums() async {
    final sourceIds = <String>{};

    await for (final slice in source.allAlbums().slices(kSliceSize)) {
      sourceIds.addAll(slice.map((e) => e.id));

      await db.batch((batch) async {
        batch.insertAllOnConflictUpdate(
          db.albums,
          slice.map((e) => e.toDb(sourceId)),
        );
      });
    }

    for (var slice in sourceIds.slices(kSqliteMaxVariableNumber - 1)) {
      await db.albums.deleteWhere(
        (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isNotIn(slice),
      );
    }
  }

  Future<void> syncSongs() async {
    final sourceIds = <String>{};

    await for (final slice in source.allSongs().slices(kSliceSize)) {
      sourceIds.addAll(slice.map((e) => e.id));

      await db.batch((batch) async {
        batch.insertAllOnConflictUpdate(
          db.songs,
          slice.map((e) => e.toDb(sourceId)),
        );
      });
    }

    for (var slice in sourceIds.slices(kSqliteMaxVariableNumber - 1)) {
      await db.songs.deleteWhere(
        (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isNotIn(slice),
      );
    }
  }

  Future<void> syncPlaylists() async {
    final sourceIds = <String>{};

    await for (final slice in source.allPlaylists().slices(kSliceSize)) {
      sourceIds.addAll(slice.map((e) => e.id));

      await db.batch((batch) async {
        batch.insertAllOnConflictUpdate(
          db.playlists,
          slice.map((e) => e.toDb(sourceId)),
        );
      });
    }

    for (var slice in sourceIds.slices(kSqliteMaxVariableNumber - 1)) {
      await db.playlists.deleteWhere(
        (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isNotIn(slice),
      );
    }
  }

  Future<void> syncPlaylistSongs() async {
    final sourceIds = <(String, String)>{};

    await for (final slice in source.allPlaylistSongs().slices(kSliceSize)) {
      sourceIds.addAll(slice.map((e) => (e.playlistId, e.songId)));

      await db.batch((batch) async {
        batch.insertAllOnConflictUpdate(
          db.playlistSongs,
          slice.map((e) => e.toDb(sourceId)),
        );
      });
    }

    for (var slice in sourceIds.slices((kSqliteMaxVariableNumber ~/ 2) - 1)) {
      await db.playlistSongs.deleteWhere(
        (tbl) =>
            tbl.sourceId.equals(sourceId) &
            tbl.playlistId.isNotIn(slice.map((e) => e.$1)) &
            tbl.songId.isNotIn(slice.map((e) => e.$2)),
      );
    }
  }
}
