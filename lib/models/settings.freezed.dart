// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'settings.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#custom-getters-and-methods');

/// @nodoc
mixin _$Settings {
  IList<SourceSettings> get sources => throw _privateConstructorUsedError;
  SourceSettings? get activeSource => throw _privateConstructorUsedError;
  AppSettings get app => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $SettingsCopyWith<Settings> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SettingsCopyWith<$Res> {
  factory $SettingsCopyWith(Settings value, $Res Function(Settings) then) =
      _$SettingsCopyWithImpl<$Res, Settings>;
  @useResult
  $Res call(
      {IList<SourceSettings> sources,
      SourceSettings? activeSource,
      AppSettings app});

  $AppSettingsCopyWith<$Res> get app;
}

/// @nodoc
class _$SettingsCopyWithImpl<$Res, $Val extends Settings>
    implements $SettingsCopyWith<$Res> {
  _$SettingsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sources = null,
    Object? activeSource = freezed,
    Object? app = null,
  }) {
    return _then(_value.copyWith(
      sources: null == sources
          ? _value.sources
          : sources // ignore: cast_nullable_to_non_nullable
              as IList<SourceSettings>,
      activeSource: freezed == activeSource
          ? _value.activeSource
          : activeSource // ignore: cast_nullable_to_non_nullable
              as SourceSettings?,
      app: null == app
          ? _value.app
          : app // ignore: cast_nullable_to_non_nullable
              as AppSettings,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $AppSettingsCopyWith<$Res> get app {
    return $AppSettingsCopyWith<$Res>(_value.app, (value) {
      return _then(_value.copyWith(app: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$_SettingsCopyWith<$Res> implements $SettingsCopyWith<$Res> {
  factory _$$_SettingsCopyWith(
          _$_Settings value, $Res Function(_$_Settings) then) =
      __$$_SettingsCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {IList<SourceSettings> sources,
      SourceSettings? activeSource,
      AppSettings app});

  @override
  $AppSettingsCopyWith<$Res> get app;
}

/// @nodoc
class __$$_SettingsCopyWithImpl<$Res>
    extends _$SettingsCopyWithImpl<$Res, _$_Settings>
    implements _$$_SettingsCopyWith<$Res> {
  __$$_SettingsCopyWithImpl(
      _$_Settings _value, $Res Function(_$_Settings) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sources = null,
    Object? activeSource = freezed,
    Object? app = null,
  }) {
    return _then(_$_Settings(
      sources: null == sources
          ? _value.sources
          : sources // ignore: cast_nullable_to_non_nullable
              as IList<SourceSettings>,
      activeSource: freezed == activeSource
          ? _value.activeSource
          : activeSource // ignore: cast_nullable_to_non_nullable
              as SourceSettings?,
      app: null == app
          ? _value.app
          : app // ignore: cast_nullable_to_non_nullable
              as AppSettings,
    ));
  }
}

/// @nodoc

class _$_Settings implements _Settings {
  const _$_Settings(
      {this.sources = const IListConst([]),
      this.activeSource,
      this.app = const AppSettings()});

  @override
  @JsonKey()
  final IList<SourceSettings> sources;
  @override
  final SourceSettings? activeSource;
  @override
  @JsonKey()
  final AppSettings app;

  @override
  String toString() {
    return 'Settings(sources: $sources, activeSource: $activeSource, app: $app)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_Settings &&
            const DeepCollectionEquality().equals(other.sources, sources) &&
            (identical(other.activeSource, activeSource) ||
                other.activeSource == activeSource) &&
            (identical(other.app, app) || other.app == app));
  }

  @override
  int get hashCode => Object.hash(runtimeType,
      const DeepCollectionEquality().hash(sources), activeSource, app);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_SettingsCopyWith<_$_Settings> get copyWith =>
      __$$_SettingsCopyWithImpl<_$_Settings>(this, _$identity);
}

abstract class _Settings implements Settings {
  const factory _Settings(
      {final IList<SourceSettings> sources,
      final SourceSettings? activeSource,
      final AppSettings app}) = _$_Settings;

  @override
  IList<SourceSettings> get sources;
  @override
  SourceSettings? get activeSource;
  @override
  AppSettings get app;
  @override
  @JsonKey(ignore: true)
  _$$_SettingsCopyWith<_$_Settings> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$AppSettings {
  int get maxBitrateWifi => throw _privateConstructorUsedError;
  int get maxBitrateMobile => throw _privateConstructorUsedError;
  String? get streamFormat => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $AppSettingsCopyWith<AppSettings> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AppSettingsCopyWith<$Res> {
  factory $AppSettingsCopyWith(
          AppSettings value, $Res Function(AppSettings) then) =
      _$AppSettingsCopyWithImpl<$Res, AppSettings>;
  @useResult
  $Res call({int maxBitrateWifi, int maxBitrateMobile, String? streamFormat});
}

/// @nodoc
class _$AppSettingsCopyWithImpl<$Res, $Val extends AppSettings>
    implements $AppSettingsCopyWith<$Res> {
  _$AppSettingsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? maxBitrateWifi = null,
    Object? maxBitrateMobile = null,
    Object? streamFormat = freezed,
  }) {
    return _then(_value.copyWith(
      maxBitrateWifi: null == maxBitrateWifi
          ? _value.maxBitrateWifi
          : maxBitrateWifi // ignore: cast_nullable_to_non_nullable
              as int,
      maxBitrateMobile: null == maxBitrateMobile
          ? _value.maxBitrateMobile
          : maxBitrateMobile // ignore: cast_nullable_to_non_nullable
              as int,
      streamFormat: freezed == streamFormat
          ? _value.streamFormat
          : streamFormat // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$_AppSettingsCopyWith<$Res>
    implements $AppSettingsCopyWith<$Res> {
  factory _$$_AppSettingsCopyWith(
          _$_AppSettings value, $Res Function(_$_AppSettings) then) =
      __$$_AppSettingsCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int maxBitrateWifi, int maxBitrateMobile, String? streamFormat});
}

/// @nodoc
class __$$_AppSettingsCopyWithImpl<$Res>
    extends _$AppSettingsCopyWithImpl<$Res, _$_AppSettings>
    implements _$$_AppSettingsCopyWith<$Res> {
  __$$_AppSettingsCopyWithImpl(
      _$_AppSettings _value, $Res Function(_$_AppSettings) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? maxBitrateWifi = null,
    Object? maxBitrateMobile = null,
    Object? streamFormat = freezed,
  }) {
    return _then(_$_AppSettings(
      maxBitrateWifi: null == maxBitrateWifi
          ? _value.maxBitrateWifi
          : maxBitrateWifi // ignore: cast_nullable_to_non_nullable
              as int,
      maxBitrateMobile: null == maxBitrateMobile
          ? _value.maxBitrateMobile
          : maxBitrateMobile // ignore: cast_nullable_to_non_nullable
              as int,
      streamFormat: freezed == streamFormat
          ? _value.streamFormat
          : streamFormat // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc

class _$_AppSettings extends _AppSettings {
  const _$_AppSettings(
      {this.maxBitrateWifi = 0,
      this.maxBitrateMobile = 192,
      this.streamFormat = 'mp3'})
      : super._();

  @override
  @JsonKey()
  final int maxBitrateWifi;
  @override
  @JsonKey()
  final int maxBitrateMobile;
  @override
  @JsonKey()
  final String? streamFormat;

  @override
  String toString() {
    return 'AppSettings(maxBitrateWifi: $maxBitrateWifi, maxBitrateMobile: $maxBitrateMobile, streamFormat: $streamFormat)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_AppSettings &&
            (identical(other.maxBitrateWifi, maxBitrateWifi) ||
                other.maxBitrateWifi == maxBitrateWifi) &&
            (identical(other.maxBitrateMobile, maxBitrateMobile) ||
                other.maxBitrateMobile == maxBitrateMobile) &&
            (identical(other.streamFormat, streamFormat) ||
                other.streamFormat == streamFormat));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, maxBitrateWifi, maxBitrateMobile, streamFormat);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_AppSettingsCopyWith<_$_AppSettings> get copyWith =>
      __$$_AppSettingsCopyWithImpl<_$_AppSettings>(this, _$identity);
}

abstract class _AppSettings extends AppSettings {
  const factory _AppSettings(
      {final int maxBitrateWifi,
      final int maxBitrateMobile,
      final String? streamFormat}) = _$_AppSettings;
  const _AppSettings._() : super._();

  @override
  int get maxBitrateWifi;
  @override
  int get maxBitrateMobile;
  @override
  String? get streamFormat;
  @override
  @JsonKey(ignore: true)
  _$$_AppSettingsCopyWith<_$_AppSettings> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$SubsonicSettings {
  int get id => throw _privateConstructorUsedError;
  IList<SubsonicFeature> get features => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  Uri get address => throw _privateConstructorUsedError;
  bool? get isActive => throw _privateConstructorUsedError;
  DateTime get createdAt => throw _privateConstructorUsedError;
  String get username => throw _privateConstructorUsedError;
  String get password => throw _privateConstructorUsedError;
  bool get useTokenAuth => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $SubsonicSettingsCopyWith<SubsonicSettings> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SubsonicSettingsCopyWith<$Res> {
  factory $SubsonicSettingsCopyWith(
          SubsonicSettings value, $Res Function(SubsonicSettings) then) =
      _$SubsonicSettingsCopyWithImpl<$Res, SubsonicSettings>;
  @useResult
  $Res call(
      {int id,
      IList<SubsonicFeature> features,
      String name,
      Uri address,
      bool? isActive,
      DateTime createdAt,
      String username,
      String password,
      bool useTokenAuth});
}

/// @nodoc
class _$SubsonicSettingsCopyWithImpl<$Res, $Val extends SubsonicSettings>
    implements $SubsonicSettingsCopyWith<$Res> {
  _$SubsonicSettingsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? features = null,
    Object? name = null,
    Object? address = null,
    Object? isActive = freezed,
    Object? createdAt = null,
    Object? username = null,
    Object? password = null,
    Object? useTokenAuth = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      features: null == features
          ? _value.features
          : features // ignore: cast_nullable_to_non_nullable
              as IList<SubsonicFeature>,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      address: null == address
          ? _value.address
          : address // ignore: cast_nullable_to_non_nullable
              as Uri,
      isActive: freezed == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool?,
      createdAt: null == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      username: null == username
          ? _value.username
          : username // ignore: cast_nullable_to_non_nullable
              as String,
      password: null == password
          ? _value.password
          : password // ignore: cast_nullable_to_non_nullable
              as String,
      useTokenAuth: null == useTokenAuth
          ? _value.useTokenAuth
          : useTokenAuth // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$_SubsonicSettingsCopyWith<$Res>
    implements $SubsonicSettingsCopyWith<$Res> {
  factory _$$_SubsonicSettingsCopyWith(
          _$_SubsonicSettings value, $Res Function(_$_SubsonicSettings) then) =
      __$$_SubsonicSettingsCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int id,
      IList<SubsonicFeature> features,
      String name,
      Uri address,
      bool? isActive,
      DateTime createdAt,
      String username,
      String password,
      bool useTokenAuth});
}

/// @nodoc
class __$$_SubsonicSettingsCopyWithImpl<$Res>
    extends _$SubsonicSettingsCopyWithImpl<$Res, _$_SubsonicSettings>
    implements _$$_SubsonicSettingsCopyWith<$Res> {
  __$$_SubsonicSettingsCopyWithImpl(
      _$_SubsonicSettings _value, $Res Function(_$_SubsonicSettings) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? features = null,
    Object? name = null,
    Object? address = null,
    Object? isActive = freezed,
    Object? createdAt = null,
    Object? username = null,
    Object? password = null,
    Object? useTokenAuth = null,
  }) {
    return _then(_$_SubsonicSettings(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      features: null == features
          ? _value.features
          : features // ignore: cast_nullable_to_non_nullable
              as IList<SubsonicFeature>,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      address: null == address
          ? _value.address
          : address // ignore: cast_nullable_to_non_nullable
              as Uri,
      isActive: freezed == isActive
          ? _value.isActive
          : isActive // ignore: cast_nullable_to_non_nullable
              as bool?,
      createdAt: null == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      username: null == username
          ? _value.username
          : username // ignore: cast_nullable_to_non_nullable
              as String,
      password: null == password
          ? _value.password
          : password // ignore: cast_nullable_to_non_nullable
              as String,
      useTokenAuth: null == useTokenAuth
          ? _value.useTokenAuth
          : useTokenAuth // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$_SubsonicSettings extends _SubsonicSettings {
  const _$_SubsonicSettings(
      {required this.id,
      this.features = const IListConst([]),
      required this.name,
      required this.address,
      required this.isActive,
      required this.createdAt,
      required this.username,
      required this.password,
      this.useTokenAuth = true})
      : super._();

  @override
  final int id;
  @override
  @JsonKey()
  final IList<SubsonicFeature> features;
  @override
  final String name;
  @override
  final Uri address;
  @override
  final bool? isActive;
  @override
  final DateTime createdAt;
  @override
  final String username;
  @override
  final String password;
  @override
  @JsonKey()
  final bool useTokenAuth;

  @override
  String toString() {
    return 'SubsonicSettings(id: $id, features: $features, name: $name, address: $address, isActive: $isActive, createdAt: $createdAt, username: $username, password: $password, useTokenAuth: $useTokenAuth)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_SubsonicSettings &&
            (identical(other.id, id) || other.id == id) &&
            const DeepCollectionEquality().equals(other.features, features) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.address, address) || other.address == address) &&
            (identical(other.isActive, isActive) ||
                other.isActive == isActive) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.username, username) ||
                other.username == username) &&
            (identical(other.password, password) ||
                other.password == password) &&
            (identical(other.useTokenAuth, useTokenAuth) ||
                other.useTokenAuth == useTokenAuth));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      const DeepCollectionEquality().hash(features),
      name,
      address,
      isActive,
      createdAt,
      username,
      password,
      useTokenAuth);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_SubsonicSettingsCopyWith<_$_SubsonicSettings> get copyWith =>
      __$$_SubsonicSettingsCopyWithImpl<_$_SubsonicSettings>(this, _$identity);
}

abstract class _SubsonicSettings extends SubsonicSettings {
  const factory _SubsonicSettings(
      {required final int id,
      final IList<SubsonicFeature> features,
      required final String name,
      required final Uri address,
      required final bool? isActive,
      required final DateTime createdAt,
      required final String username,
      required final String password,
      final bool useTokenAuth}) = _$_SubsonicSettings;
  const _SubsonicSettings._() : super._();

  @override
  int get id;
  @override
  IList<SubsonicFeature> get features;
  @override
  String get name;
  @override
  Uri get address;
  @override
  bool? get isActive;
  @override
  DateTime get createdAt;
  @override
  String get username;
  @override
  String get password;
  @override
  bool get useTokenAuth;
  @override
  @JsonKey(ignore: true)
  _$$_SubsonicSettingsCopyWith<_$_SubsonicSettings> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$SubsonicSourceSettings {
  SourceSettings get source => throw _privateConstructorUsedError;
  SubsonicSettings get subsonic => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $SubsonicSourceSettingsCopyWith<SubsonicSourceSettings> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SubsonicSourceSettingsCopyWith<$Res> {
  factory $SubsonicSourceSettingsCopyWith(SubsonicSourceSettings value,
          $Res Function(SubsonicSourceSettings) then) =
      _$SubsonicSourceSettingsCopyWithImpl<$Res, SubsonicSourceSettings>;
  @useResult
  $Res call({SourceSettings source, SubsonicSettings subsonic});

  $SubsonicSettingsCopyWith<$Res> get subsonic;
}

/// @nodoc
class _$SubsonicSourceSettingsCopyWithImpl<$Res,
        $Val extends SubsonicSourceSettings>
    implements $SubsonicSourceSettingsCopyWith<$Res> {
  _$SubsonicSourceSettingsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? source = null,
    Object? subsonic = null,
  }) {
    return _then(_value.copyWith(
      source: null == source
          ? _value.source
          : source // ignore: cast_nullable_to_non_nullable
              as SourceSettings,
      subsonic: null == subsonic
          ? _value.subsonic
          : subsonic // ignore: cast_nullable_to_non_nullable
              as SubsonicSettings,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $SubsonicSettingsCopyWith<$Res> get subsonic {
    return $SubsonicSettingsCopyWith<$Res>(_value.subsonic, (value) {
      return _then(_value.copyWith(subsonic: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$_SubsonicSourceSettingsCopyWith<$Res>
    implements $SubsonicSourceSettingsCopyWith<$Res> {
  factory _$$_SubsonicSourceSettingsCopyWith(_$_SubsonicSourceSettings value,
          $Res Function(_$_SubsonicSourceSettings) then) =
      __$$_SubsonicSourceSettingsCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({SourceSettings source, SubsonicSettings subsonic});

  @override
  $SubsonicSettingsCopyWith<$Res> get subsonic;
}

/// @nodoc
class __$$_SubsonicSourceSettingsCopyWithImpl<$Res>
    extends _$SubsonicSourceSettingsCopyWithImpl<$Res,
        _$_SubsonicSourceSettings>
    implements _$$_SubsonicSourceSettingsCopyWith<$Res> {
  __$$_SubsonicSourceSettingsCopyWithImpl(_$_SubsonicSourceSettings _value,
      $Res Function(_$_SubsonicSourceSettings) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? source = null,
    Object? subsonic = null,
  }) {
    return _then(_$_SubsonicSourceSettings(
      source: null == source
          ? _value.source
          : source // ignore: cast_nullable_to_non_nullable
              as SourceSettings,
      subsonic: null == subsonic
          ? _value.subsonic
          : subsonic // ignore: cast_nullable_to_non_nullable
              as SubsonicSettings,
    ));
  }
}

/// @nodoc

class _$_SubsonicSourceSettings extends _SubsonicSourceSettings {
  const _$_SubsonicSourceSettings(
      {required this.source, required this.subsonic})
      : super._();

  @override
  final SourceSettings source;
  @override
  final SubsonicSettings subsonic;

  @override
  String toString() {
    return 'SubsonicSourceSettings(source: $source, subsonic: $subsonic)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_SubsonicSourceSettings &&
            (identical(other.source, source) || other.source == source) &&
            (identical(other.subsonic, subsonic) ||
                other.subsonic == subsonic));
  }

  @override
  int get hashCode => Object.hash(runtimeType, source, subsonic);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_SubsonicSourceSettingsCopyWith<_$_SubsonicSourceSettings> get copyWith =>
      __$$_SubsonicSourceSettingsCopyWithImpl<_$_SubsonicSourceSettings>(
          this, _$identity);
}

abstract class _SubsonicSourceSettings extends SubsonicSourceSettings {
  const factory _SubsonicSourceSettings(
      {required final SourceSettings source,
      required final SubsonicSettings subsonic}) = _$_SubsonicSourceSettings;
  const _SubsonicSourceSettings._() : super._();

  @override
  SourceSettings get source;
  @override
  SubsonicSettings get subsonic;
  @override
  @JsonKey(ignore: true)
  _$$_SubsonicSourceSettingsCopyWith<_$_SubsonicSourceSettings> get copyWith =>
      throw _privateConstructorUsedError;
}
