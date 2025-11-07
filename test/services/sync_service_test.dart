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

  test('syncAlbums', () async {
    await db
        .into(db.albums)
        .insert(
          AlbumsCompanion.insert(
            sourceId: sourceId,
            id: 'shouldBeDeleted',
            name: 'shouldBeDeleted',
            created: DateTime.now(),
          ),
        );
    await db
        .into(db.albums)
        .insert(
          AlbumsCompanion.insert(
            sourceId: sourceIdOther,
            id: 'shouldBeKept',
            name: 'shouldBeKept',
            created: DateTime.now(),
          ),
        );

    await sync.syncAlbums();

    expect(
      await db.managers.albums
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(3),
    );
    expect(
      await db.managers.albums
          .filter((f) => f.id.equals('shouldBeDeleted'))
          .getSingleOrNull(),
      isNull,
    );
    expect(
      await db.managers.albums
          .filter((f) => f.id.equals('shouldBeKept'))
          .getSingleOrNull(),
      isNotNull,
    );
  });

  test('syncSongs', () async {
    await db
        .into(db.songs)
        .insert(
          SongsCompanion.insert(
            sourceId: sourceId,
            id: 'shouldBeDeleted',
            title: 'shouldBeDeleted',
          ),
        );
    await db
        .into(db.songs)
        .insert(
          SongsCompanion.insert(
            sourceId: sourceIdOther,
            id: 'shouldBeKept',
            title: 'shouldBeKept',
          ),
        );

    await sync.syncSongs();

    expect(
      await db.managers.songs
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(20),
    );
    expect(
      await db.managers.songs
          .filter((f) => f.id.equals('shouldBeDeleted'))
          .getSingleOrNull(),
      isNull,
    );
    expect(
      await db.managers.songs
          .filter((f) => f.id.equals('shouldBeKept'))
          .getSingleOrNull(),
      isNotNull,
    );
  });

  test('syncPlaylists', () async {
    await db
        .into(db.playlists)
        .insert(
          PlaylistsCompanion.insert(
            sourceId: sourceId,
            id: 'shouldBeDeleted',
            name: 'shouldBeDeleted',
            created: DateTime.now(),
            changed: DateTime.now(),
          ),
        );
    await db
        .into(db.playlists)
        .insert(
          PlaylistsCompanion.insert(
            sourceId: sourceIdOther,
            id: 'shouldBeKept',
            name: 'shouldBeKept',
            created: DateTime.now(),
            changed: DateTime.now(),
          ),
        );

    await sync.syncPlaylists();

    expect(
      await db.managers.playlists
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(1),
    );
    expect(
      await db.managers.playlists
          .filter((f) => f.id.equals('shouldBeDeleted'))
          .getSingleOrNull(),
      isNull,
    );
    expect(
      await db.managers.playlists
          .filter((f) => f.id.equals('shouldBeKept'))
          .getSingleOrNull(),
      isNotNull,
    );
  });

  test('syncPlaylistSongs', () async {
    await db
        .into(db.playlistSongs)
        .insert(
          PlaylistSongsCompanion.insert(
            sourceId: sourceId,
            playlistId: 'shouldBeDeleted',
            songId: 'shouldBeDeleted',
            position: 1,
          ),
        );
    await db
        .into(db.playlistSongs)
        .insert(
          PlaylistSongsCompanion.insert(
            sourceId: sourceIdOther,
            playlistId: 'shouldBeKept',
            songId: 'shouldBeKept',
            position: 1,
          ),
        );

    await sync.syncPlaylistSongs();

    expect(
      await db.managers.playlistSongs
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(7),
    );
    expect(
      await db.managers.playlistSongs
          .filter((f) => f.playlistId.equals('shouldBeDeleted'))
          .getSingleOrNull(),
      isNull,
    );
    expect(
      await db.managers.playlistSongs
          .filter((f) => f.playlistId.equals('shouldBeKept'))
          .getSingleOrNull(),
      isNotNull,
    );
  });

  test('syncPlaylistSongs', () async {
    await sync.sync();

    expect(
      await db.managers.artists
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(2),
    );
    expect(
      await db.managers.albums
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(3),
    );
    expect(
      await db.managers.songs
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(20),
    );
    expect(
      await db.managers.playlists
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(1),
    );
    expect(
      await db.managers.playlistSongs
          .filter((f) => f.sourceId.equals(sourceId))
          .count(),
      equals(7),
    );
  });
}
