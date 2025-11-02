import 'package:collection/collection.dart';
import 'package:http/http.dart';
import 'package:subtracks/sources/subsonic/client.dart';
import 'package:subtracks/sources/subsonic/source.dart';
import 'package:test/test.dart';

class TestHttpClient extends BaseClient {
  @override
  Future<StreamedResponse> send(BaseRequest request) => request.send();
}

class Server {
  Server({
    required this.name,
    required this.client,
  });

  final String name;
  final SubsonicClient client;
}

void main() {
  late SubsonicSource source;

  final clients = [
    Server(
      name: 'navidrome',
      client: SubsonicClient(
        http: TestHttpClient(),
        address: Uri.parse('http://localhost:4533/'),
        username: 'admin',
        password: 'password',
      ),
    ),
    Server(
      name: 'gonic',
      client: SubsonicClient(
        http: TestHttpClient(),
        address: Uri.parse('http://localhost:4747/'),
        username: 'admin',
        password: 'admin',
      ),
    ),
  ];

  for (final Server(:name, :client) in clients) {
    group(name, () {
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
        expect(kosmo.starred, isNull);
        expect(kosmo.genre, equals('Electronic'));

        final retro = items.firstWhere(
          (a) => a.name == 'Retroconnaissance EP',
        );
        final dunno = items.firstWhere(
          (a) => a.name == "I Don't Know What I'm Doing",
        );

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
      });

      test('allSongs', () async {
        final items = await source.allSongs().toList();

        expect(items.length, equals(20));
      });

      test('allPlaylists', () async {
        final items = await source.allPlaylists().toList();

        expect(items.length, equals(0));
      });

      test('allPlaylistSongs', () async {
        final items = await source.allPlaylistSongs().toList();

        expect(items.length, equals(0));
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
}
