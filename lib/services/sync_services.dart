import 'package:async/async.dart';
import 'package:collection/collection.dart';
import 'package:drift/drift.dart';

import '../database/database.dart';
import '../sources/music_source.dart';

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
      await syncArtists();
    });
  }

  Future<void> syncArtists() async {
    final sourceArtistIds = <String>{};

    await for (final artists in source.allArtists().slices(200)) {
      sourceArtistIds.addAll(artists.map((e) => e.id));

      await db.batch((batch) async {
        batch.insertAllOnConflictUpdate(
          db.artists,
          artists.map((artist) => artist.toDb(sourceId)),
        );
      });
    }

    for (var slice in sourceArtistIds.slices(kSqliteMaxVariableNumber - 1)) {
      await db.artists.deleteWhere(
        (tbl) => tbl.sourceId.equals(sourceId) & tbl.id.isNotIn(slice),
      );
    }
  }
}
