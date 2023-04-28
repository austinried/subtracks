import 'package:drift/drift.dart' show Value;
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../database/database.dart';

part 'settings.freezed.dart';

@freezed
class Settings with _$Settings {
  const factory Settings({
    @Default(IListConst([])) IList<SourceSettings> sources,
    SourceSettings? activeSource,
    @Default(AppSettings()) AppSettings app,
  }) = _Settings;
}

@freezed
class AppSettings with _$AppSettings {
  const AppSettings._();

  const factory AppSettings({
    @Default(0) int maxBitrateWifi,
    @Default(192) int maxBitrateMobile,
    @Default('mp3') String? streamFormat,
  }) = _AppSettings;

  AppSettingsCompanion toCompanion() {
    return AppSettingsCompanion.insert(
      id: const Value(1),
      maxBitrateWifi: maxBitrateWifi,
      maxBitrateMobile: maxBitrateMobile,
      streamFormat: Value(streamFormat),
    );
  }
}

class ParentChild<T> {
  final T parent;
  final T child;

  ParentChild(this.parent, this.child);
}

abstract class SourceSettings {
  const SourceSettings();

  int get id;
  String get name;
  Uri get address;
  bool? get isActive;
  DateTime get createdAt;
}

enum SubsonicFeature {
  emptyQuerySearch('emptyQuerySearch');

  const SubsonicFeature(this.value);
  final String value;

  @override
  String toString() => value;
}

@freezed
class SubsonicSettings with _$SubsonicSettings implements SourceSettings {
  const SubsonicSettings._();

  const factory SubsonicSettings({
    required int id,
    @Default(IListConst([])) IList<SubsonicFeature> features,
    required String name,
    required Uri address,
    required bool? isActive,
    required DateTime createdAt,
    required String username,
    required String password,
    @Default(true) bool useTokenAuth,
  }) = _SubsonicSettings;

  SourcesCompanion toSourceInsertable() {
    return SourcesCompanion(
      id: Value(id),
      name: Value(name),
      address: Value(address),
      createdAt: Value(createdAt),
    );
  }

  SubsonicSourcesCompanion toSubsonicInsertable() {
    return SubsonicSourcesCompanion(
      sourceId: Value(id),
      features: Value(features),
      username: Value(username),
      password: Value(password),
      useTokenAuth: Value(useTokenAuth),
    );
  }
}

@freezed
class SubsonicSourceSettings with _$SubsonicSourceSettings {
  const SubsonicSourceSettings._();

  const factory SubsonicSourceSettings({
    required SourceSettings source,
    required SubsonicSettings subsonic,
  }) = _SubsonicSourceSettings;
}

enum NetworkMode {
  mobile('mobile'),
  wifi('wifi');

  const NetworkMode(this.value);
  final String value;

  @override
  String toString() => value;
}
