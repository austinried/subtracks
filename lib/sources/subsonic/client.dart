import 'dart:convert';
import 'dart:math';

import 'package:collection/collection.dart';
import 'package:crypto/crypto.dart';
import 'package:http/http.dart';
import 'package:xml/xml.dart';

import '../../models/settings.dart';
import 'xml.dart';

class SubsonicException implements Exception {
  final XmlElement xml;

  late int code;
  late String message;

  SubsonicException(this.xml) {
    try {
      final error = xml.getElement('error')!;
      code = int.parse(error.getAttribute('code')!);
      message = error.getAttribute('message')!;
    } catch (err) {
      code = -1;
      message = 'Unknown error.';
    }
  }

  @override
  String toString() => 'SubsonicException [$code]: $message';
}

class SubsonicClient {
  final SubsonicSettings opt;
  final BaseClient http;

  SubsonicClient(this.opt, this.http);

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
    p['u'] = opt.username;

    if (opt.useTokenAuth) {
      p['s'] = _salt();
      p['t'] = md5.convert(utf8.encode(opt.password + p['s']!)).toString();
    } else {
      p['p'] = opt.password;
    }

    return p;
  }

  Uri uri(
    String method, [
    Map<String, String?>? extraParams,
  ]) {
    final pathSegments = [...opt.address.pathSegments, 'rest', '$method.view'];

    _removeIdPrefix(extraParams);
    extraParams?.removeWhere((key, value) => value == null);
    final queryParameters = {
      ..._params(),
      ...(extraParams ?? {}),
    };

    return Uri(
      scheme: opt.address.scheme,
      host: opt.address.host,
      port: opt.address.hasPort ? opt.address.port : null,
      pathSegments: pathSegments,
      queryParameters: queryParameters,
    );
  }

  Future<SubsonicResponse> get(
    String method, [
    Map<String, String?>? extraParams,
  ]) async {
    final res = await http.get(uri(method, extraParams));
    final subsonicResponse =
        SubsonicResponse(XmlDocument.parse(utf8.decode(res.bodyBytes)));
    if (subsonicResponse.status == Status.failed) {
      throw SubsonicException(subsonicResponse.xml);
    }

    return subsonicResponse;
  }

  Future<bool> testFeature(SubsonicFeature feature) async {
    switch (feature) {
      case SubsonicFeature.emptyQuerySearch:
        final res = await get(
          'search3',
          {'query': '""', 'songCount': '1'},
        );
        return res.xml.findAllElements('song').isNotEmpty;
      default:
        return false;
    }
  }

  static const _idsWithPrefix = {
    'id',
    'playlistId',
    'songIdToAdd',
    'albumId',
    'artistId',
  };

  static const _idPrefixMatch =
      r'(artist\.|album\.|playlist\.|song\.|coverArt\.)';

  void _removeIdPrefix(Map<String, String?>? params) {
    if (params == null) return;

    for (var key in params.keys) {
      if (!_idsWithPrefix.contains(key)) continue;
      if (params[key] == null) continue;

      final hasPrefix = params[key]!.startsWith(RegExp(_idPrefixMatch));
      if (!hasPrefix) continue;

      params[key] = params[key]?.split('.').slice(1).join('');
    }
  }
}
