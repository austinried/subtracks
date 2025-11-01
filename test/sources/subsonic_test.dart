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
  ];

  for (final Server(:name, :client) in clients) {
    group(name, () {
      setUp(() async {
        source = SubsonicSource(client: client, maxBitrate: 196);
      });

      test('ping', () async {
        await source.ping();
      });

      test('allAlbums', () async {
        final items = (await source.allAlbums().toList()).flattened.toList();

        expect(items.length, equals(3));
      });

      test('allArtists', () async {
        final items = (await source.allArtists().toList()).flattened.toList();

        expect(items.length, equals(2));
      });

      test('allSongs', () async {
        final items = (await source.allSongs().toList()).flattened.toList();

        expect(items.length, equals(20));
      });
    });
  }
}
