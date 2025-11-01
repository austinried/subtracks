import 'package:xml/xml.dart';

enum Status {
  ok('ok'),
  failed('failed');

  const Status(this.value);
  final String value;
}

class SubsonicResponse {
  SubsonicResponse(XmlDocument xml) {
    this.xml = xml.getElement('subsonic-response')!;
    status = Status.values.byName(this.xml.getAttribute('status')!);
  }

  late Status status;
  late XmlElement xml;
}

class SubsonicException implements Exception {
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

  final XmlElement xml;

  late int code;
  late String message;

  @override
  String toString() => 'SubsonicException [$code]: $message';
}
