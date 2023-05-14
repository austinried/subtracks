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
    log.info('${request.method} ${_redactUri(request.url)}');
    return request.send();
  }
}

String _redactUri(Uri uri) {
  var redacted = uri.toString();
  redacted = _redactParam(redacted, 'u');
  redacted = _redactParam(redacted, 'p');
  redacted = _redactParam(redacted, 's');
  redacted = _redactParam(redacted, 't');

  return redacted.toString();
}

RegExp _queryReplace(String key) => RegExp('$key=([^&|\\n|\\t\\s]+)');

String _redactParam(String url, String key) =>
    url.replaceFirst(_queryReplace(key), '$key=REDACTED');

@Riverpod(keepAlive: true)
BaseClient httpClient(HttpClientRef ref) {
  return SubtracksHttpClient();
}
