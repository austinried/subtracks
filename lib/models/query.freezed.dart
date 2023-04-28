// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'query.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#custom-getters-and-methods');

Pagination _$PaginationFromJson(Map<String, dynamic> json) {
  return _Pagination.fromJson(json);
}

/// @nodoc
mixin _$Pagination {
  int get limit => throw _privateConstructorUsedError;
  int get offset => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $PaginationCopyWith<Pagination> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $PaginationCopyWith<$Res> {
  factory $PaginationCopyWith(
          Pagination value, $Res Function(Pagination) then) =
      _$PaginationCopyWithImpl<$Res, Pagination>;
  @useResult
  $Res call({int limit, int offset});
}

/// @nodoc
class _$PaginationCopyWithImpl<$Res, $Val extends Pagination>
    implements $PaginationCopyWith<$Res> {
  _$PaginationCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? limit = null,
    Object? offset = null,
  }) {
    return _then(_value.copyWith(
      limit: null == limit
          ? _value.limit
          : limit // ignore: cast_nullable_to_non_nullable
              as int,
      offset: null == offset
          ? _value.offset
          : offset // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$_PaginationCopyWith<$Res>
    implements $PaginationCopyWith<$Res> {
  factory _$$_PaginationCopyWith(
          _$_Pagination value, $Res Function(_$_Pagination) then) =
      __$$_PaginationCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int limit, int offset});
}

/// @nodoc
class __$$_PaginationCopyWithImpl<$Res>
    extends _$PaginationCopyWithImpl<$Res, _$_Pagination>
    implements _$$_PaginationCopyWith<$Res> {
  __$$_PaginationCopyWithImpl(
      _$_Pagination _value, $Res Function(_$_Pagination) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? limit = null,
    Object? offset = null,
  }) {
    return _then(_$_Pagination(
      limit: null == limit
          ? _value.limit
          : limit // ignore: cast_nullable_to_non_nullable
              as int,
      offset: null == offset
          ? _value.offset
          : offset // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_Pagination implements _Pagination {
  const _$_Pagination({required this.limit, this.offset = 0});

  factory _$_Pagination.fromJson(Map<String, dynamic> json) =>
      _$$_PaginationFromJson(json);

  @override
  final int limit;
  @override
  @JsonKey()
  final int offset;

  @override
  String toString() {
    return 'Pagination(limit: $limit, offset: $offset)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_Pagination &&
            (identical(other.limit, limit) || other.limit == limit) &&
            (identical(other.offset, offset) || other.offset == offset));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, limit, offset);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_PaginationCopyWith<_$_Pagination> get copyWith =>
      __$$_PaginationCopyWithImpl<_$_Pagination>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$_PaginationToJson(
      this,
    );
  }
}

abstract class _Pagination implements Pagination {
  const factory _Pagination({required final int limit, final int offset}) =
      _$_Pagination;

  factory _Pagination.fromJson(Map<String, dynamic> json) =
      _$_Pagination.fromJson;

  @override
  int get limit;
  @override
  int get offset;
  @override
  @JsonKey(ignore: true)
  _$$_PaginationCopyWith<_$_Pagination> get copyWith =>
      throw _privateConstructorUsedError;
}

SortBy _$SortByFromJson(Map<String, dynamic> json) {
  return _SortBy.fromJson(json);
}

/// @nodoc
mixin _$SortBy {
  String get column => throw _privateConstructorUsedError;
  SortDirection get dir => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $SortByCopyWith<SortBy> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SortByCopyWith<$Res> {
  factory $SortByCopyWith(SortBy value, $Res Function(SortBy) then) =
      _$SortByCopyWithImpl<$Res, SortBy>;
  @useResult
  $Res call({String column, SortDirection dir});
}

/// @nodoc
class _$SortByCopyWithImpl<$Res, $Val extends SortBy>
    implements $SortByCopyWith<$Res> {
  _$SortByCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? dir = null,
  }) {
    return _then(_value.copyWith(
      column: null == column
          ? _value.column
          : column // ignore: cast_nullable_to_non_nullable
              as String,
      dir: null == dir
          ? _value.dir
          : dir // ignore: cast_nullable_to_non_nullable
              as SortDirection,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$_SortByCopyWith<$Res> implements $SortByCopyWith<$Res> {
  factory _$$_SortByCopyWith(_$_SortBy value, $Res Function(_$_SortBy) then) =
      __$$_SortByCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, SortDirection dir});
}

/// @nodoc
class __$$_SortByCopyWithImpl<$Res>
    extends _$SortByCopyWithImpl<$Res, _$_SortBy>
    implements _$$_SortByCopyWith<$Res> {
  __$$_SortByCopyWithImpl(_$_SortBy _value, $Res Function(_$_SortBy) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? dir = null,
  }) {
    return _then(_$_SortBy(
      column: null == column
          ? _value.column
          : column // ignore: cast_nullable_to_non_nullable
              as String,
      dir: null == dir
          ? _value.dir
          : dir // ignore: cast_nullable_to_non_nullable
              as SortDirection,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_SortBy implements _SortBy {
  const _$_SortBy({required this.column, this.dir = SortDirection.asc});

  factory _$_SortBy.fromJson(Map<String, dynamic> json) =>
      _$$_SortByFromJson(json);

  @override
  final String column;
  @override
  @JsonKey()
  final SortDirection dir;

  @override
  String toString() {
    return 'SortBy(column: $column, dir: $dir)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_SortBy &&
            (identical(other.column, column) || other.column == column) &&
            (identical(other.dir, dir) || other.dir == dir));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, column, dir);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_SortByCopyWith<_$_SortBy> get copyWith =>
      __$$_SortByCopyWithImpl<_$_SortBy>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$_SortByToJson(
      this,
    );
  }
}

abstract class _SortBy implements SortBy {
  const factory _SortBy(
      {required final String column, final SortDirection dir}) = _$_SortBy;

  factory _SortBy.fromJson(Map<String, dynamic> json) = _$_SortBy.fromJson;

  @override
  String get column;
  @override
  SortDirection get dir;
  @override
  @JsonKey(ignore: true)
  _$$_SortByCopyWith<_$_SortBy> get copyWith =>
      throw _privateConstructorUsedError;
}

FilterWith _$FilterWithFromJson(Map<String, dynamic> json) {
  switch (json['runtimeType']) {
    case 'equals':
      return _FilterWithEquals.fromJson(json);
    case 'greaterThan':
      return _FilterWithGreaterThan.fromJson(json);
    case 'isNull':
      return _FilterWithIsNull.fromJson(json);
    case 'betweenInt':
      return _FilterWithBetweenInt.fromJson(json);
    case 'isIn':
      return _FilterWithIsIn.fromJson(json);

    default:
      throw CheckedFromJsonException(json, 'runtimeType', 'FilterWith',
          'Invalid union type "${json['runtimeType']}"!');
  }
}

/// @nodoc
mixin _$FilterWith {
  String get column => throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String column, String value, bool invert) equals,
    required TResult Function(String column, String value, bool orEquals)
        greaterThan,
    required TResult Function(String column, bool invert) isNull,
    required TResult Function(String column, int from, int to) betweenInt,
    required TResult Function(String column, bool invert, IList<String> values)
        isIn,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String column, String value, bool invert)? equals,
    TResult? Function(String column, String value, bool orEquals)? greaterThan,
    TResult? Function(String column, bool invert)? isNull,
    TResult? Function(String column, int from, int to)? betweenInt,
    TResult? Function(String column, bool invert, IList<String> values)? isIn,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String column, String value, bool invert)? equals,
    TResult Function(String column, String value, bool orEquals)? greaterThan,
    TResult Function(String column, bool invert)? isNull,
    TResult Function(String column, int from, int to)? betweenInt,
    TResult Function(String column, bool invert, IList<String> values)? isIn,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_FilterWithEquals value) equals,
    required TResult Function(_FilterWithGreaterThan value) greaterThan,
    required TResult Function(_FilterWithIsNull value) isNull,
    required TResult Function(_FilterWithBetweenInt value) betweenInt,
    required TResult Function(_FilterWithIsIn value) isIn,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_FilterWithEquals value)? equals,
    TResult? Function(_FilterWithGreaterThan value)? greaterThan,
    TResult? Function(_FilterWithIsNull value)? isNull,
    TResult? Function(_FilterWithBetweenInt value)? betweenInt,
    TResult? Function(_FilterWithIsIn value)? isIn,
  }) =>
      throw _privateConstructorUsedError;
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_FilterWithEquals value)? equals,
    TResult Function(_FilterWithGreaterThan value)? greaterThan,
    TResult Function(_FilterWithIsNull value)? isNull,
    TResult Function(_FilterWithBetweenInt value)? betweenInt,
    TResult Function(_FilterWithIsIn value)? isIn,
    required TResult orElse(),
  }) =>
      throw _privateConstructorUsedError;
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $FilterWithCopyWith<FilterWith> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $FilterWithCopyWith<$Res> {
  factory $FilterWithCopyWith(
          FilterWith value, $Res Function(FilterWith) then) =
      _$FilterWithCopyWithImpl<$Res, FilterWith>;
  @useResult
  $Res call({String column});
}

