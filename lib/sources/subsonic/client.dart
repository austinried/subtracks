import 'dart:convert';
import 'dart:math';

import 'package:crypto/crypto.dart';
import 'package:http/http.dart';
import 'package:xml/xml.dart';

import 'xml.dart';

class SubsonicClient {
  SubsonicClient({
    required this.http,
    required this.address,
    required this.username,
    required this.password,
    this.useTokenAuth = true,
  });

  final BaseClient http;
  final Uri address;
  final String username;
  final String password;
  final bool useTokenAuth;

  Uri uri(
    String method, [
    Map<String, String?>? extraParams,
  ]) {
    final pathSegments = [...address.pathSegments, 'rest', '$method.view'];

    final queryParameters = {
      ..._params(),
      ...(extraParams ?? {}),
    }..removeWhere((_, value) => value == null);

    return Uri(
      scheme: address.scheme,
      host: address.host,
      port: address.hasPort ? address.port : null,
      pathSegments: pathSegments,
      queryParameters: queryParameters,
    );
  }

  Future<SubsonicResponse> get(
    String method, [
    Map<String, String?>? extraParams,
  ]) async {
    final res = await http.get(uri(method, extraParams));

    final subsonicResponse = SubsonicResponse(
      XmlDocument.parse(utf8.decode(res.bodyBytes)),
    );

    if (subsonicResponse.status == Status.failed) {
      final error = SubsonicException(subsonicResponse.xml);
      throw error;
    }

    return subsonicResponse;
  }

  String _salt() {
    final r = Random();
    return String.fromCharCodes(
      List.generate(4, (index) => r.nextInt(92) + 33),
    );
  }

  Map<String, String> _params() {
    final Map<String, String> p = {};
    p['v'] = '1.13.0';
    p['c'] = 'subtracks';
    p['u'] = username;

    if (useTokenAuth) {
      p['s'] = _salt();
      p['t'] = md5.convert(utf8.encode(password + p['s']!)).toString();
    } else {
      p['p'] = password;
    }

    return p;
  }
}
