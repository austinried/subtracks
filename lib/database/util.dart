import 'package:intl/intl.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

Future<String> datapasePath(String database) async =>
    join((await getApplicationSupportDirectory()).path, 'databases', database);

extension DateTimeExt on DateTime {
  String toDb() => DateFormat('yyyy-MM-dd hh:mm:ss').format(toUtc());

  static DateTime parseUtc(Object? obj) {
    final str = obj.toString();
    return DateTime.parse(hasTimeZone(str) ? str : '${obj}Z').toLocal();
  }

  static DateTime? tryParseUtc(Object? obj) {
    final str = obj.toString();
    return DateTime.tryParse(hasTimeZone(str) ? str : '${obj}Z')?.toLocal();
  }

  static bool hasTimeZone(String str) =>
      RegExp(r'(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])').hasMatch(str);
}