/// @nodoc
class _$FilterWithCopyWithImpl<$Res, $Val extends FilterWith>
    implements $FilterWithCopyWith<$Res> {
  _$FilterWithCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
  }) {
    return _then(_value.copyWith(
      column: null == column
          ? _value.column
          : column // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$_FilterWithEqualsCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$_FilterWithEqualsCopyWith(
          _$_FilterWithEquals value, $Res Function(_$_FilterWithEquals) then) =
      __$$_FilterWithEqualsCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, String value, bool invert});
}

/// @nodoc
class __$$_FilterWithEqualsCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$_FilterWithEquals>
    implements _$$_FilterWithEqualsCopyWith<$Res> {
  __$$_FilterWithEqualsCopyWithImpl(
      _$_FilterWithEquals _value, $Res Function(_$_FilterWithEquals) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? value = null,
    Object? invert = null,
  }) {
    return _then(_$_FilterWithEquals(
      column: null == column
          ? _value.column
          : column // ignore: cast_nullable_to_non_nullable
              as String,
      value: null == value
          ? _value.value
          : value // ignore: cast_nullable_to_non_nullable
              as String,
      invert: null == invert
          ? _value.invert
          : invert // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_FilterWithEquals implements _FilterWithEquals {
  const _$_FilterWithEquals(
      {required this.column,
      required this.value,
      this.invert = false,
      final String? $type})
      : $type = $type ?? 'equals';

  factory _$_FilterWithEquals.fromJson(Map<String, dynamic> json) =>
      _$$_FilterWithEqualsFromJson(json);

  @override
  final String column;
  @override
  final String value;
  @override
  @JsonKey()
  final bool invert;

  @JsonKey(name: 'runtimeType')
  final String $type;

  @override
  String toString() {
    return 'FilterWith.equals(column: $column, value: $value, invert: $invert)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_FilterWithEquals &&
            (identical(other.column, column) || other.column == column) &&
            (identical(other.value, value) || other.value == value) &&
            (identical(other.invert, invert) || other.invert == invert));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, column, value, invert);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_FilterWithEqualsCopyWith<_$_FilterWithEquals> get copyWith =>
      __$$_FilterWithEqualsCopyWithImpl<_$_FilterWithEquals>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String column, String value, bool invert) equals,
    required TResult Function(String column, String value, bool orEquals)
        greaterThan,
    required TResult Function(String column, bool invert) isNull,
    required TResult Function(String column, int from, int to) betweenInt,
    required TResult Function(String column, bool invert, IList<String> values)
        isIn,
  }) {
    return equals(column, value, invert);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String column, String value, bool invert)? equals,
    TResult? Function(String column, String value, bool orEquals)? greaterThan,
    TResult? Function(String column, bool invert)? isNull,
    TResult? Function(String column, int from, int to)? betweenInt,
    TResult? Function(String column, bool invert, IList<String> values)? isIn,
  }) {
    return equals?.call(column, value, invert);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String column, String value, bool invert)? equals,
    TResult Function(String column, String value, bool orEquals)? greaterThan,
    TResult Function(String column, bool invert)? isNull,
    TResult Function(String column, int from, int to)? betweenInt,
    TResult Function(String column, bool invert, IList<String> values)? isIn,
    required TResult orElse(),
  }) {
    if (equals != null) {
      return equals(column, value, invert);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_FilterWithEquals value) equals,
    required TResult Function(_FilterWithGreaterThan value) greaterThan,
    required TResult Function(_FilterWithIsNull value) isNull,
    required TResult Function(_FilterWithBetweenInt value) betweenInt,
    required TResult Function(_FilterWithIsIn value) isIn,
  }) {
    return equals(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_FilterWithEquals value)? equals,
    TResult? Function(_FilterWithGreaterThan value)? greaterThan,
    TResult? Function(_FilterWithIsNull value)? isNull,
    TResult? Function(_FilterWithBetweenInt value)? betweenInt,
    TResult? Function(_FilterWithIsIn value)? isIn,
  }) {
    return equals?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_FilterWithEquals value)? equals,
    TResult Function(_FilterWithGreaterThan value)? greaterThan,
    TResult Function(_FilterWithIsNull value)? isNull,
    TResult Function(_FilterWithBetweenInt value)? betweenInt,
    TResult Function(_FilterWithIsIn value)? isIn,
    required TResult orElse(),
  }) {
    if (equals != null) {
      return equals(this);
    }
    return orElse();
  }

  @override
  Map<String, dynamic> toJson() {
    return _$$_FilterWithEqualsToJson(
      this,
    );
  }
}

