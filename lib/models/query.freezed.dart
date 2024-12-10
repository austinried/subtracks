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
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

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
abstract class _$$PaginationImplCopyWith<$Res>
    implements $PaginationCopyWith<$Res> {
  factory _$$PaginationImplCopyWith(
          _$PaginationImpl value, $Res Function(_$PaginationImpl) then) =
      __$$PaginationImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int limit, int offset});
}

/// @nodoc
class __$$PaginationImplCopyWithImpl<$Res>
    extends _$PaginationCopyWithImpl<$Res, _$PaginationImpl>
    implements _$$PaginationImplCopyWith<$Res> {
  __$$PaginationImplCopyWithImpl(
      _$PaginationImpl _value, $Res Function(_$PaginationImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? limit = null,
    Object? offset = null,
  }) {
    return _then(_$PaginationImpl(
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
class _$PaginationImpl implements _Pagination {
  const _$PaginationImpl({required this.limit, this.offset = 0});

  factory _$PaginationImpl.fromJson(Map<String, dynamic> json) =>
      _$$PaginationImplFromJson(json);

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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$PaginationImpl &&
            (identical(other.limit, limit) || other.limit == limit) &&
            (identical(other.offset, offset) || other.offset == offset));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, limit, offset);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$PaginationImplCopyWith<_$PaginationImpl> get copyWith =>
      __$$PaginationImplCopyWithImpl<_$PaginationImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$PaginationImplToJson(
      this,
    );
  }
}

abstract class _Pagination implements Pagination {
  const factory _Pagination({required final int limit, final int offset}) =
      _$PaginationImpl;

  factory _Pagination.fromJson(Map<String, dynamic> json) =
      _$PaginationImpl.fromJson;

  @override
  int get limit;
  @override
  int get offset;
  @override
  @JsonKey(ignore: true)
  _$$PaginationImplCopyWith<_$PaginationImpl> get copyWith =>
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
abstract class _$$SortByImplCopyWith<$Res> implements $SortByCopyWith<$Res> {
  factory _$$SortByImplCopyWith(
          _$SortByImpl value, $Res Function(_$SortByImpl) then) =
      __$$SortByImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, SortDirection dir});
}

/// @nodoc
class __$$SortByImplCopyWithImpl<$Res>
    extends _$SortByCopyWithImpl<$Res, _$SortByImpl>
    implements _$$SortByImplCopyWith<$Res> {
  __$$SortByImplCopyWithImpl(
      _$SortByImpl _value, $Res Function(_$SortByImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? dir = null,
  }) {
    return _then(_$SortByImpl(
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
class _$SortByImpl implements _SortBy {
  const _$SortByImpl({required this.column, this.dir = SortDirection.asc});

  factory _$SortByImpl.fromJson(Map<String, dynamic> json) =>
      _$$SortByImplFromJson(json);

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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SortByImpl &&
            (identical(other.column, column) || other.column == column) &&
            (identical(other.dir, dir) || other.dir == dir));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, column, dir);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SortByImplCopyWith<_$SortByImpl> get copyWith =>
      __$$SortByImplCopyWithImpl<_$SortByImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$SortByImplToJson(
      this,
    );
  }
}

abstract class _SortBy implements SortBy {
  const factory _SortBy(
      {required final String column, final SortDirection dir}) = _$SortByImpl;

  factory _SortBy.fromJson(Map<String, dynamic> json) = _$SortByImpl.fromJson;

  @override
  String get column;
  @override
  SortDirection get dir;
  @override
  @JsonKey(ignore: true)
  _$$SortByImplCopyWith<_$SortByImpl> get copyWith =>
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
abstract class _$$FilterWithEqualsImplCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$FilterWithEqualsImplCopyWith(_$FilterWithEqualsImpl value,
          $Res Function(_$FilterWithEqualsImpl) then) =
      __$$FilterWithEqualsImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, String value, bool invert});
}

