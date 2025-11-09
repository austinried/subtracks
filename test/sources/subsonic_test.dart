import 'package:collection/collection.dart';
import 'package:subtracks/sources/subsonic/source.dart';
import 'package:test/test.dart';

import '../util/subsonic.dart';

void main() {
  groupByTestServer((server, client) {
    late SubsonicSource source;

    setUp(() async {
      source = SubsonicSource(client);
    });

    test('ping', () async {
      await source.ping();
    });

    test('allAlbums', () async {
      final items = await source.allAlbums().toList();

      expect(items.length, equals(3));

      final kosmo = items.firstWhere((a) => a.name == 'Kosmonaut');

      expect(kosmo.id.length, greaterThan(0));
      expect(kosmo.artistId?.length, greaterThan(0));
      expect(kosmo.albumArtist, equals('Ugress'));
      expect(kosmo.created.compareTo(DateTime.now()), lessThan(0));
      expect(kosmo.coverArt?.length, greaterThan(0));
      expect(kosmo.year, equals(2006));
      expect(kosmo.starred?.compareTo(DateTime.now()), lessThan(0));
      expect(kosmo.genre, equals('Electronic'));

      final retro = items.firstWhere(
        (a) => a.name == 'Retroconnaissance EP',
      );
      final dunno = items.firstWhere(
        (a) => a.name == "I Don't Know What I'm Doing",
      );

      expect(retro.starred, isNull);
      expect(dunno.starred, isNull);

      expect(kosmo.recentRank, equals(0));
      expect(kosmo.frequentRank, equals(1));

      expect(retro.recentRank, equals(1));
      expect(retro.frequentRank, equals(0));

      expect(dunno.recentRank, isNull);
      expect(dunno.frequentRank, isNull);
    });

    test('allArtists', () async {
      final items = await source.allArtists().toList();

      expect(items.length, equals(2));

      final brad = items.firstWhere((a) => a.name == 'Brad Sucks');

      expect(brad.id.length, greaterThan(0));
      expect(brad.starred, isNull);

      if (![Servers.gonic].contains(server)) {
        expect(brad.coverArt?.length, greaterThan(0));
      }

      final ugress = items.firstWhere((a) => a.name == 'Ugress');

      expect(ugress.starred?.compareTo(DateTime.now()), lessThan(0));
    });

    test('allSongs', () async {
      final items = await source.allSongs().toList();

      expect(items.length, equals(20));
    });

    test('allPlaylists', () async {
      final items = await source.allPlaylists().toList();

      expect(items.length, equals(1));
    });

    test('allPlaylistSongs', () async {
      final items = await source.allPlaylistSongs().toList();

      expect(items.length, equals(7));
    });

    test('album-artist relation', () async {
      final artists = await source.allArtists().toList();
      final albums = await source.allAlbums().toList();

      final artistAlbums = artists
          .map(
            (artist) => [
              artist.name,
              ...albums
                  .where((album) => album.artistId == artist.id)
                  .map((album) => album.name)
                  .sorted(),
            ],
          )
          .sorted((a, b) => (a[0]).compareTo(b[0]));

      expect(artistAlbums.length, equals(2));
      expect(
        artistAlbums,
        equals([
          [
            'Brad Sucks',
            "I Don't Know What I'm Doing",
          ],
          [
            'Ugress',
            'Kosmonaut',
            'Retroconnaissance EP',
          ],
        ]),
      );
    });
  });
}