abstract class _FilterWithEquals implements FilterWith {
  const factory _FilterWithEquals(
      {required final String column,
      required final String value,
      final bool invert}) = _$_FilterWithEquals;

  factory _FilterWithEquals.fromJson(Map<String, dynamic> json) =
      _$_FilterWithEquals.fromJson;

  @override
  String get column;
  String get value;
  bool get invert;
  @override
  @JsonKey(ignore: true)
  _$$_FilterWithEqualsCopyWith<_$_FilterWithEquals> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$_FilterWithGreaterThanCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$_FilterWithGreaterThanCopyWith(_$_FilterWithGreaterThan value,
          $Res Function(_$_FilterWithGreaterThan) then) =
      __$$_FilterWithGreaterThanCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, String value, bool orEquals});
}

/// @nodoc
class __$$_FilterWithGreaterThanCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$_FilterWithGreaterThan>
    implements _$$_FilterWithGreaterThanCopyWith<$Res> {
  __$$_FilterWithGreaterThanCopyWithImpl(_$_FilterWithGreaterThan _value,
      $Res Function(_$_FilterWithGreaterThan) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? value = null,
    Object? orEquals = null,
  }) {
    return _then(_$_FilterWithGreaterThan(
      column: null == column
          ? _value.column
          : column // ignore: cast_nullable_to_non_nullable
              as String,
      value: null == value
          ? _value.value
          : value // ignore: cast_nullable_to_non_nullable
              as String,
      orEquals: null == orEquals
          ? _value.orEquals
          : orEquals // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_FilterWithGreaterThan implements _FilterWithGreaterThan {
  const _$_FilterWithGreaterThan(
      {required this.column,
      required this.value,
      this.orEquals = false,
      final String? $type})
      : $type = $type ?? 'greaterThan';

  factory _$_FilterWithGreaterThan.fromJson(Map<String, dynamic> json) =>
      _$$_FilterWithGreaterThanFromJson(json);

  @override
  final String column;
  @override
  final String value;
  @override
  @JsonKey()
  final bool orEquals;

  @JsonKey(name: 'runtimeType')
  final String $type;

  @override
  String toString() {
    return 'FilterWith.greaterThan(column: $column, value: $value, orEquals: $orEquals)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_FilterWithGreaterThan &&
            (identical(other.column, column) || other.column == column) &&
            (identical(other.value, value) || other.value == value) &&
            (identical(other.orEquals, orEquals) ||
                other.orEquals == orEquals));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, column, value, orEquals);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_FilterWithGreaterThanCopyWith<_$_FilterWithGreaterThan> get copyWith =>
      __$$_FilterWithGreaterThanCopyWithImpl<_$_FilterWithGreaterThan>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String column, String value, bool invert) equals,
    required TResult Function(String column, String value, bool orEquals)
        greaterThan,
    required TResult Function(String column, bool invert) isNull,
    required TResult Function(String column, int from, int to) betweenInt,
    required TResult Function(String column, bool invert, IList<String> values)
        isIn,
  }) {
    return greaterThan(column, value, orEquals);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String column, String value, bool invert)? equals,
    TResult? Function(String column, String value, bool orEquals)? greaterThan,
    TResult? Function(String column, bool invert)? isNull,
    TResult? Function(String column, int from, int to)? betweenInt,
    TResult? Function(String column, bool invert, IList<String> values)? isIn,
  }) {
    return greaterThan?.call(column, value, orEquals);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String column, String value, bool invert)? equals,
    TResult Function(String column, String value, bool orEquals)? greaterThan,
    TResult Function(String column, bool invert)? isNull,
    TResult Function(String column, int from, int to)? betweenInt,
    TResult Function(String column, bool invert, IList<String> values)? isIn,
    required TResult orElse(),
  }) {
    if (greaterThan != null) {
      return greaterThan(column, value, orEquals);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_FilterWithEquals value) equals,
    required TResult Function(_FilterWithGreaterThan value) greaterThan,
    required TResult Function(_FilterWithIsNull value) isNull,
    required TResult Function(_FilterWithBetweenInt value) betweenInt,
    required TResult Function(_FilterWithIsIn value) isIn,
  }) {
    return greaterThan(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_FilterWithEquals value)? equals,
    TResult? Function(_FilterWithGreaterThan value)? greaterThan,
    TResult? Function(_FilterWithIsNull value)? isNull,
    TResult? Function(_FilterWithBetweenInt value)? betweenInt,
    TResult? Function(_FilterWithIsIn value)? isIn,
  }) {
    return greaterThan?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_FilterWithEquals value)? equals,
    TResult Function(_FilterWithGreaterThan value)? greaterThan,
    TResult Function(_FilterWithIsNull value)? isNull,
    TResult Function(_FilterWithBetweenInt value)? betweenInt,
    TResult Function(_FilterWithIsIn value)? isIn,
    required TResult orElse(),
  }) {
    if (greaterThan != null) {
      return greaterThan(this);
    }
    return orElse();
  }

  @override
  Map<String, dynamic> toJson() {
    return _$$_FilterWithGreaterThanToJson(
      this,
    );
  }
}