/// @nodoc
class __$$FilterWithEqualsImplCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$FilterWithEqualsImpl>
    implements _$$FilterWithEqualsImplCopyWith<$Res> {
  __$$FilterWithEqualsImplCopyWithImpl(_$FilterWithEqualsImpl _value,
      $Res Function(_$FilterWithEqualsImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? value = null,
    Object? invert = null,
  }) {
    return _then(_$FilterWithEqualsImpl(
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
class _$FilterWithEqualsImpl implements _FilterWithEquals {
  const _$FilterWithEqualsImpl(
      {required this.column,
      required this.value,
      this.invert = false,
      final String? $type})
      : $type = $type ?? 'equals';

  factory _$FilterWithEqualsImpl.fromJson(Map<String, dynamic> json) =>
      _$$FilterWithEqualsImplFromJson(json);

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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FilterWithEqualsImpl &&
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
  _$$FilterWithEqualsImplCopyWith<_$FilterWithEqualsImpl> get copyWith =>
      __$$FilterWithEqualsImplCopyWithImpl<_$FilterWithEqualsImpl>(
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
    return _$$FilterWithEqualsImplToJson(
      this,
    );
  }
}

abstract class _FilterWithEquals implements FilterWith {
  const factory _FilterWithEquals(
      {required final String column,
      required final String value,
      final bool invert}) = _$FilterWithEqualsImpl;

  factory _FilterWithEquals.fromJson(Map<String, dynamic> json) =
      _$FilterWithEqualsImpl.fromJson;

  @override
  String get column;
  String get value;
  bool get invert;
  @override
  @JsonKey(ignore: true)
  _$$FilterWithEqualsImplCopyWith<_$FilterWithEqualsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FilterWithGreaterThanImplCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$FilterWithGreaterThanImplCopyWith(
          _$FilterWithGreaterThanImpl value,
          $Res Function(_$FilterWithGreaterThanImpl) then) =
      __$$FilterWithGreaterThanImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, String value, bool orEquals});
}

/// @nodoc
class __$$FilterWithGreaterThanImplCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$FilterWithGreaterThanImpl>
    implements _$$FilterWithGreaterThanImplCopyWith<$Res> {
  __$$FilterWithGreaterThanImplCopyWithImpl(_$FilterWithGreaterThanImpl _value,
      $Res Function(_$FilterWithGreaterThanImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? value = null,
    Object? orEquals = null,
  }) {
    return _then(_$FilterWithGreaterThanImpl(
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
class _$FilterWithGreaterThanImpl implements _FilterWithGreaterThan {
  const _$FilterWithGreaterThanImpl(
      {required this.column,
      required this.value,
      this.orEquals = false,
      final String? $type})
      : $type = $type ?? 'greaterThan';

  factory _$FilterWithGreaterThanImpl.fromJson(Map<String, dynamic> json) =>
      _$$FilterWithGreaterThanImplFromJson(json);

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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FilterWithGreaterThanImpl &&
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
  _$$FilterWithGreaterThanImplCopyWith<_$FilterWithGreaterThanImpl>
      get copyWith => __$$FilterWithGreaterThanImplCopyWithImpl<
          _$FilterWithGreaterThanImpl>(this, _$identity);

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
    return _$$FilterWithGreaterThanImplToJson(
      this,
    );
  }
}

abstract class _FilterWithGreaterThan implements FilterWith {
  const factory _FilterWithGreaterThan(
      {required final String column,
      required final String value,
      final bool orEquals}) = _$FilterWithGreaterThanImpl;

  factory _FilterWithGreaterThan.fromJson(Map<String, dynamic> json) =
      _$FilterWithGreaterThanImpl.fromJson;

  @override
  String get column;
  String get value;
  bool get orEquals;
  @override
  @JsonKey(ignore: true)
  _$$FilterWithGreaterThanImplCopyWith<_$FilterWithGreaterThanImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FilterWithIsNullImplCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$FilterWithIsNullImplCopyWith(_$FilterWithIsNullImpl value,
          $Res Function(_$FilterWithIsNullImpl) then) =
      __$$FilterWithIsNullImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, bool invert});
}

/// @nodoc
class __$$FilterWithIsNullImplCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$FilterWithIsNullImpl>
    implements _$$FilterWithIsNullImplCopyWith<$Res> {
  __$$FilterWithIsNullImplCopyWithImpl(_$FilterWithIsNullImpl _value,
      $Res Function(_$FilterWithIsNullImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? invert = null,
  }) {
    return _then(_$FilterWithIsNullImpl(
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
class _$FilterWithIsNullImpl implements _FilterWithIsNull {
  const _$FilterWithIsNullImpl(
      {required this.column, this.invert = false, final String? $type})
      : $type = $type ?? 'isNull';

  factory _$FilterWithIsNullImpl.fromJson(Map<String, dynamic> json) =>
      _$$FilterWithIsNullImplFromJson(json);

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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FilterWithIsNullImpl &&
            (identical(other.column, column) || other.column == column) &&
            (identical(other.invert, invert) || other.invert == invert));
  }

  @JsonKey(ignore: true)
  @override
  int get hashCode => Object.hash(runtimeType, column, invert);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$FilterWithIsNullImplCopyWith<_$FilterWithIsNullImpl> get copyWith =>
      __$$FilterWithIsNullImplCopyWithImpl<_$FilterWithIsNullImpl>(
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
    return _$$FilterWithIsNullImplToJson(
      this,
    );
  }
}

abstract class _FilterWithIsNull implements FilterWith {
  const factory _FilterWithIsNull(
      {required final String column,
      final bool invert}) = _$FilterWithIsNullImpl;

  factory _FilterWithIsNull.fromJson(Map<String, dynamic> json) =
      _$FilterWithIsNullImpl.fromJson;

  @override
  String get column;
  bool get invert;
  @override
  @JsonKey(ignore: true)
  _$$FilterWithIsNullImplCopyWith<_$FilterWithIsNullImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FilterWithBetweenIntImplCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$FilterWithBetweenIntImplCopyWith(_$FilterWithBetweenIntImpl value,
          $Res Function(_$FilterWithBetweenIntImpl) then) =
      __$$FilterWithBetweenIntImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, int from, int to});
}

/// @nodoc
class __$$FilterWithBetweenIntImplCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$FilterWithBetweenIntImpl>
    implements _$$FilterWithBetweenIntImplCopyWith<$Res> {
  __$$FilterWithBetweenIntImplCopyWithImpl(_$FilterWithBetweenIntImpl _value,
      $Res Function(_$FilterWithBetweenIntImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? from = null,
    Object? to = null,
  }) {
    return _then(_$FilterWithBetweenIntImpl(
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
class _$FilterWithBetweenIntImpl implements _FilterWithBetweenInt {
  const _$FilterWithBetweenIntImpl(
      {required this.column,
      required this.from,
      required this.to,
      final String? $type})
      : $type = $type ?? 'betweenInt';

  factory _$FilterWithBetweenIntImpl.fromJson(Map<String, dynamic> json) =>
      _$$FilterWithBetweenIntImplFromJson(json);

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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FilterWithBetweenIntImpl &&
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
  _$$FilterWithBetweenIntImplCopyWith<_$FilterWithBetweenIntImpl>
      get copyWith =>
          __$$FilterWithBetweenIntImplCopyWithImpl<_$FilterWithBetweenIntImpl>(
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
    return _$$FilterWithBetweenIntImplToJson(
      this,
    );
  }
}

abstract class _FilterWithBetweenInt implements FilterWith {
  const factory _FilterWithBetweenInt(
      {required final String column,
      required final int from,
      required final int to}) = _$FilterWithBetweenIntImpl;

  factory _FilterWithBetweenInt.fromJson(Map<String, dynamic> json) =
      _$FilterWithBetweenIntImpl.fromJson;

  @override
  String get column;
  int get from;
  int get to;
  @override
  @JsonKey(ignore: true)
  _$$FilterWithBetweenIntImplCopyWith<_$FilterWithBetweenIntImpl>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class _$$FilterWithIsInImplCopyWith<$Res>
    implements $FilterWithCopyWith<$Res> {
  factory _$$FilterWithIsInImplCopyWith(_$FilterWithIsInImpl value,
          $Res Function(_$FilterWithIsInImpl) then) =
      __$$FilterWithIsInImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String column, bool invert, IList<String> values});
}

/// @nodoc
class __$$FilterWithIsInImplCopyWithImpl<$Res>
    extends _$FilterWithCopyWithImpl<$Res, _$FilterWithIsInImpl>
    implements _$$FilterWithIsInImplCopyWith<$Res> {
  __$$FilterWithIsInImplCopyWithImpl(
      _$FilterWithIsInImpl _value, $Res Function(_$FilterWithIsInImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? column = null,
    Object? invert = null,
    Object? values = null,
  }) {
    return _then(_$FilterWithIsInImpl(
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
class _$FilterWithIsInImpl implements _FilterWithIsIn {
  const _$FilterWithIsInImpl(
      {required this.column,
      this.invert = false,
      this.values = const IListConst([]),
      final String? $type})
      : $type = $type ?? 'isIn';

  factory _$FilterWithIsInImpl.fromJson(Map<String, dynamic> json) =>
      _$$FilterWithIsInImplFromJson(json);

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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$FilterWithIsInImpl &&
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
  _$$FilterWithIsInImplCopyWith<_$FilterWithIsInImpl> get copyWith =>
      __$$FilterWithIsInImplCopyWithImpl<_$FilterWithIsInImpl>(
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
    return _$$FilterWithIsInImplToJson(
      this,
    );
  }
}

abstract class _FilterWithIsIn implements FilterWith {
  const factory _FilterWithIsIn(
      {required final String column,
      final bool invert,
      final IList<String> values}) = _$FilterWithIsInImpl;

  factory _FilterWithIsIn.fromJson(Map<String, dynamic> json) =
      _$FilterWithIsInImpl.fromJson;

  @override
  String get column;
  bool get invert;
  IList<String> get values;
  @override
  @JsonKey(ignore: true)
  _$$FilterWithIsInImplCopyWith<_$FilterWithIsInImpl> get copyWith =>
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
abstract class _$$ListQueryImplCopyWith<$Res>
    implements $ListQueryCopyWith<$Res> {
  factory _$$ListQueryImplCopyWith(
          _$ListQueryImpl value, $Res Function(_$ListQueryImpl) then) =
      __$$ListQueryImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({Pagination page, SortBy? sort, IList<FilterWith> filters});

  @override
  $PaginationCopyWith<$Res> get page;
  @override
  $SortByCopyWith<$Res>? get sort;
}

/// @nodoc
class __$$ListQueryImplCopyWithImpl<$Res>
    extends _$ListQueryCopyWithImpl<$Res, _$ListQueryImpl>
    implements _$$ListQueryImplCopyWith<$Res> {
  __$$ListQueryImplCopyWithImpl(
      _$ListQueryImpl _value, $Res Function(_$ListQueryImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? page = null,
    Object? sort = freezed,
    Object? filters = null,
  }) {
    return _then(_$ListQueryImpl(
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
class _$ListQueryImpl implements _ListQuery {
  const _$ListQueryImpl(
      {this.page = const Pagination(limit: -1, offset: 0),
      this.sort,
      this.filters = const IListConst([])});

  factory _$ListQueryImpl.fromJson(Map<String, dynamic> json) =>
      _$$ListQueryImplFromJson(json);

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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ListQueryImpl &&
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
  _$$ListQueryImplCopyWith<_$ListQueryImpl> get copyWith =>
      __$$ListQueryImplCopyWithImpl<_$ListQueryImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ListQueryImplToJson(
      this,
    );
  }
}

abstract class _ListQuery implements ListQuery {
  const factory _ListQuery(
      {final Pagination page,
      final SortBy? sort,
      final IList<FilterWith> filters}) = _$ListQueryImpl;

  factory _ListQuery.fromJson(Map<String, dynamic> json) =
      _$ListQueryImpl.fromJson;

  @override
  Pagination get page;
  @override
  SortBy? get sort;
  @override
  IList<FilterWith> get filters;
  @override
  @JsonKey(ignore: true)
  _$$ListQueryImplCopyWith<_$ListQueryImpl> get copyWith =>
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
abstract class _$$ListQueryOptionsImplCopyWith<$Res>
    implements $ListQueryOptionsCopyWith<$Res> {
  factory _$$ListQueryOptionsImplCopyWith(_$ListQueryOptionsImpl value,
          $Res Function(_$ListQueryOptionsImpl) then) =
      __$$ListQueryOptionsImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({IList<String> sortColumns, IList<String> filterColumns});
}

/// @nodoc
class __$$ListQueryOptionsImplCopyWithImpl<$Res>
    extends _$ListQueryOptionsCopyWithImpl<$Res, _$ListQueryOptionsImpl>
    implements _$$ListQueryOptionsImplCopyWith<$Res> {
  __$$ListQueryOptionsImplCopyWithImpl(_$ListQueryOptionsImpl _value,
      $Res Function(_$ListQueryOptionsImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sortColumns = null,
    Object? filterColumns = null,
  }) {
    return _then(_$ListQueryOptionsImpl(
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

class _$ListQueryOptionsImpl implements _ListQueryOptions {
  const _$ListQueryOptionsImpl(
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
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ListQueryOptionsImpl &&
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
  _$$ListQueryOptionsImplCopyWith<_$ListQueryOptionsImpl> get copyWith =>
      __$$ListQueryOptionsImplCopyWithImpl<_$ListQueryOptionsImpl>(
          this, _$identity);
}

abstract class _ListQueryOptions implements ListQueryOptions {
  const factory _ListQueryOptions(
      {required final IList<String> sortColumns,
      required final IList<String> filterColumns}) = _$ListQueryOptionsImpl;

  @override
  IList<String> get sortColumns;
  @override
  IList<String> get filterColumns;
  @override
  @JsonKey(ignore: true)
  _$$ListQueryOptionsImplCopyWith<_$ListQueryOptionsImpl> get copyWith =>
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
abstract class _$$LibraryListQueryImplCopyWith<$Res>
    implements $LibraryListQueryCopyWith<$Res> {
  factory _$$LibraryListQueryImplCopyWith(_$LibraryListQueryImpl value,
          $Res Function(_$LibraryListQueryImpl) then) =
      __$$LibraryListQueryImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({ListQueryOptions options, ListQuery query});

  @override
  $ListQueryOptionsCopyWith<$Res> get options;
  @override
  $ListQueryCopyWith<$Res> get query;
}

/// @nodoc
class __$$LibraryListQueryImplCopyWithImpl<$Res>
    extends _$LibraryListQueryCopyWithImpl<$Res, _$LibraryListQueryImpl>
    implements _$$LibraryListQueryImplCopyWith<$Res> {
  __$$LibraryListQueryImplCopyWithImpl(_$LibraryListQueryImpl _value,
      $Res Function(_$LibraryListQueryImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? options = null,
    Object? query = null,
  }) {
    return _then(_$LibraryListQueryImpl(
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

class _$LibraryListQueryImpl implements _LibraryListQuery {
  const _$LibraryListQueryImpl({required this.options, required this.query});

  @override
  final ListQueryOptions options;
  @override
  final ListQuery query;

  @override
  String toString() {
    return 'LibraryListQuery(options: $options, query: $query)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$LibraryListQueryImpl &&
            (identical(other.options, options) || other.options == options) &&
            (identical(other.query, query) || other.query == query));
  }

  @override
  int get hashCode => Object.hash(runtimeType, options, query);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$LibraryListQueryImplCopyWith<_$LibraryListQueryImpl> get copyWith =>
      __$$LibraryListQueryImplCopyWithImpl<_$LibraryListQueryImpl>(
          this, _$identity);
}

abstract class _LibraryListQuery implements LibraryListQuery {
  const factory _LibraryListQuery(
      {required final ListQueryOptions options,
      required final ListQuery query}) = _$LibraryListQueryImpl;

  @override
  ListQueryOptions get options;
  @override
  ListQuery get query;
  @override
  @JsonKey(ignore: true)
  _$$LibraryListQueryImplCopyWith<_$LibraryListQueryImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
