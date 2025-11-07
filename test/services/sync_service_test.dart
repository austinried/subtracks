import 'package:subtracks/database/database.dart';
import 'package:subtracks/services/sync_services.dart';
import 'package:subtracks/sources/subsonic/source.dart';
import 'package:test/test.dart';

import '../util/database.dart';
import '../util/subsonic.dart';

void main() {
  late SubtracksDatabase db;
  late SubsonicSource source;
  late int sourceId;
  late int sourceIdOther;

  late SyncService sync;

  setUp(() async {
    db = testDatabase();
    source = SubsonicSource(testServerClients()[Servers.navidrome]!);
    sourceId = await db
        .into(db.sources)
        .insert(SourcesCompanion.insert(name: 'navidrome'));
    sourceIdOther = await db
        .into(db.sources)
        .insert(SourcesCompanion.insert(name: 'other'));

    sync = SyncService(
      db: db,
      source: source,
      sourceId: sourceId,
    );
  });

  tearDown(() async {
    await db.close();
  });

  test('syncArtists', () async {
    await db
        .into(db.artists)
        .insert(
          ArtistsCompanion.insert(
            sourceId: sourceId,
            id: 'shouldBeDeleted',
            name: 'shouldBeDeleted',
          ),
        );
    await db
        .into(db.artists)
        .insert(
          ArtistsCompanion.insert(
            sourceId: sourceIdOther,
            id: 'shouldBeKept',
            name: 'shouldBeKept',
          ),
        );

    await sync.syncArtists();

    expect(
      await db.managers.artists
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(2),
    );
    expect(
      await db.managers.artists
          .filter((f) => f.id.equals('shouldBeDeleted'))
          .getSingleOrNull(),
      isNull,
    );
    expect(
      await db.managers.artists
          .filter((f) => f.id.equals('shouldBeKept'))
          .getSingleOrNull(),
      isNotNull,
    );
  });
}