abstract class _FilterWithGreaterThan implements FilterWith {
  const factory _FilterWithGreaterThan(
      {required final String column,
      required final String value,
      final bool orEquals}) = _$_FilterWithGreaterThan;

  factory _FilterWithGreaterThan.fromJson(Map<String, dynamic> json) =
      _$_FilterWithGreaterThan.fromJson;

  @override
  String get column;
  String get value;
  bool get orEquals;
  @override
  @JsonKey(ignore: true)
  _$$_FilterWithGreaterThanCopyWith<_$_FilterWithGreaterThan> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$_FilterWithIsNullCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$_FilterWithIsNullCopyWith(
          _$_FilterWithIsNull value, $Res Function(_$_FilterWithIsNull) then) =
      __$$_FilterWithIsNullCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, bool invert});
}

/// @nodoc
class __$$_FilterWithIsNullCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$_FilterWithIsNull>
    implements _$$_FilterWithIsNullCopyWith<$Res> {
  __$$_FilterWithIsNullCopyWithImpl(
      _$_FilterWithIsNull _value, $Res Function(_$_FilterWithIsNull) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? invert = null,
  }) {
    return _then(_$_FilterWithIsNull(
      column: null == column
          ? _value.column
          : column // ignore: cast_nullable_to_non_nullable
              as String,
      invert: null == invert
          ? _value.invert
          : invert // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_FilterWithIsNull implements _FilterWithIsNull {
  const _$_FilterWithIsNull(
      {required this.column, this.invert = false, final String? $type})
      : $type = $type ?? 'isNull';

  factory _$_FilterWithIsNull.fromJson(Map<String, dynamic> json) =>
      _$$_FilterWithIsNullFromJson(json);

  @override
  final String column;
  @override
  @JsonKey()
  final bool invert;

  @JsonKey(name: 'runtimeType')
  final String $type;

  @override
  String toString() {
    return 'FilterWith.isNull(column: $column, invert: $invert)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_FilterWithIsNull &&
            (identical(other.column, column) || other.column == column) &&
            (identical(other.invert, invert) || other.invert == invert));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, column, invert);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_FilterWithIsNullCopyWith<_$_FilterWithIsNull> get copyWith =>
      __$$_FilterWithIsNullCopyWithImpl<_$_FilterWithIsNull>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String column, String value, bool invert) equals,
    required TResult Function(String column, String value, bool orEquals)
        greaterThan,
    required TResult Function(String column, bool invert) isNull,
    required TResult Function(String column, int from, int to) betweenInt,
    required TResult Function(String column, bool invert, IList<String> values)
        isIn,
  }) {
    return isNull(column, invert);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String column, String value, bool invert)? equals,
    TResult? Function(String column, String value, bool orEquals)? greaterThan,
    TResult? Function(String column, bool invert)? isNull,
    TResult? Function(String column, int from, int to)? betweenInt,
    TResult? Function(String column, bool invert, IList<String> values)? isIn,
  }) {
    return isNull?.call(column, invert);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String column, String value, bool invert)? equals,
    TResult Function(String column, String value, bool orEquals)? greaterThan,
    TResult Function(String column, bool invert)? isNull,
    TResult Function(String column, int from, int to)? betweenInt,
    TResult Function(String column, bool invert, IList<String> values)? isIn,
    required TResult orElse(),
  }) {
    if (isNull != null) {
      return isNull(column, invert);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_FilterWithEquals value) equals,
    required TResult Function(_FilterWithGreaterThan value) greaterThan,
    required TResult Function(_FilterWithIsNull value) isNull,
    required TResult Function(_FilterWithBetweenInt value) betweenInt,
    required TResult Function(_FilterWithIsIn value) isIn,
  }) {
    return isNull(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_FilterWithEquals value)? equals,
    TResult? Function(_FilterWithGreaterThan value)? greaterThan,
    TResult? Function(_FilterWithIsNull value)? isNull,
    TResult? Function(_FilterWithBetweenInt value)? betweenInt,
    TResult? Function(_FilterWithIsIn value)? isIn,
  }) {
    return isNull?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_FilterWithEquals value)? equals,
    TResult Function(_FilterWithGreaterThan value)? greaterThan,
    TResult Function(_FilterWithIsNull value)? isNull,
    TResult Function(_FilterWithBetweenInt value)? betweenInt,
    TResult Function(_FilterWithIsIn value)? isIn,
    required TResult orElse(),
  }) {
    if (isNull != null) {
      return isNull(this);
    }
    return orElse();
  }

  @override
  Map<String, dynamic> toJson() {
    return _$$_FilterWithIsNullToJson(
      this,
    );
  }
}

abstract class _FilterWithIsNull implements FilterWith {
  const factory _FilterWithIsNull(
      {required final String column, final bool invert}) = _$_FilterWithIsNull;

  factory _FilterWithIsNull.fromJson(Map<String, dynamic> json) =
      _$_FilterWithIsNull.fromJson;

