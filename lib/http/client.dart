import 'package:http/http.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'client.g.dart';

const Map<String, String> subtracksHeaders = {
  'user-agent': 'subtracks/android',
};

class SubtracksHttpClient extends BaseClient {
  SubtracksHttpClient();

  @override
  Future<StreamedResponse> send(BaseRequest request) {
    request.headers.addAll(subtracksHeaders);
    print('${request.method} ${request.url}');
    return request.send();
  }
}

@Riverpod(keepAlive: true)
BaseClient httpClient(HttpClientRef ref) {
  return SubtracksHttpClient();
}
