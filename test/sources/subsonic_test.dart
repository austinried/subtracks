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
    });
  }
}