  @override
  String get column;
  bool get invert;
  @override
  @JsonKey(ignore: true)
  _$$_FilterWithIsNullCopyWith<_$_FilterWithIsNull> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$_FilterWithBetweenIntCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$_FilterWithBetweenIntCopyWith(_$_FilterWithBetweenInt value,
          $Res Function(_$_FilterWithBetweenInt) then) =
      __$$_FilterWithBetweenIntCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, int from, int to});
}

/// @nodoc
class __$$_FilterWithBetweenIntCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$_FilterWithBetweenInt>
    implements _$$_FilterWithBetweenIntCopyWith<$Res> {
  __$$_FilterWithBetweenIntCopyWithImpl(_$_FilterWithBetweenInt _value,
      $Res Function(_$_FilterWithBetweenInt) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? from = null,
    Object? to = null,
  }) {
    return _then(_$_FilterWithBetweenInt(
      column: null == column
          ? _value.column
          : column // ignore: cast_nullable_to_non_nullable
              as String,
      from: null == from
          ? _value.from
          : from // ignore: cast_nullable_to_non_nullable
              as int,
      to: null == to
          ? _value.to
          : to // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_FilterWithBetweenInt implements _FilterWithBetweenInt {
  const _$_FilterWithBetweenInt(
      {required this.column,
      required this.from,
      required this.to,
      final String? $type})
      : $type = $type ?? 'betweenInt';

  factory _$_FilterWithBetweenInt.fromJson(Map<String, dynamic> json) =>
      _$$_FilterWithBetweenIntFromJson(json);

  @override
  final String column;
  @override
  final int from;
  @override
  final int to;

  @JsonKey(name: 'runtimeType')
  final String $type;

  @override
  String toString() {
    return 'FilterWith.betweenInt(column: $column, from: $from, to: $to)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_FilterWithBetweenInt &&
            (identical(other.column, column) || other.column == column) &&
            (identical(other.from, from) || other.from == from) &&
            (identical(other.to, to) || other.to == to));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, column, from, to);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_FilterWithBetweenIntCopyWith<_$_FilterWithBetweenInt> get copyWith =>
      __$$_FilterWithBetweenIntCopyWithImpl<_$_FilterWithBetweenInt>(
          this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String column, String value, bool invert) equals,
    required TResult Function(String column, String value, bool orEquals)
        greaterThan,
    required TResult Function(String column, bool invert) isNull,
    required TResult Function(String column, int from, int to) betweenInt,
    required TResult Function(String column, bool invert, IList<String> values)
        isIn,
  }) {
    return betweenInt(column, from, to);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String column, String value, bool invert)? equals,
    TResult? Function(String column, String value, bool orEquals)? greaterThan,
    TResult? Function(String column, bool invert)? isNull,
    TResult? Function(String column, int from, int to)? betweenInt,
    TResult? Function(String column, bool invert, IList<String> values)? isIn,
  }) {
    return betweenInt?.call(column, from, to);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String column, String value, bool invert)? equals,
    TResult Function(String column, String value, bool orEquals)? greaterThan,
    TResult Function(String column, bool invert)? isNull,
    TResult Function(String column, int from, int to)? betweenInt,
    TResult Function(String column, bool invert, IList<String> values)? isIn,
    required TResult orElse(),
  }) {
    if (betweenInt != null) {
      return betweenInt(column, from, to);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_FilterWithEquals value) equals,
    required TResult Function(_FilterWithGreaterThan value) greaterThan,
    required TResult Function(_FilterWithIsNull value) isNull,
    required TResult Function(_FilterWithBetweenInt value) betweenInt,
    required TResult Function(_FilterWithIsIn value) isIn,
  }) {
    return betweenInt(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_FilterWithEquals value)? equals,
    TResult? Function(_FilterWithGreaterThan value)? greaterThan,
    TResult? Function(_FilterWithIsNull value)? isNull,
    TResult? Function(_FilterWithBetweenInt value)? betweenInt,
    TResult? Function(_FilterWithIsIn value)? isIn,
  }) {
    return betweenInt?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_FilterWithEquals value)? equals,
    TResult Function(_FilterWithGreaterThan value)? greaterThan,
    TResult Function(_FilterWithIsNull value)? isNull,
    TResult Function(_FilterWithBetweenInt value)? betweenInt,
    TResult Function(_FilterWithIsIn value)? isIn,
    required TResult orElse(),
  }) {
    if (betweenInt != null) {
      return betweenInt(this);
    }
    return orElse();
  }

  @override
  Map<String, dynamic> toJson() {
    return _$$_FilterWithBetweenIntToJson(
      this,
    );
  }
}

abstract class _FilterWithBetweenInt implements FilterWith {
  const factory _FilterWithBetweenInt(
      {required final String column,
      required final int from,
      required final int to}) = _$_FilterWithBetweenInt;

  factory _FilterWithBetweenInt.fromJson(Map<String, dynamic> json) =
      _$_FilterWithBetweenInt.fromJson;

  @override
  String get column;
  int get from;
  int get to;
  @override
  @JsonKey(ignore: true)
  _$$_FilterWithBetweenIntCopyWith<_$_FilterWithBetweenInt> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$_FilterWithIsInCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$_FilterWithIsInCopyWith(
          _$_FilterWithIsIn value, $Res Function(_$_FilterWithIsIn) then) =
      __$$_FilterWithIsInCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, bool invert, IList<String> values});
}

