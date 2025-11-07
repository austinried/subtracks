import 'package:subtracks/sources/subsonic/client.dart';
import 'package:test/test.dart';

import 'http.dart';

enum Servers {
  navidrome,
  gonic,
}

Map<Servers, SubsonicClient> testServerClients() => {
  Servers.navidrome: SubsonicClient(
    http: TestHttpClient(),
    address: Uri.parse('http://localhost:4533/'),
    username: 'admin',
    password: 'password',
  ),
  Servers.gonic: SubsonicClient(
    http: TestHttpClient(),
    address: Uri.parse('http://localhost:4747/'),
    username: 'admin',
    password: 'admin',
  ),
};

void groupByTestServer(void Function(SubsonicClient client) callback) {
  final clients = testServerClients();

  for (final MapEntry(key: server, value: client) in clients.entries) {
    group(server.name, () {
      callback(client);
    });
  }
}
