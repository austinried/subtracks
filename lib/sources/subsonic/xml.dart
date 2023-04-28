import 'package:xml/xml.dart';

enum Status {
  ok('ok'),
  failed('failed');

  const Status(this.value);
  final String value;
}

class SubsonicResponse {
  late Status status;
  late XmlElement xml;

  SubsonicResponse(XmlDocument xml) {
    this.xml = xml.getElement('subsonic-response')!;
    status = Status.values.byName(this.xml.getAttribute('status')!);
  }
}