/// @nodoc
class __$$_FilterWithIsInCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$_FilterWithIsIn>
    implements _$$_FilterWithIsInCopyWith<$Res> {
  __$$_FilterWithIsInCopyWithImpl(
      _$_FilterWithIsIn _value, $Res Function(_$_FilterWithIsIn) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? invert = null,
    Object? values = null,
  }) {
    return _then(_$_FilterWithIsIn(
      column: null == column
          ? _value.column
          : column // ignore: cast_nullable_to_non_nullable
              as String,
      invert: null == invert
          ? _value.invert
          : invert // ignore: cast_nullable_to_non_nullable
              as bool,
      values: null == values
          ? _value.values
          : values // ignore: cast_nullable_to_non_nullable
              as IList<String>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_FilterWithIsIn implements _FilterWithIsIn {
  const _$_FilterWithIsIn(
      {required this.column,
      this.invert = false,
      this.values = const IListConst([]),
      final String? $type})
      : $type = $type ?? 'isIn';

  factory _$_FilterWithIsIn.fromJson(Map<String, dynamic> json) =>
      _$$_FilterWithIsInFromJson(json);

  @override
  final String column;
  @override
  @JsonKey()
  final bool invert;
  @override
  @JsonKey()
  final IList<String> values;

  @JsonKey(name: 'runtimeType')
  final String $type;

  @override
  String toString() {
    return 'FilterWith.isIn(column: $column, invert: $invert, values: $values)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_FilterWithIsIn &&
            (identical(other.column, column) || other.column == column) &&
            (identical(other.invert, invert) || other.invert == invert) &&
            const DeepCollectionEquality().equals(other.values, values));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType, column, invert, const DeepCollectionEquality().hash(values));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_FilterWithIsInCopyWith<_$_FilterWithIsIn> get copyWith =>
      __$$_FilterWithIsInCopyWithImpl<_$_FilterWithIsIn>(this, _$identity);

  @override
  @optionalTypeArgs
  TResult when<TResult extends Object?>({
    required TResult Function(String column, String value, bool invert) equals,
    required TResult Function(String column, String value, bool orEquals)
        greaterThan,
    required TResult Function(String column, bool invert) isNull,
    required TResult Function(String column, int from, int to) betweenInt,
    required TResult Function(String column, bool invert, IList<String> values)
        isIn,
  }) {
    return isIn(column, invert, values);
  }

  @override
  @optionalTypeArgs
  TResult? whenOrNull<TResult extends Object?>({
    TResult? Function(String column, String value, bool invert)? equals,
    TResult? Function(String column, String value, bool orEquals)? greaterThan,
    TResult? Function(String column, bool invert)? isNull,
    TResult? Function(String column, int from, int to)? betweenInt,
    TResult? Function(String column, bool invert, IList<String> values)? isIn,
  }) {
    return isIn?.call(column, invert, values);
  }

  @override
  @optionalTypeArgs
  TResult maybeWhen<TResult extends Object?>({
    TResult Function(String column, String value, bool invert)? equals,
    TResult Function(String column, String value, bool orEquals)? greaterThan,
    TResult Function(String column, bool invert)? isNull,
    TResult Function(String column, int from, int to)? betweenInt,
    TResult Function(String column, bool invert, IList<String> values)? isIn,
    required TResult orElse(),
  }) {
    if (isIn != null) {
      return isIn(column, invert, values);
    }
    return orElse();
  }

  @override
  @optionalTypeArgs
  TResult map<TResult extends Object?>({
    required TResult Function(_FilterWithEquals value) equals,
    required TResult Function(_FilterWithGreaterThan value) greaterThan,
    required TResult Function(_FilterWithIsNull value) isNull,
    required TResult Function(_FilterWithBetweenInt value) betweenInt,
    required TResult Function(_FilterWithIsIn value) isIn,
  }) {
    return isIn(this);
  }

  @override
  @optionalTypeArgs
  TResult? mapOrNull<TResult extends Object?>({
    TResult? Function(_FilterWithEquals value)? equals,
    TResult? Function(_FilterWithGreaterThan value)? greaterThan,
    TResult? Function(_FilterWithIsNull value)? isNull,
    TResult? Function(_FilterWithBetweenInt value)? betweenInt,
    TResult? Function(_FilterWithIsIn value)? isIn,
  }) {
    return isIn?.call(this);
  }

  @override
  @optionalTypeArgs
  TResult maybeMap<TResult extends Object?>({
    TResult Function(_FilterWithEquals value)? equals,
    TResult Function(_FilterWithGreaterThan value)? greaterThan,
    TResult Function(_FilterWithIsNull value)? isNull,
    TResult Function(_FilterWithBetweenInt value)? betweenInt,
    TResult Function(_FilterWithIsIn value)? isIn,
    required TResult orElse(),
  }) {
    if (isIn != null) {
      return isIn(this);
    }
    return orElse();
  }

  @override
  Map<String, dynamic> toJson() {
    return _$$_FilterWithIsInToJson(
      this,
    );
  }
}

abstract class _FilterWithIsIn implements FilterWith {
  const factory _FilterWithIsIn(
      {required final String column,
      final bool invert,
      final IList<String> values}) = _$_FilterWithIsIn;

  factory _FilterWithIsIn.fromJson(Map<String, dynamic> json) =
      _$_FilterWithIsIn.fromJson;

  @override
  String get column;
  bool get invert;
  IList<String> get values;
  @override
  @JsonKey(ignore: true)
  _$$_FilterWithIsInCopyWith<_$_FilterWithIsIn> get copyWith =>
      throw _privateConstructorUsedError;
}

ListQuery _$ListQueryFromJson(Map<String, dynamic> json) {
  return _ListQuery.fromJson(json);
}

/// @nodoc
mixin _$ListQuery {
  Pagination get page => throw _privateConstructorUsedError;
  SortBy? get sort => throw _privateConstructorUsedError;
  IList<FilterWith> get filters => throw _privateConstructorUsedError;

  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;
  @JsonKey(ignore: true)
  $ListQueryCopyWith<ListQuery> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ListQueryCopyWith<$Res> {
  factory $ListQueryCopyWith(ListQuery value, $Res Function(ListQuery) then) =
      _$ListQueryCopyWithImpl<$Res, ListQuery>;
  @useResult
  $Res call({Pagination page, SortBy? sort, IList<FilterWith> filters});

  $PaginationCopyWith<$Res> get page;
  $SortByCopyWith<$Res>? get sort;
}

/// @nodoc
class _$ListQueryCopyWithImpl<$Res, $Val extends ListQuery>
    implements $ListQueryCopyWith<$Res> {
  _$ListQueryCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? page = null,
    Object? sort = freezed,
    Object? filters = null,
  }) {
    return _then(_value.copyWith(
      page: null == page
          ? _value.page
          : page // ignore: cast_nullable_to_non_nullable
              as Pagination,
      sort: freezed == sort
          ? _value.sort
          : sort // ignore: cast_nullable_to_non_nullable
              as SortBy?,
      filters: null == filters
          ? _value.filters
          : filters // ignore: cast_nullable_to_non_nullable
              as IList<FilterWith>,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $PaginationCopyWith<$Res> get page {
    return $PaginationCopyWith<$Res>(_value.page, (value) {
      return _then(_value.copyWith(page: value) as $Val);
    });
  }

  @override
  @pragma('vm:prefer-inline')
  $SortByCopyWith<$Res>? get sort {
    if (_value.sort == null) {
      return null;
    }

    return $SortByCopyWith<$Res>(_value.sort!, (value) {
      return _then(_value.copyWith(sort: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$_ListQueryCopyWith<$Res> implements $ListQueryCopyWith<$Res> {
  factory _$$_ListQueryCopyWith(
          _$_ListQuery value, $Res Function(_$_ListQuery) then) =
      __$$_ListQueryCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({Pagination page, SortBy? sort, IList<FilterWith> filters});

  @override
  $PaginationCopyWith<$Res> get page;
  @override
  $SortByCopyWith<$Res>? get sort;
}

/// @nodoc
class __$$_ListQueryCopyWithImpl<$Res>
    extends _$ListQueryCopyWithImpl<$Res, _$_ListQuery>
    implements _$$_ListQueryCopyWith<$Res> {
  __$$_ListQueryCopyWithImpl(
      _$_ListQuery _value, $Res Function(_$_ListQuery) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? page = null,
    Object? sort = freezed,
    Object? filters = null,
  }) {
    return _then(_$_ListQuery(
      page: null == page
          ? _value.page
          : page // ignore: cast_nullable_to_non_nullable
              as Pagination,
      sort: freezed == sort
          ? _value.sort
          : sort // ignore: cast_nullable_to_non_nullable
              as SortBy?,
      filters: null == filters
          ? _value.filters
          : filters // ignore: cast_nullable_to_non_nullable
              as IList<FilterWith>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$_ListQuery implements _ListQuery {
  const _$_ListQuery(
      {this.page = const Pagination(limit: -1, offset: 0),
      this.sort,
      this.filters = const IListConst([])});

  factory _$_ListQuery.fromJson(Map<String, dynamic> json) =>
      _$$_ListQueryFromJson(json);

  @override
  @JsonKey()
  final Pagination page;
  @override
  final SortBy? sort;
  @override
  @JsonKey()
  final IList<FilterWith> filters;

  @override
  String toString() {
    return 'ListQuery(page: $page, sort: $sort, filters: $filters)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_ListQuery &&
            (identical(other.page, page) || other.page == page) &&
            (identical(other.sort, sort) || other.sort == sort) &&
            const DeepCollectionEquality().equals(other.filters, filters));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(
      runtimeType, page, sort, const DeepCollectionEquality().hash(filters));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_ListQueryCopyWith<_$_ListQuery> get copyWith =>
      __$$_ListQueryCopyWithImpl<_$_ListQuery>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$_ListQueryToJson(
      this,
    );
  }
}

abstract class _ListQuery implements ListQuery {
  const factory _ListQuery(
      {final Pagination page,
      final SortBy? sort,
      final IList<FilterWith> filters}) = _$_ListQuery;

  factory _ListQuery.fromJson(Map<String, dynamic> json) =
      _$_ListQuery.fromJson;

  @override
  Pagination get page;
  @override
  SortBy? get sort;
  @override
  IList<FilterWith> get filters;
  @override
  @JsonKey(ignore: true)
  _$$_ListQueryCopyWith<_$_ListQuery> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$ListQueryOptions {
  IList<String> get sortColumns => throw _privateConstructorUsedError;
  IList<String> get filterColumns => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $ListQueryOptionsCopyWith<ListQueryOptions> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ListQueryOptionsCopyWith<$Res> {
  factory $ListQueryOptionsCopyWith(
          ListQueryOptions value, $Res Function(ListQueryOptions) then) =
      _$ListQueryOptionsCopyWithImpl<$Res, ListQueryOptions>;
  @useResult
  $Res call({IList<String> sortColumns, IList<String> filterColumns});
}

/// @nodoc
class _$ListQueryOptionsCopyWithImpl<$Res, $Val extends ListQueryOptions>
    implements $ListQueryOptionsCopyWith<$Res> {
  _$ListQueryOptionsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sortColumns = null,
    Object? filterColumns = null,
  }) {
    return _then(_value.copyWith(
      sortColumns: null == sortColumns
          ? _value.sortColumns
          : sortColumns // ignore: cast_nullable_to_non_nullable
              as IList<String>,
      filterColumns: null == filterColumns
          ? _value.filterColumns
          : filterColumns // ignore: cast_nullable_to_non_nullable
              as IList<String>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$_ListQueryOptionsCopyWith<$Res>
    implements $ListQueryOptionsCopyWith<$Res> {
  factory _$$_ListQueryOptionsCopyWith(
          _$_ListQueryOptions value, $Res Function(_$_ListQueryOptions) then) =
      __$$_ListQueryOptionsCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({IList<String> sortColumns, IList<String> filterColumns});
}

/// @nodoc
class __$$_ListQueryOptionsCopyWithImpl<$Res>
    extends _$ListQueryOptionsCopyWithImpl<$Res, _$_ListQueryOptions>
    implements _$$_ListQueryOptionsCopyWith<$Res> {
  __$$_ListQueryOptionsCopyWithImpl(
      _$_ListQueryOptions _value, $Res Function(_$_ListQueryOptions) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sortColumns = null,
    Object? filterColumns = null,
  }) {
    return _then(_$_ListQueryOptions(
      sortColumns: null == sortColumns
          ? _value.sortColumns
          : sortColumns // ignore: cast_nullable_to_non_nullable
              as IList<String>,
      filterColumns: null == filterColumns
          ? _value.filterColumns
          : filterColumns // ignore: cast_nullable_to_non_nullable
              as IList<String>,
    ));
  }
}

/// @nodoc

class _$_ListQueryOptions implements _ListQueryOptions {
  const _$_ListQueryOptions(
      {required this.sortColumns, required this.filterColumns});

  @override
  final IList<String> sortColumns;
  @override
  final IList<String> filterColumns;

  @override
  String toString() {
    return 'ListQueryOptions(sortColumns: $sortColumns, filterColumns: $filterColumns)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_ListQueryOptions &&
            const DeepCollectionEquality()
                .equals(other.sortColumns, sortColumns) &&
            const DeepCollectionEquality()
                .equals(other.filterColumns, filterColumns));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(sortColumns),
      const DeepCollectionEquality().hash(filterColumns));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_ListQueryOptionsCopyWith<_$_ListQueryOptions> get copyWith =>
      __$$_ListQueryOptionsCopyWithImpl<_$_ListQueryOptions>(this, _$identity);
}

abstract class _ListQueryOptions implements ListQueryOptions {
  const factory _ListQueryOptions(
      {required final IList<String> sortColumns,
      required final IList<String> filterColumns}) = _$_ListQueryOptions;

  @override
  IList<String> get sortColumns;
  @override
  IList<String> get filterColumns;
  @override
  @JsonKey(ignore: true)
  _$$_ListQueryOptionsCopyWith<_$_ListQueryOptions> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$LibraryListQuery {
  ListQueryOptions get options => throw _privateConstructorUsedError;
  ListQuery get query => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $LibraryListQueryCopyWith<LibraryListQuery> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $LibraryListQueryCopyWith<$Res> {
  factory $LibraryListQueryCopyWith(
          LibraryListQuery value, $Res Function(LibraryListQuery) then) =
      _$LibraryListQueryCopyWithImpl<$Res, LibraryListQuery>;
  @useResult
  $Res call({ListQueryOptions options, ListQuery query});

  $ListQueryOptionsCopyWith<$Res> get options;
  $ListQueryCopyWith<$Res> get query;
}

/// @nodoc
class _$LibraryListQueryCopyWithImpl<$Res, $Val extends LibraryListQuery>
    implements $LibraryListQueryCopyWith<$Res> {
  _$LibraryListQueryCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? options = null,
    Object? query = null,
  }) {
    return _then(_value.copyWith(
      options: null == options
          ? _value.options
          : options // ignore: cast_nullable_to_non_nullable
              as ListQueryOptions,
      query: null == query
          ? _value.query
          : query // ignore: cast_nullable_to_non_nullable
              as ListQuery,
    ) as $Val);
  }

  @override
  @pragma('vm:prefer-inline')
  $ListQueryOptionsCopyWith<$Res> get options {
    return $ListQueryOptionsCopyWith<$Res>(_value.options, (value) {
      return _then(_value.copyWith(options: value) as $Val);
    });
  }

  @override
  @pragma('vm:prefer-inline')
  $ListQueryCopyWith<$Res> get query {
    return $ListQueryCopyWith<$Res>(_value.query, (value) {
      return _then(_value.copyWith(query: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$_LibraryListQueryCopyWith<$Res>
    implements $LibraryListQueryCopyWith<$Res> {
  factory _$$_LibraryListQueryCopyWith(
          _$_LibraryListQuery value, $Res Function(_$_LibraryListQuery) then) =
      __$$_LibraryListQueryCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({ListQueryOptions options, ListQuery query});

  @override
  $ListQueryOptionsCopyWith<$Res> get options;
  @override
  $ListQueryCopyWith<$Res> get query;
}

/// @nodoc
class __$$_LibraryListQueryCopyWithImpl<$Res>
    extends _$LibraryListQueryCopyWithImpl<$Res, _$_LibraryListQuery>
    implements _$$_LibraryListQueryCopyWith<$Res> {
  __$$_LibraryListQueryCopyWithImpl(
      _$_LibraryListQuery _value, $Res Function(_$_LibraryListQuery) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? options = null,
    Object? query = null,
  }) {
    return _then(_$_LibraryListQuery(
      options: null == options
          ? _value.options
          : options // ignore: cast_nullable_to_non_nullable
              as ListQueryOptions,
      query: null == query
          ? _value.query
          : query // ignore: cast_nullable_to_non_nullable
              as ListQuery,
    ));
  }
}

/// @nodoc

class _$_LibraryListQuery implements _LibraryListQuery {
  const _$_LibraryListQuery({required this.options, required this.query});

  @override
  final ListQueryOptions options;
  @override
  final ListQuery query;

  @override
  String toString() {
    return 'LibraryListQuery(options: $options, query: $query)';
  }

  @override
  bool operator ==(dynamic other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$_LibraryListQuery &&
            (identical(other.options, options) || other.options == options) &&
            (identical(other.query, query) || other.query == query));
  }

  @override
  int get hashCode => Object.hash(runtimeType, options, query);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$_LibraryListQueryCopyWith<_$_LibraryListQuery> get copyWith =>
      __$$_LibraryListQueryCopyWithImpl<_$_LibraryListQuery>(this, _$identity);
}

abstract class _LibraryListQuery implements LibraryListQuery {
  const factory _LibraryListQuery(
      {required final ListQueryOptions options,
      required final ListQuery query}) = _$_LibraryListQuery;

  @override
  ListQueryOptions get options;
  @override
  ListQuery get query;
  @override
  @JsonKey(ignore: true)
  _$$_LibraryListQueryCopyWith<_$_LibraryListQuery> get copyWith =>
      throw _privateConstructorUsedError;
}
