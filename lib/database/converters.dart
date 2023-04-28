import 'dart:convert';

import 'package:drift/drift.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';

import '../models/query.dart';
import '../models/settings.dart';

class DurationSecondsConverter extends TypeConverter<Duration, int> {
  const DurationSecondsConverter();

  @override
  Duration fromSql(int fromDb) => Duration(seconds: fromDb);

  @override
  int toSql(Duration value) => value.inSeconds;
}

class UriConverter extends TypeConverter<Uri, String> {
  const UriConverter();

  @override
  Uri fromSql(String fromDb) => Uri.parse(fromDb);

  @override
  String toSql(Uri value) => value.toString();
}

class ListQueryConverter extends TypeConverter<ListQuery, String> {
  const ListQueryConverter();

  @override
  ListQuery fromSql(String fromDb) => ListQuery.fromJson(jsonDecode(fromDb));

  @override
  String toSql(ListQuery value) => jsonEncode(value.toJson());
}

class SubsonicFeatureListConverter
    extends TypeConverter<IList<SubsonicFeature>, String> {
  const SubsonicFeatureListConverter();

  @override
  IList<SubsonicFeature> fromSql(String fromDb) {
    return IList<SubsonicFeature>.fromJson(
      jsonDecode(fromDb),
      (item) => SubsonicFeature.values.byName(item as String),
    );
  }

  @override
  String toSql(IList<SubsonicFeature> value) {
    return jsonEncode(value.toJson((e) => e.toString()));
  }
}

class IListIntConverter extends TypeConverter<IList<int>, String> {
  const IListIntConverter();

  @override
  IList<int> fromSql(String fromDb) {
    return IList<int>.fromJson(
      jsonDecode(fromDb),
      (item) => int.parse(item as String),
    );
  }

  @override
  String toSql(IList<int> value) {
    return jsonEncode(value.toJson((e) => jsonEncode(e)));
  }
}
