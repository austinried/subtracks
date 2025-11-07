import 'package:http/http.dart';

class TestHttpClient extends BaseClient {
  @override
  Future<StreamedResponse> send(BaseRequest request) => request.send();
}
