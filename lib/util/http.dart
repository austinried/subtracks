import 'package:http/http.dart';

class SubtracksHttpClient extends BaseClient {
  @override
  Future<StreamedResponse> send(BaseRequest request) => request.send();
}
