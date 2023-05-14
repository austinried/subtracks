import 'package:http/http.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../log.dart';

part 'client.g.dart';

const Map<String, String> subtracksHeaders = {
  'user-agent': 'subtracks/android',
};

class SubtracksHttpClient extends BaseClient {
  SubtracksHttpClient();

  @override
  Future<StreamedResponse> send(BaseRequest request) {
    request.headers.addAll(subtracksHeaders);
    log.info('${request.method} ${request.url}');

    try {
      return request.send();
    } catch (e, st) {
      log.severe('HTTP client: ${request.method} ${request.url}', e, st);
      rethrow;
    }
  }
}

@Riverpod(keepAlive: true)
BaseClient httpClient(HttpClientRef ref) {
  return SubtracksHttpClient();
}
