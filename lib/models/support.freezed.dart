// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'support.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$UriCacheInfo {
  Uri get uri => throw _privateConstructorUsedError;
  String get cacheKey => throw _privateConstructorUsedError;
  CacheManager get cacheManager => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $UriCacheInfoCopyWith<UriCacheInfo> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $UriCacheInfoCopyWith<$Res> {
  factory $UriCacheInfoCopyWith(
          UriCacheInfo value, $Res Function(UriCacheInfo) then) =
      _$UriCacheInfoCopyWithImpl<$Res, UriCacheInfo>;
  @useResult
  $Res call({Uri uri, String cacheKey, CacheManager cacheManager});
}

/// @nodoc
class _$UriCacheInfoCopyWithImpl<$Res, $Val extends UriCacheInfo>
    implements $UriCacheInfoCopyWith<$Res> {
  _$UriCacheInfoCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? uri = null,
    Object? cacheKey = null,
    Object? cacheManager = null,
  }) {
    return _then(_value.copyWith(
      uri: null == uri
          ? _value.uri
          : uri // ignore: cast_nullable_to_non_nullable
              as Uri,
      cacheKey: null == cacheKey
          ? _value.cacheKey
          : cacheKey // ignore: cast_nullable_to_non_nullable
              as String,
      cacheManager: null == cacheManager
          ? _value.cacheManager
          : cacheManager // ignore: cast_nullable_to_non_nullable
              as CacheManager,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$UriCacheInfoImplCopyWith<$Res>
    implements $UriCacheInfoCopyWith<$Res> {
  factory _$$UriCacheInfoImplCopyWith(
          _$UriCacheInfoImpl value, $Res Function(_$UriCacheInfoImpl) then) =
      __$$UriCacheInfoImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({Uri uri, String cacheKey, CacheManager cacheManager});
}

/// @nodoc
class __$$UriCacheInfoImplCopyWithImpl<$Res>
    extends _$UriCacheInfoCopyWithImpl<$Res, _$UriCacheInfoImpl>
    implements _$$UriCacheInfoImplCopyWith<$Res> {
  __$$UriCacheInfoImplCopyWithImpl(
      _$UriCacheInfoImpl _value, $Res Function(_$UriCacheInfoImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? uri = null,
    Object? cacheKey = null,
    Object? cacheManager = null,
  }) {
    return _then(_$UriCacheInfoImpl(
      uri: null == uri
          ? _value.uri
          : uri // ignore: cast_nullable_to_non_nullable
              as Uri,
      cacheKey: null == cacheKey
          ? _value.cacheKey
          : cacheKey // ignore: cast_nullable_to_non_nullable
              as String,
      cacheManager: null == cacheManager
          ? _value.cacheManager
          : cacheManager // ignore: cast_nullable_to_non_nullable
              as CacheManager,
    ));
  }
}

/// @nodoc

class _$UriCacheInfoImpl implements _UriCacheInfo {
  const _$UriCacheInfoImpl(
      {required this.uri, required this.cacheKey, required this.cacheManager});

  @override
  final Uri uri;
  @override
  final String cacheKey;
  @override
  final CacheManager cacheManager;

  @override
  String toString() {
    return 'UriCacheInfo(uri: $uri, cacheKey: $cacheKey, cacheManager: $cacheManager)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UriCacheInfoImpl &&
            (identical(other.uri, uri) || other.uri == uri) &&
            (identical(other.cacheKey, cacheKey) ||
                other.cacheKey == cacheKey) &&
            (identical(other.cacheManager, cacheManager) ||
                other.cacheManager == cacheManager));
  }

  @override
  int get hashCode => Object.hash(runtimeType, uri, cacheKey, cacheManager);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$UriCacheInfoImplCopyWith<_$UriCacheInfoImpl> get copyWith =>
      __$$UriCacheInfoImplCopyWithImpl<_$UriCacheInfoImpl>(this, _$identity);
}

abstract class _UriCacheInfo implements UriCacheInfo {
  const factory _UriCacheInfo(
      {required final Uri uri,
      required final String cacheKey,
      required final CacheManager cacheManager}) = _$UriCacheInfoImpl;

  @override
  Uri get uri;
  @override
  String get cacheKey;
  @override
  CacheManager get cacheManager;
  @override
  @JsonKey(ignore: true)
  _$$UriCacheInfoImplCopyWith<_$UriCacheInfoImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$CacheInfo {
  String get cacheKey => throw _privateConstructorUsedError;
  CacheManager get cacheManager => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $CacheInfoCopyWith<CacheInfo> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CacheInfoCopyWith<$Res> {
  factory $CacheInfoCopyWith(CacheInfo value, $Res Function(CacheInfo) then) =
      _$CacheInfoCopyWithImpl<$Res, CacheInfo>;
  @useResult
  $Res call({String cacheKey, CacheManager cacheManager});
}

/// @nodoc
class _$CacheInfoCopyWithImpl<$Res, $Val extends CacheInfo>
    implements $CacheInfoCopyWith<$Res> {
  _$CacheInfoCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? cacheKey = null,
    Object? cacheManager = null,
  }) {
    return _then(_value.copyWith(
      cacheKey: null == cacheKey
          ? _value.cacheKey
          : cacheKey // ignore: cast_nullable_to_non_nullable
              as String,
      cacheManager: null == cacheManager
          ? _value.cacheManager
          : cacheManager // ignore: cast_nullable_to_non_nullable
              as CacheManager,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$CacheInfoImplCopyWith<$Res>
    implements $CacheInfoCopyWith<$Res> {
  factory _$$CacheInfoImplCopyWith(
          _$CacheInfoImpl value, $Res Function(_$CacheInfoImpl) then) =
      __$$CacheInfoImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String cacheKey, CacheManager cacheManager});
}

/// @nodoc
class __$$CacheInfoImplCopyWithImpl<$Res>
    extends _$CacheInfoCopyWithImpl<$Res, _$CacheInfoImpl>
    implements _$$CacheInfoImplCopyWith<$Res> {
  __$$CacheInfoImplCopyWithImpl(
      _$CacheInfoImpl _value, $Res Function(_$CacheInfoImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? cacheKey = null,
    Object? cacheManager = null,
  }) {
    return _then(_$CacheInfoImpl(
      cacheKey: null == cacheKey
          ? _value.cacheKey
          : cacheKey // ignore: cast_nullable_to_non_nullable
              as String,
      cacheManager: null == cacheManager
          ? _value.cacheManager
          : cacheManager // ignore: cast_nullable_to_non_nullable
              as CacheManager,
    ));
  }
}

/// @nodoc

class _$CacheInfoImpl implements _CacheInfo {
  const _$CacheInfoImpl({required this.cacheKey, required this.cacheManager});

  @override
  final String cacheKey;
  @override
  final CacheManager cacheManager;

  @override
  String toString() {
    return 'CacheInfo(cacheKey: $cacheKey, cacheManager: $cacheManager)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CacheInfoImpl &&
            (identical(other.cacheKey, cacheKey) ||
                other.cacheKey == cacheKey) &&
            (identical(other.cacheManager, cacheManager) ||
                other.cacheManager == cacheManager));
  }

  @override
  int get hashCode => Object.hash(runtimeType, cacheKey, cacheManager);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$CacheInfoImplCopyWith<_$CacheInfoImpl> get copyWith =>
      __$$CacheInfoImplCopyWithImpl<_$CacheInfoImpl>(this, _$identity);
}

abstract class _CacheInfo implements CacheInfo {
  const factory _CacheInfo(
      {required final String cacheKey,
      required final CacheManager cacheManager}) = _$CacheInfoImpl;

  @override
  String get cacheKey;
  @override
  CacheManager get cacheManager;
  @override
  @JsonKey(ignore: true)
  _$$CacheInfoImplCopyWith<_$CacheInfoImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$Palette {
  PaletteColor? get vibrantColor => throw _privateConstructorUsedError;
  PaletteColor? get dominantColor => throw _privateConstructorUsedError;
  PaletteColor? get mutedColor => throw _privateConstructorUsedError;
  PaletteColor? get darkMutedColor => throw _privateConstructorUsedError;
  PaletteColor? get darkVibrantColor => throw _privateConstructorUsedError;
  PaletteColor? get lightMutedColor => throw _privateConstructorUsedError;
  PaletteColor? get lightVibrantColor => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $PaletteCopyWith<Palette> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $PaletteCopyWith<$Res> {
  factory $PaletteCopyWith(Palette value, $Res Function(Palette) then) =
      _$PaletteCopyWithImpl<$Res, Palette>;
  @useResult
  $Res call(
      {PaletteColor? vibrantColor,
      PaletteColor? dominantColor,
      PaletteColor? mutedColor,
      PaletteColor? darkMutedColor,
      PaletteColor? darkVibrantColor,
      PaletteColor? lightMutedColor,
      PaletteColor? lightVibrantColor});
}

/// @nodoc
class _$PaletteCopyWithImpl<$Res, $Val extends Palette>
    implements $PaletteCopyWith<$Res> {
  _$PaletteCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? vibrantColor = freezed,
    Object? dominantColor = freezed,
    Object? mutedColor = freezed,
    Object? darkMutedColor = freezed,
    Object? darkVibrantColor = freezed,
    Object? lightMutedColor = freezed,
    Object? lightVibrantColor = freezed,
  }) {
    return _then(_value.copyWith(
      vibrantColor: freezed == vibrantColor
          ? _value.vibrantColor
          : vibrantColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      dominantColor: freezed == dominantColor
          ? _value.dominantColor
          : dominantColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      mutedColor: freezed == mutedColor
          ? _value.mutedColor
          : mutedColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      darkMutedColor: freezed == darkMutedColor
          ? _value.darkMutedColor
          : darkMutedColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      darkVibrantColor: freezed == darkVibrantColor
          ? _value.darkVibrantColor
          : darkVibrantColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      lightMutedColor: freezed == lightMutedColor
          ? _value.lightMutedColor
          : lightMutedColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      lightVibrantColor: freezed == lightVibrantColor
          ? _value.lightVibrantColor
          : lightVibrantColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$PaletteImplCopyWith<$Res> implements $PaletteCopyWith<$Res> {
  factory _$$PaletteImplCopyWith(
          _$PaletteImpl value, $Res Function(_$PaletteImpl) then) =
      __$$PaletteImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {PaletteColor? vibrantColor,
      PaletteColor? dominantColor,
      PaletteColor? mutedColor,
      PaletteColor? darkMutedColor,
      PaletteColor? darkVibrantColor,
      PaletteColor? lightMutedColor,
      PaletteColor? lightVibrantColor});
}

/// @nodoc
class __$$PaletteImplCopyWithImpl<$Res>
    extends _$PaletteCopyWithImpl<$Res, _$PaletteImpl>
    implements _$$PaletteImplCopyWith<$Res> {
  __$$PaletteImplCopyWithImpl(
      _$PaletteImpl _value, $Res Function(_$PaletteImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? vibrantColor = freezed,
    Object? dominantColor = freezed,
    Object? mutedColor = freezed,
    Object? darkMutedColor = freezed,
    Object? darkVibrantColor = freezed,
    Object? lightMutedColor = freezed,
    Object? lightVibrantColor = freezed,
  }) {
    return _then(_$PaletteImpl(
      vibrantColor: freezed == vibrantColor
          ? _value.vibrantColor
          : vibrantColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      dominantColor: freezed == dominantColor
          ? _value.dominantColor
          : dominantColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      mutedColor: freezed == mutedColor
          ? _value.mutedColor
          : mutedColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      darkMutedColor: freezed == darkMutedColor
          ? _value.darkMutedColor
          : darkMutedColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      darkVibrantColor: freezed == darkVibrantColor
          ? _value.darkVibrantColor
          : darkVibrantColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      lightMutedColor: freezed == lightMutedColor
          ? _value.lightMutedColor
          : lightMutedColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
      lightVibrantColor: freezed == lightVibrantColor
          ? _value.lightVibrantColor
          : lightVibrantColor // ignore: cast_nullable_to_non_nullable
              as PaletteColor?,
    ));
  }
}

/// @nodoc

class _$PaletteImpl extends _Palette {
  const _$PaletteImpl(
      {this.vibrantColor,
      this.dominantColor,
      this.mutedColor,
      this.darkMutedColor,
      this.darkVibrantColor,
      this.lightMutedColor,
      this.lightVibrantColor})
      : super._();

  @override
  final PaletteColor? vibrantColor;
  @override
  final PaletteColor? dominantColor;
  @override
  final PaletteColor? mutedColor;
  @override
  final PaletteColor? darkMutedColor;
  @override
  final PaletteColor? darkVibrantColor;
  @override
  final PaletteColor? lightMutedColor;
  @override
  final PaletteColor? lightVibrantColor;

  @override
  String toString() {
    return 'Palette(vibrantColor: $vibrantColor, dominantColor: $dominantColor, mutedColor: $mutedColor, darkMutedColor: $darkMutedColor, darkVibrantColor: $darkVibrantColor, lightMutedColor: $lightMutedColor, lightVibrantColor: $lightVibrantColor)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$PaletteImpl &&
            (identical(other.vibrantColor, vibrantColor) ||
                other.vibrantColor == vibrantColor) &&
            (identical(other.dominantColor, dominantColor) ||
                other.dominantColor == dominantColor) &&
            (identical(other.mutedColor, mutedColor) ||
                other.mutedColor == mutedColor) &&
            (identical(other.darkMutedColor, darkMutedColor) ||
                other.darkMutedColor == darkMutedColor) &&
            (identical(other.darkVibrantColor, darkVibrantColor) ||
                other.darkVibrantColor == darkVibrantColor) &&
            (identical(other.lightMutedColor, lightMutedColor) ||
                other.lightMutedColor == lightMutedColor) &&
            (identical(other.lightVibrantColor, lightVibrantColor) ||
                other.lightVibrantColor == lightVibrantColor));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      vibrantColor,
      dominantColor,
      mutedColor,
      darkMutedColor,
      darkVibrantColor,
      lightMutedColor,
      lightVibrantColor);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$PaletteImplCopyWith<_$PaletteImpl> get copyWith =>
      __$$PaletteImplCopyWithImpl<_$PaletteImpl>(this, _$identity);
}

abstract class _Palette extends Palette {
  const factory _Palette(
      {final PaletteColor? vibrantColor,
      final PaletteColor? dominantColor,
      final PaletteColor? mutedColor,
      final PaletteColor? darkMutedColor,
      final PaletteColor? darkVibrantColor,
      final PaletteColor? lightMutedColor,
      final PaletteColor? lightVibrantColor}) = _$PaletteImpl;
  const _Palette._() : super._();

  @override
  PaletteColor? get vibrantColor;
  @override
  PaletteColor? get dominantColor;
  @override
  PaletteColor? get mutedColor;
  @override
  PaletteColor? get darkMutedColor;
  @override
  PaletteColor? get darkVibrantColor;
  @override
  PaletteColor? get lightMutedColor;
  @override
  PaletteColor? get lightVibrantColor;
  @override
  @JsonKey(ignore: true)
  _$$PaletteImplCopyWith<_$PaletteImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ColorTheme {
  ThemeData get theme => throw _privateConstructorUsedError;
  Color get gradientHigh => throw _privateConstructorUsedError;
  Color get gradientLow => throw _privateConstructorUsedError;
  Color get darkBackground => throw _privateConstructorUsedError;
  Color get darkerBackground => throw _privateConstructorUsedError;
  Color get onDarkerBackground => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $ColorThemeCopyWith<ColorTheme> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ColorThemeCopyWith<$Res> {
  factory $ColorThemeCopyWith(
          ColorTheme value, $Res Function(ColorTheme) then) =
      _$ColorThemeCopyWithImpl<$Res, ColorTheme>;
  @useResult
  $Res call(
      {ThemeData theme,
      Color gradientHigh,
      Color gradientLow,
      Color darkBackground,
      Color darkerBackground,
      Color onDarkerBackground});
}

/// @nodoc
class _$ColorThemeCopyWithImpl<$Res, $Val extends ColorTheme>
    implements $ColorThemeCopyWith<$Res> {
  _$ColorThemeCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? theme = null,
    Object? gradientHigh = null,
    Object? gradientLow = null,
    Object? darkBackground = null,
    Object? darkerBackground = null,
    Object? onDarkerBackground = null,
  }) {
    return _then(_value.copyWith(
      theme: null == theme
          ? _value.theme
          : theme // ignore: cast_nullable_to_non_nullable
              as ThemeData,
      gradientHigh: null == gradientHigh
          ? _value.gradientHigh
          : gradientHigh // ignore: cast_nullable_to_non_nullable
              as Color,
      gradientLow: null == gradientLow
          ? _value.gradientLow
          : gradientLow // ignore: cast_nullable_to_non_nullable
              as Color,
      darkBackground: null == darkBackground
          ? _value.darkBackground
          : darkBackground // ignore: cast_nullable_to_non_nullable
              as Color,
      darkerBackground: null == darkerBackground
          ? _value.darkerBackground
          : darkerBackground // ignore: cast_nullable_to_non_nullable
              as Color,
      onDarkerBackground: null == onDarkerBackground
          ? _value.onDarkerBackground
          : onDarkerBackground // ignore: cast_nullable_to_non_nullable
              as Color,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ColorThemeImplCopyWith<$Res>
    implements $ColorThemeCopyWith<$Res> {
  factory _$$ColorThemeImplCopyWith(
          _$ColorThemeImpl value, $Res Function(_$ColorThemeImpl) then) =
      __$$ColorThemeImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {ThemeData theme,
      Color gradientHigh,
      Color gradientLow,
      Color darkBackground,
      Color darkerBackground,
      Color onDarkerBackground});
}

/// @nodoc
class __$$ColorThemeImplCopyWithImpl<$Res>
    extends _$ColorThemeCopyWithImpl<$Res, _$ColorThemeImpl>
    implements _$$ColorThemeImplCopyWith<$Res> {
  __$$ColorThemeImplCopyWithImpl(
      _$ColorThemeImpl _value, $Res Function(_$ColorThemeImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? theme = null,
    Object? gradientHigh = null,
    Object? gradientLow = null,
    Object? darkBackground = null,
    Object? darkerBackground = null,
    Object? onDarkerBackground = null,
  }) {
    return _then(_$ColorThemeImpl(
      theme: null == theme
          ? _value.theme
          : theme // ignore: cast_nullable_to_non_nullable
              as ThemeData,
      gradientHigh: null == gradientHigh
          ? _value.gradientHigh
          : gradientHigh // ignore: cast_nullable_to_non_nullable
              as Color,
      gradientLow: null == gradientLow
          ? _value.gradientLow
          : gradientLow // ignore: cast_nullable_to_non_nullable
              as Color,
      darkBackground: null == darkBackground
          ? _value.darkBackground
          : darkBackground // ignore: cast_nullable_to_non_nullable
              as Color,
      darkerBackground: null == darkerBackground
          ? _value.darkerBackground
          : darkerBackground // ignore: cast_nullable_to_non_nullable
              as Color,
      onDarkerBackground: null == onDarkerBackground
          ? _value.onDarkerBackground
          : onDarkerBackground // ignore: cast_nullable_to_non_nullable
              as Color,
    ));
  }
}

/// @nodoc

class _$ColorThemeImpl implements _ColorTheme {
  const _$ColorThemeImpl(
      {required this.theme,
      required this.gradientHigh,
      required this.gradientLow,
      required this.darkBackground,
      required this.darkerBackground,
      required this.onDarkerBackground});

  @override
  final ThemeData theme;
  @override
  final Color gradientHigh;
  @override
  final Color gradientLow;
  @override
  final Color darkBackground;
  @override
  final Color darkerBackground;
  @override
  final Color onDarkerBackground;

  @override
  String toString() {
    return 'ColorTheme(theme: $theme, gradientHigh: $gradientHigh, gradientLow: $gradientLow, darkBackground: $darkBackground, darkerBackground: $darkerBackground, onDarkerBackground: $onDarkerBackground)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ColorThemeImpl &&
            (identical(other.theme, theme) || other.theme == theme) &&
            (identical(other.gradientHigh, gradientHigh) ||
                other.gradientHigh == gradientHigh) &&
            (identical(other.gradientLow, gradientLow) ||
                other.gradientLow == gradientLow) &&
            (identical(other.darkBackground, darkBackground) ||
                other.darkBackground == darkBackground) &&
            (identical(other.darkerBackground, darkerBackground) ||
                other.darkerBackground == darkerBackground) &&
            (identical(other.onDarkerBackground, onDarkerBackground) ||
                other.onDarkerBackground == onDarkerBackground));
  }

  @override
  int get hashCode => Object.hash(runtimeType, theme, gradientHigh, gradientLow,
      darkBackground, darkerBackground, onDarkerBackground);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ColorThemeImplCopyWith<_$ColorThemeImpl> get copyWith =>
      __$$ColorThemeImplCopyWithImpl<_$ColorThemeImpl>(this, _$identity);
}

abstract class _ColorTheme implements ColorTheme {
  const factory _ColorTheme(
      {required final ThemeData theme,
      required final Color gradientHigh,
      required final Color gradientLow,
      required final Color darkBackground,
      required final Color darkerBackground,
      required final Color onDarkerBackground}) = _$ColorThemeImpl;

  @override
  ThemeData get theme;
  @override
  Color get gradientHigh;
  @override
  Color get gradientLow;
  @override
  Color get darkBackground;
  @override
  Color get darkerBackground;
  @override
  Color get onDarkerBackground;
  @override
  @JsonKey(ignore: true)
  _$$ColorThemeImplCopyWith<_$ColorThemeImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

QueueItemState _$QueueItemStateFromJson(Map<String, dynamic> json) {
  return _QueueItemState.fromJson(json);
}

/// @nodoc
mixin _$QueueItemState {
  String get id => throw _privateConstructorUsedError;
  QueueContextType get contextType => throw _privateConstructorUsedError;
  String? get contextId => throw _privateConstructorUsedError;
  String? get contextTitle => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $QueueItemStateCopyWith<QueueItemState> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $QueueItemStateCopyWith<$Res> {
  factory $QueueItemStateCopyWith(
          QueueItemState value, $Res Function(QueueItemState) then) =
      _$QueueItemStateCopyWithImpl<$Res, QueueItemState>;
  @useResult
  $Res call(
      {String id,
      QueueContextType contextType,
      String? contextId,
      String? contextTitle});
}

/// @nodoc
class _$QueueItemStateCopyWithImpl<$Res, $Val extends QueueItemState>
    implements $QueueItemStateCopyWith<$Res> {
  _$QueueItemStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? contextType = null,
    Object? contextId = freezed,
    Object? contextTitle = freezed,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      contextType: null == contextType
          ? _value.contextType
          : contextType // ignore: cast_nullable_to_non_nullable
              as QueueContextType,
      contextId: freezed == contextId
          ? _value.contextId
          : contextId // ignore: cast_nullable_to_non_nullable
              as String?,
      contextTitle: freezed == contextTitle
          ? _value.contextTitle
          : contextTitle // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$QueueItemStateImplCopyWith<$Res>
    implements $QueueItemStateCopyWith<$Res> {
  factory _$$QueueItemStateImplCopyWith(_$QueueItemStateImpl value,
          $Res Function(_$QueueItemStateImpl) then) =
      __$$QueueItemStateImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      QueueContextType contextType,
      String? contextId,
      String? contextTitle});
}

/// @nodoc
class __$$QueueItemStateImplCopyWithImpl<$Res>
    extends _$QueueItemStateCopyWithImpl<$Res, _$QueueItemStateImpl>
    implements _$$QueueItemStateImplCopyWith<$Res> {
  __$$QueueItemStateImplCopyWithImpl(
      _$QueueItemStateImpl _value, $Res Function(_$QueueItemStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? contextType = null,
    Object? contextId = freezed,
    Object? contextTitle = freezed,
  }) {
    return _then(_$QueueItemStateImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      contextType: null == contextType
          ? _value.contextType
          : contextType // ignore: cast_nullable_to_non_nullable
              as QueueContextType,
      contextId: freezed == contextId
          ? _value.contextId
          : contextId // ignore: cast_nullable_to_non_nullable
              as String?,
      contextTitle: freezed == contextTitle
          ? _value.contextTitle
          : contextTitle // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$QueueItemStateImpl implements _QueueItemState {
  const _$QueueItemStateImpl(
      {required this.id,
      required this.contextType,
      this.contextId,
      this.contextTitle});

  factory _$QueueItemStateImpl.fromJson(Map<String, dynamic> json) =>
      _$$QueueItemStateImplFromJson(json);

  @override
  final String id;
  @override
  final QueueContextType contextType;
  @override
  final String? contextId;
  @override
  final String? contextTitle;

  @override
  String toString() {
    return 'QueueItemState(id: $id, contextType: $contextType, contextId: $contextId, contextTitle: $contextTitle)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$QueueItemStateImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.contextType, contextType) ||
                other.contextType == contextType) &&
            (identical(other.contextId, contextId) ||
                other.contextId == contextId) &&
            (identical(other.contextTitle, contextTitle) ||
                other.contextTitle == contextTitle));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode =>
      Object.hash(runtimeType, id, contextType, contextId, contextTitle);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$QueueItemStateImplCopyWith<_$QueueItemStateImpl> get copyWith =>
      __$$QueueItemStateImplCopyWithImpl<_$QueueItemStateImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$QueueItemStateImplToJson(
      this,
    );
  }
}

abstract class _QueueItemState implements QueueItemState {
  const factory _QueueItemState(
      {required final String id,
      required final QueueContextType contextType,
      final String? contextId,
      final String? contextTitle}) = _$QueueItemStateImpl;

  factory _QueueItemState.fromJson(Map<String, dynamic> json) =
      _$QueueItemStateImpl.fromJson;

  @override
  String get id;
  @override
  QueueContextType get contextType;
  @override
  String? get contextId;
  @override
  String? get contextTitle;
  @override
  @JsonKey(ignore: true)
  _$$QueueItemStateImplCopyWith<_$QueueItemStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

MediaItemData _$MediaItemDataFromJson(Map<String, dynamic> json) {
  return _MediaItemData.fromJson(json);
}

/// @nodoc
mixin _$MediaItemData {
  int get sourceId => throw _privateConstructorUsedError;
  String? get albumId => throw _privateConstructorUsedError;
  @MediaItemArtCacheConverter()
  MediaItemArtCache? get artCache => throw _privateConstructorUsedError;
  QueueContextType get contextType => throw _privateConstructorUsedError;
  String? get contextId => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $MediaItemDataCopyWith<MediaItemData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MediaItemDataCopyWith<$Res> {
  factory $MediaItemDataCopyWith(
          MediaItemData value, $Res Function(MediaItemData) then) =
      _$MediaItemDataCopyWithImpl<$Res, MediaItemData>;
  @useResult
  $Res call(
      {int sourceId,
      String? albumId,
      @MediaItemArtCacheConverter() MediaItemArtCache? artCache,
      QueueContextType contextType,
      String? contextId});

  $MediaItemArtCacheCopyWith<$Res>? get artCache;
}

/// @nodoc
class _$MediaItemDataCopyWithImpl<$Res, $Val extends MediaItemData>
    implements $MediaItemDataCopyWith<$Res> {
  _$MediaItemDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? albumId = freezed,
    Object? artCache = freezed,
    Object? contextType = null,
    Object? contextId = freezed,
  }) {
    return _then(_value.copyWith(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      albumId: freezed == albumId
          ? _value.albumId
          : albumId // ignore: cast_nullable_to_non_nullable
              as String?,
      artCache: freezed == artCache
          ? _value.artCache
          : artCache // ignore: cast_nullable_to_non_nullable
              as MediaItemArtCache?,
      contextType: null == contextType
          ? _value.contextType
          : contextType // ignore: cast_nullable_to_non_nullable
              as QueueContextType,
      contextId: freezed == contextId
          ? _value.contextId
          : contextId // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $MediaItemArtCacheCopyWith<$Res>? get artCache {
    if (_value.artCache == null) {
      return null;
    }

    return $MediaItemArtCacheCopyWith<$Res>(_value.artCache!, (value) {
      return _then(_value.copyWith(artCache: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$MediaItemDataImplCopyWith<$Res>
    implements $MediaItemDataCopyWith<$Res> {
  factory _$$MediaItemDataImplCopyWith(
          _$MediaItemDataImpl value, $Res Function(_$MediaItemDataImpl) then) =
      __$$MediaItemDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int sourceId,
      String? albumId,
      @MediaItemArtCacheConverter() MediaItemArtCache? artCache,
      QueueContextType contextType,
      String? contextId});

  @override
  $MediaItemArtCacheCopyWith<$Res>? get artCache;
}

/// @nodoc
class __$$MediaItemDataImplCopyWithImpl<$Res>
    extends _$MediaItemDataCopyWithImpl<$Res, _$MediaItemDataImpl>
    implements _$$MediaItemDataImplCopyWith<$Res> {
  __$$MediaItemDataImplCopyWithImpl(
      _$MediaItemDataImpl _value, $Res Function(_$MediaItemDataImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? albumId = freezed,
    Object? artCache = freezed,
    Object? contextType = null,
    Object? contextId = freezed,
  }) {
    return _then(_$MediaItemDataImpl(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      albumId: freezed == albumId
          ? _value.albumId
          : albumId // ignore: cast_nullable_to_non_nullable
              as String?,
      artCache: freezed == artCache
          ? _value.artCache
          : artCache // ignore: cast_nullable_to_non_nullable
              as MediaItemArtCache?,
      contextType: null == contextType
          ? _value.contextType
          : contextType // ignore: cast_nullable_to_non_nullable
              as QueueContextType,
      contextId: freezed == contextId
          ? _value.contextId
          : contextId // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$MediaItemDataImpl implements _MediaItemData {
  const _$MediaItemDataImpl(
      {required this.sourceId,
      this.albumId,
      @MediaItemArtCacheConverter() this.artCache,
      required this.contextType,
      this.contextId});

  factory _$MediaItemDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$MediaItemDataImplFromJson(json);

  @override
  final int sourceId;
  @override
  final String? albumId;
  @override
  @MediaItemArtCacheConverter()
  final MediaItemArtCache? artCache;
  @override
  final QueueContextType contextType;
  @override
  final String? contextId;

  @override
  String toString() {
    return 'MediaItemData(sourceId: $sourceId, albumId: $albumId, artCache: $artCache, contextType: $contextType, contextId: $contextId)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MediaItemDataImpl &&
            (identical(other.sourceId, sourceId) ||
                other.sourceId == sourceId) &&
            (identical(other.albumId, albumId) || other.albumId == albumId) &&
            (identical(other.artCache, artCache) ||
                other.artCache == artCache) &&
            (identical(other.contextType, contextType) ||
                other.contextType == contextType) &&
            (identical(other.contextId, contextId) ||
                other.contextId == contextId));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType, sourceId, albumId, artCache, contextType, contextId);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$MediaItemDataImplCopyWith<_$MediaItemDataImpl> get copyWith =>
      __$$MediaItemDataImplCopyWithImpl<_$MediaItemDataImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$MediaItemDataImplToJson(
      this,
    );
  }
}

abstract class _MediaItemData implements MediaItemData {
  const factory _MediaItemData(
      {required final int sourceId,
      final String? albumId,
      @MediaItemArtCacheConverter() final MediaItemArtCache? artCache,
      required final QueueContextType contextType,
      final String? contextId}) = _$MediaItemDataImpl;

  factory _MediaItemData.fromJson(Map<String, dynamic> json) =
      _$MediaItemDataImpl.fromJson;

  @override
  int get sourceId;
  @override
  String? get albumId;
  @override
  @MediaItemArtCacheConverter()
  MediaItemArtCache? get artCache;
  @override
  QueueContextType get contextType;
  @override
  String? get contextId;
  @override
  @JsonKey(ignore: true)
  _$$MediaItemDataImplCopyWith<_$MediaItemDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

MediaItemArtCache _$MediaItemArtCacheFromJson(Map<String, dynamic> json) {
  return _MediaItemArtCache.fromJson(json);
}

/// @nodoc
mixin _$MediaItemArtCache {
  Uri get fullArtUri => throw _privateConstructorUsedError;
  String get fullArtCacheKey => throw _privateConstructorUsedError;
  Uri get thumbnailArtUri => throw _privateConstructorUsedError;
  String get thumbnailArtCacheKey => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $MediaItemArtCacheCopyWith<MediaItemArtCache> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MediaItemArtCacheCopyWith<$Res> {
  factory $MediaItemArtCacheCopyWith(
          MediaItemArtCache value, $Res Function(MediaItemArtCache) then) =
      _$MediaItemArtCacheCopyWithImpl<$Res, MediaItemArtCache>;
  @useResult
  $Res call(
      {Uri fullArtUri,
      String fullArtCacheKey,
      Uri thumbnailArtUri,
      String thumbnailArtCacheKey});
}

/// @nodoc
class _$MediaItemArtCacheCopyWithImpl<$Res, $Val extends MediaItemArtCache>
    implements $MediaItemArtCacheCopyWith<$Res> {
  _$MediaItemArtCacheCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? fullArtUri = null,
    Object? fullArtCacheKey = null,
    Object? thumbnailArtUri = null,
    Object? thumbnailArtCacheKey = null,
  }) {
    return _then(_value.copyWith(
      fullArtUri: null == fullArtUri
          ? _value.fullArtUri
          : fullArtUri // ignore: cast_nullable_to_non_nullable
              as Uri,
      fullArtCacheKey: null == fullArtCacheKey
          ? _value.fullArtCacheKey
          : fullArtCacheKey // ignore: cast_nullable_to_non_nullable
              as String,
      thumbnailArtUri: null == thumbnailArtUri
          ? _value.thumbnailArtUri
          : thumbnailArtUri // ignore: cast_nullable_to_non_nullable
              as Uri,
      thumbnailArtCacheKey: null == thumbnailArtCacheKey
          ? _value.thumbnailArtCacheKey
          : thumbnailArtCacheKey // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$MediaItemArtCacheImplCopyWith<$Res>
    implements $MediaItemArtCacheCopyWith<$Res> {
  factory _$$MediaItemArtCacheImplCopyWith(_$MediaItemArtCacheImpl value,
          $Res Function(_$MediaItemArtCacheImpl) then) =
      __$$MediaItemArtCacheImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {Uri fullArtUri,
      String fullArtCacheKey,
      Uri thumbnailArtUri,
      String thumbnailArtCacheKey});
}

/// @nodoc
class __$$MediaItemArtCacheImplCopyWithImpl<$Res>
    extends _$MediaItemArtCacheCopyWithImpl<$Res, _$MediaItemArtCacheImpl>
    implements _$$MediaItemArtCacheImplCopyWith<$Res> {
  __$$MediaItemArtCacheImplCopyWithImpl(_$MediaItemArtCacheImpl _value,
      $Res Function(_$MediaItemArtCacheImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? fullArtUri = null,
    Object? fullArtCacheKey = null,
    Object? thumbnailArtUri = null,
    Object? thumbnailArtCacheKey = null,
  }) {
    return _then(_$MediaItemArtCacheImpl(
      fullArtUri: null == fullArtUri
          ? _value.fullArtUri
          : fullArtUri // ignore: cast_nullable_to_non_nullable
              as Uri,
      fullArtCacheKey: null == fullArtCacheKey
          ? _value.fullArtCacheKey
          : fullArtCacheKey // ignore: cast_nullable_to_non_nullable
              as String,
      thumbnailArtUri: null == thumbnailArtUri
          ? _value.thumbnailArtUri
          : thumbnailArtUri // ignore: cast_nullable_to_non_nullable
              as Uri,
      thumbnailArtCacheKey: null == thumbnailArtCacheKey
          ? _value.thumbnailArtCacheKey
          : thumbnailArtCacheKey // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$MediaItemArtCacheImpl implements _MediaItemArtCache {
  const _$MediaItemArtCacheImpl(
      {required this.fullArtUri,
      required this.fullArtCacheKey,
      required this.thumbnailArtUri,
      required this.thumbnailArtCacheKey});

  factory _$MediaItemArtCacheImpl.fromJson(Map<String, dynamic> json) =>
      _$$MediaItemArtCacheImplFromJson(json);

  @override
  final Uri fullArtUri;
  @override
  final String fullArtCacheKey;
  @override
  final Uri thumbnailArtUri;
  @override
  final String thumbnailArtCacheKey;

  @override
  String toString() {
    return 'MediaItemArtCache(fullArtUri: $fullArtUri, fullArtCacheKey: $fullArtCacheKey, thumbnailArtUri: $thumbnailArtUri, thumbnailArtCacheKey: $thumbnailArtCacheKey)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MediaItemArtCacheImpl &&
            (identical(other.fullArtUri, fullArtUri) ||
                other.fullArtUri == fullArtUri) &&
            (identical(other.fullArtCacheKey, fullArtCacheKey) ||
                other.fullArtCacheKey == fullArtCacheKey) &&
            (identical(other.thumbnailArtUri, thumbnailArtUri) ||
                other.thumbnailArtUri == thumbnailArtUri) &&
            (identical(other.thumbnailArtCacheKey, thumbnailArtCacheKey) ||
                other.thumbnailArtCacheKey == thumbnailArtCacheKey));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, fullArtUri, fullArtCacheKey,
      thumbnailArtUri, thumbnailArtCacheKey);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$MediaItemArtCacheImplCopyWith<_$MediaItemArtCacheImpl> get copyWith =>
      __$$MediaItemArtCacheImplCopyWithImpl<_$MediaItemArtCacheImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$MediaItemArtCacheImplToJson(
      this,
    );
  }
}

abstract class _MediaItemArtCache implements MediaItemArtCache {
  const factory _MediaItemArtCache(
      {required final Uri fullArtUri,
      required final String fullArtCacheKey,
      required final Uri thumbnailArtUri,
      required final String thumbnailArtCacheKey}) = _$MediaItemArtCacheImpl;

  factory _MediaItemArtCache.fromJson(Map<String, dynamic> json) =
      _$MediaItemArtCacheImpl.fromJson;

  @override
  Uri get fullArtUri;
  @override
  String get fullArtCacheKey;
  @override
  Uri get thumbnailArtUri;
  @override
  String get thumbnailArtCacheKey;
  @override
  @JsonKey(ignore: true)
  _$$MediaItemArtCacheImplCopyWith<_$MediaItemArtCacheImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ListDownloadStatus {
  int get total => throw _privateConstructorUsedError;
  int get downloaded => throw _privateConstructorUsedError;
  int get downloading => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $ListDownloadStatusCopyWith<ListDownloadStatus> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ListDownloadStatusCopyWith<$Res> {
  factory $ListDownloadStatusCopyWith(
          ListDownloadStatus value, $Res Function(ListDownloadStatus) then) =
      _$ListDownloadStatusCopyWithImpl<$Res, ListDownloadStatus>;
  @useResult
  $Res call({int total, int downloaded, int downloading});
}

/// @nodoc
class _$ListDownloadStatusCopyWithImpl<$Res, $Val extends ListDownloadStatus>
    implements $ListDownloadStatusCopyWith<$Res> {
  _$ListDownloadStatusCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? total = null,
    Object? downloaded = null,
    Object? downloading = null,
  }) {
    return _then(_value.copyWith(
      total: null == total
          ? _value.total
          : total // ignore: cast_nullable_to_non_nullable
              as int,
      downloaded: null == downloaded
          ? _value.downloaded
          : downloaded // ignore: cast_nullable_to_non_nullable
              as int,
      downloading: null == downloading
          ? _value.downloading
          : downloading // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ListDownloadStatusImplCopyWith<$Res>
    implements $ListDownloadStatusCopyWith<$Res> {
  factory _$$ListDownloadStatusImplCopyWith(_$ListDownloadStatusImpl value,
          $Res Function(_$ListDownloadStatusImpl) then) =
      __$$ListDownloadStatusImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int total, int downloaded, int downloading});
}

/// @nodoc
class __$$ListDownloadStatusImplCopyWithImpl<$Res>
    extends _$ListDownloadStatusCopyWithImpl<$Res, _$ListDownloadStatusImpl>
    implements _$$ListDownloadStatusImplCopyWith<$Res> {
  __$$ListDownloadStatusImplCopyWithImpl(_$ListDownloadStatusImpl _value,
      $Res Function(_$ListDownloadStatusImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? total = null,
    Object? downloaded = null,
    Object? downloading = null,
  }) {
    return _then(_$ListDownloadStatusImpl(
      total: null == total
          ? _value.total
          : total // ignore: cast_nullable_to_non_nullable
              as int,
      downloaded: null == downloaded
          ? _value.downloaded
          : downloaded // ignore: cast_nullable_to_non_nullable
              as int,
      downloading: null == downloading
          ? _value.downloading
          : downloading // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$ListDownloadStatusImpl implements _ListDownloadStatus {
  const _$ListDownloadStatusImpl(
      {required this.total,
      required this.downloaded,
      required this.downloading});

  @override
  final int total;
  @override
  final int downloaded;
  @override
  final int downloading;

  @override
  String toString() {
    return 'ListDownloadStatus(total: $total, downloaded: $downloaded, downloading: $downloading)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ListDownloadStatusImpl &&
            (identical(other.total, total) || other.total == total) &&
            (identical(other.downloaded, downloaded) ||
                other.downloaded == downloaded) &&
            (identical(other.downloading, downloading) ||
                other.downloading == downloading));
  }

  @override
  int get hashCode => Object.hash(runtimeType, total, downloaded, downloading);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ListDownloadStatusImplCopyWith<_$ListDownloadStatusImpl> get copyWith =>
      __$$ListDownloadStatusImplCopyWithImpl<_$ListDownloadStatusImpl>(
          this, _$identity);
}

abstract class _ListDownloadStatus implements ListDownloadStatus {
  const factory _ListDownloadStatus(
      {required final int total,
      required final int downloaded,
      required final int downloading}) = _$ListDownloadStatusImpl;

  @override
  int get total;
  @override
  int get downloaded;
  @override
  int get downloading;
  @override
  @JsonKey(ignore: true)
  _$$ListDownloadStatusImplCopyWith<_$ListDownloadStatusImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$MultiChoiceOption {
  String get title => throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult when<TResult extends Object?>(
    TResult Function(String title) $default, {
    required TResult Function(String title, int option) int,
    required TResult Function(String title, String option) string,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>(
    TResult? Function(String title)? $default, {
    TResult? Function(String title, int option)? int,
    TResult? Function(String title, String option)? string,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>(
    TResult Function(String title)? $default, {
    TResult Function(String title, int option)? int,
    TResult Function(String title, String option)? string,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>(
    TResult Function(_MultiChoiceOption value) $default, {
    required TResult Function(_MultiChoiceOptionInt value) int,
    required TResult Function(_MultiChoiceOptionString value) string,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>(
    TResult? Function(_MultiChoiceOption value)? $default, {
    TResult? Function(_MultiChoiceOptionInt value)? int,
    TResult? Function(_MultiChoiceOptionString value)? string,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>(
    TResult Function(_MultiChoiceOption value)? $default, {
    TResult Function(_MultiChoiceOptionInt value)? int,
    TResult Function(_MultiChoiceOptionString value)? string,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $MultiChoiceOptionCopyWith<MultiChoiceOption> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $MultiChoiceOptionCopyWith<$Res> {
  factory $MultiChoiceOptionCopyWith(
          MultiChoiceOption value, $Res Function(MultiChoiceOption) then) =
      _$MultiChoiceOptionCopyWithImpl<$Res, MultiChoiceOption>;
  @useResult
  $Res call({String title});
}

/// @nodoc
class _$MultiChoiceOptionCopyWithImpl<$Res, $Val extends MultiChoiceOption>
    implements $MultiChoiceOptionCopyWith<$Res> {
  _$MultiChoiceOptionCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = null,
  }) {
    return _then(_value.copyWith(
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$MultiChoiceOptionImplCopyWith<$Res>
    implements $MultiChoiceOptionCopyWith<$Res> {
  factory _$$MultiChoiceOptionImplCopyWith(_$MultiChoiceOptionImpl value,
          $Res Function(_$MultiChoiceOptionImpl) then) =
      __$$MultiChoiceOptionImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String title});
}

/// @nodoc
class __$$MultiChoiceOptionImplCopyWithImpl<$Res>
    extends _$MultiChoiceOptionCopyWithImpl<$Res, _$MultiChoiceOptionImpl>
    implements _$$MultiChoiceOptionImplCopyWith<$Res> {
  __$$MultiChoiceOptionImplCopyWithImpl(_$MultiChoiceOptionImpl _value,
      $Res Function(_$MultiChoiceOptionImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = null,
  }) {
    return _then(_$MultiChoiceOptionImpl(
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$MultiChoiceOptionImpl implements _MultiChoiceOption {
  const _$MultiChoiceOptionImpl({required this.title});

  @override
  final String title;

  @override
  String toString() {
    return 'MultiChoiceOption(title: $title)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MultiChoiceOptionImpl &&
            (identical(other.title, title) || other.title == title));
  }

  @override
  int get hashCode => Object.hash(runtimeType, title);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$MultiChoiceOptionImplCopyWith<_$MultiChoiceOptionImpl> get copyWith =>
      __$$MultiChoiceOptionImplCopyWithImpl<_$MultiChoiceOptionImpl>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>(
    TResult Function(String title) $default, {
    required TResult Function(String title, int option) int,
    required TResult Function(String title, String option) string,
  }) {
    return $default(title);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>(
    TResult? Function(String title)? $default, {
    TResult? Function(String title, int option)? int,
    TResult? Function(String title, String option)? string,
  }) {
    return $default?.call(title);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>(
    TResult Function(String title)? $default, {
    TResult Function(String title, int option)? int,
    TResult Function(String title, String option)? string,
    required TResult orElse(),
  }) {
    if ($default != null) {
      return $default(title);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>(
    TResult Function(_MultiChoiceOption value) $default, {
    required TResult Function(_MultiChoiceOptionInt value) int,
    required TResult Function(_MultiChoiceOptionString value) string,
  }) {
    return $default(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>(
    TResult? Function(_MultiChoiceOption value)? $default, {
    TResult? Function(_MultiChoiceOptionInt value)? int,
    TResult? Function(_MultiChoiceOptionString value)? string,
  }) {
    return $default?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>(
    TResult Function(_MultiChoiceOption value)? $default, {
    TResult Function(_MultiChoiceOptionInt value)? int,
    TResult Function(_MultiChoiceOptionString value)? string,
    required TResult orElse(),
  }) {
    if ($default != null) {
      return $default(this);
    }
    return orElse();
  }
}

abstract class _MultiChoiceOption implements MultiChoiceOption {
  const factory _MultiChoiceOption({required final String title}) =
      _$MultiChoiceOptionImpl;

  @override
  String get title;
  @override
  @JsonKey(ignore: true)
  _$$MultiChoiceOptionImplCopyWith<_$MultiChoiceOptionImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$MultiChoiceOptionIntImplCopyWith<$Res>
    implements $MultiChoiceOptionCopyWith<$Res> {
  factory _$$MultiChoiceOptionIntImplCopyWith(_$MultiChoiceOptionIntImpl value,
          $Res Function(_$MultiChoiceOptionIntImpl) then) =
      __$$MultiChoiceOptionIntImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String title, int option});
}

/// @nodoc
class __$$MultiChoiceOptionIntImplCopyWithImpl<$Res>
    extends _$MultiChoiceOptionCopyWithImpl<$Res, _$MultiChoiceOptionIntImpl>
    implements _$$MultiChoiceOptionIntImplCopyWith<$Res> {
  __$$MultiChoiceOptionIntImplCopyWithImpl(_$MultiChoiceOptionIntImpl _value,
      $Res Function(_$MultiChoiceOptionIntImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = null,
    Object? option = null,
  }) {
    return _then(_$MultiChoiceOptionIntImpl(
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      option: null == option
          ? _value.option
          : option // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc

class _$MultiChoiceOptionIntImpl implements _MultiChoiceOptionInt {
  _$MultiChoiceOptionIntImpl({required this.title, required this.option});

  @override
  final String title;
  @override
  final int option;

  @override
  String toString() {
    return 'MultiChoiceOption.int(title: $title, option: $option)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MultiChoiceOptionIntImpl &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.option, option) || other.option == option));
  }

  @override
  int get hashCode => Object.hash(runtimeType, title, option);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$MultiChoiceOptionIntImplCopyWith<_$MultiChoiceOptionIntImpl>
      get copyWith =>
          __$$MultiChoiceOptionIntImplCopyWithImpl<_$MultiChoiceOptionIntImpl>(
              this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>(
    TResult Function(String title) $default, {
    required TResult Function(String title, int option) int,
    required TResult Function(String title, String option) string,
  }) {
    return int(title, option);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>(
    TResult? Function(String title)? $default, {
    TResult? Function(String title, int option)? int,
    TResult? Function(String title, String option)? string,
  }) {
    return int?.call(title, option);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>(
    TResult Function(String title)? $default, {
    TResult Function(String title, int option)? int,
    TResult Function(String title, String option)? string,
    required TResult orElse(),
  }) {
    if (int != null) {
      return int(title, option);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>(
    TResult Function(_MultiChoiceOption value) $default, {
    required TResult Function(_MultiChoiceOptionInt value) int,
    required TResult Function(_MultiChoiceOptionString value) string,
  }) {
    return int(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>(
    TResult? Function(_MultiChoiceOption value)? $default, {
    TResult? Function(_MultiChoiceOptionInt value)? int,
    TResult? Function(_MultiChoiceOptionString value)? string,
  }) {
    return int?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>(
    TResult Function(_MultiChoiceOption value)? $default, {
    TResult Function(_MultiChoiceOptionInt value)? int,
    TResult Function(_MultiChoiceOptionString value)? string,
    required TResult orElse(),
  }) {
    if (int != null) {
      return int(this);
    }
    return orElse();
  }
}

abstract class _MultiChoiceOptionInt implements MultiChoiceOption {
  factory _MultiChoiceOptionInt(
      {required final String title,
      required final int option}) = _$MultiChoiceOptionIntImpl;

  @override
  String get title;
  int get option;
  @override
  @JsonKey(ignore: true)
  _$$MultiChoiceOptionIntImplCopyWith<_$MultiChoiceOptionIntImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$MultiChoiceOptionStringImplCopyWith<$Res>
    implements $MultiChoiceOptionCopyWith<$Res> {
  factory _$$MultiChoiceOptionStringImplCopyWith(
          _$MultiChoiceOptionStringImpl value,
          $Res Function(_$MultiChoiceOptionStringImpl) then) =
      __$$MultiChoiceOptionStringImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String title, String option});
}

/// @nodoc
class __$$MultiChoiceOptionStringImplCopyWithImpl<$Res>
    extends _$MultiChoiceOptionCopyWithImpl<$Res, _$MultiChoiceOptionStringImpl>
    implements _$$MultiChoiceOptionStringImplCopyWith<$Res> {
  __$$MultiChoiceOptionStringImplCopyWithImpl(
      _$MultiChoiceOptionStringImpl _value,
      $Res Function(_$MultiChoiceOptionStringImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = null,
    Object? option = null,
  }) {
    return _then(_$MultiChoiceOptionStringImpl(
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      option: null == option
          ? _value.option
          : option // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$MultiChoiceOptionStringImpl implements _MultiChoiceOptionString {
  _$MultiChoiceOptionStringImpl({required this.title, required this.option});

  @override
  final String title;
  @override
  final String option;

  @override
  String toString() {
    return 'MultiChoiceOption.string(title: $title, option: $option)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$MultiChoiceOptionStringImpl &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.option, option) || other.option == option));
  }

  @override
  int get hashCode => Object.hash(runtimeType, title, option);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$MultiChoiceOptionStringImplCopyWith<_$MultiChoiceOptionStringImpl>
      get copyWith => __$$MultiChoiceOptionStringImplCopyWithImpl<
          _$MultiChoiceOptionStringImpl>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>(
    TResult Function(String title) $default, {
    required TResult Function(String title, int option) int,
    required TResult Function(String title, String option) string,
  }) {
    return string(title, option);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>(
    TResult? Function(String title)? $default, {
    TResult? Function(String title, int option)? int,
    TResult? Function(String title, String option)? string,
  }) {
    return string?.call(title, option);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>(
    TResult Function(String title)? $default, {
    TResult Function(String title, int option)? int,
    TResult Function(String title, String option)? string,
    required TResult orElse(),
  }) {
    if (string != null) {
      return string(title, option);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>(
    TResult Function(_MultiChoiceOption value) $default, {
    required TResult Function(_MultiChoiceOptionInt value) int,
    required TResult Function(_MultiChoiceOptionString value) string,
  }) {
    return string(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>(
    TResult? Function(_MultiChoiceOption value)? $default, {
    TResult? Function(_MultiChoiceOptionInt value)? int,
    TResult? Function(_MultiChoiceOptionString value)? string,
  }) {
    return string?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>(
    TResult Function(_MultiChoiceOption value)? $default, {
    TResult Function(_MultiChoiceOptionInt value)? int,
    TResult Function(_MultiChoiceOptionString value)? string,
    required TResult orElse(),
  }) {
    if (string != null) {
      return string(this);
    }
    return orElse();
  }
}

abstract class _MultiChoiceOptionString implements MultiChoiceOption {
  factory _MultiChoiceOptionString(
      {required final String title,
      required final String option}) = _$MultiChoiceOptionStringImpl;

  @override
  String get title;
  String get option;
  @override
  @JsonKey(ignore: true)
  _$$MultiChoiceOptionStringImplCopyWith<_$MultiChoiceOptionStringImpl>
      get copyWith => throw _privateConstructorUsedError;
}
