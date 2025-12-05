// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'query.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;
SortingTerm _$SortingTermFromJson(
  Map<String, dynamic> json
) {
        switch (json['runtimeType']) {
                  case 'albums':
          return AlbumsSortingTerm.fromJson(
            json
          );
                case 'artists':
          return ArtistsSortingTerm.fromJson(
            json
          );
                case 'songs':
          return SongsSortingTerm.fromJson(
            json
          );
        
          default:
            throw CheckedFromJsonException(
  json,
  'runtimeType',
  'SortingTerm',
  'Invalid union type "${json['runtimeType']}"!'
);
        }
      
}

/// @nodoc
mixin _$SortingTerm {

 SortDirection get dir; Enum get by;
/// Create a copy of SortingTerm
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SortingTermCopyWith<SortingTerm> get copyWith => _$SortingTermCopyWithImpl<SortingTerm>(this as SortingTerm, _$identity);

  /// Serializes this SortingTerm to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SortingTerm&&(identical(other.dir, dir) || other.dir == dir)&&(identical(other.by, by) || other.by == by));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,dir,by);

@override
String toString() {
  return 'SortingTerm(dir: $dir, by: $by)';
}


}

/// @nodoc
abstract mixin class $SortingTermCopyWith<$Res>  {
  factory $SortingTermCopyWith(SortingTerm value, $Res Function(SortingTerm) _then) = _$SortingTermCopyWithImpl;
@useResult
$Res call({
 SortDirection dir
});




}
/// @nodoc
class _$SortingTermCopyWithImpl<$Res>
    implements $SortingTermCopyWith<$Res> {
  _$SortingTermCopyWithImpl(this._self, this._then);

  final SortingTerm _self;
  final $Res Function(SortingTerm) _then;

/// Create a copy of SortingTerm
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? dir = null,}) {
  return _then(_self.copyWith(
dir: null == dir ? _self.dir : dir // ignore: cast_nullable_to_non_nullable
as SortDirection,
  ));
}

}


/// Adds pattern-matching-related methods to [SortingTerm].
extension SortingTermPatterns on SortingTerm {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>({TResult Function( AlbumsSortingTerm value)?  albums,TResult Function( ArtistsSortingTerm value)?  artists,TResult Function( SongsSortingTerm value)?  songs,required TResult orElse(),}){
final _that = this;
switch (_that) {
case AlbumsSortingTerm() when albums != null:
return albums(_that);case ArtistsSortingTerm() when artists != null:
return artists(_that);case SongsSortingTerm() when songs != null:
return songs(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>({required TResult Function( AlbumsSortingTerm value)  albums,required TResult Function( ArtistsSortingTerm value)  artists,required TResult Function( SongsSortingTerm value)  songs,}){
final _that = this;
switch (_that) {
case AlbumsSortingTerm():
return albums(_that);case ArtistsSortingTerm():
return artists(_that);case SongsSortingTerm():
return songs(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>({TResult? Function( AlbumsSortingTerm value)?  albums,TResult? Function( ArtistsSortingTerm value)?  artists,TResult? Function( SongsSortingTerm value)?  songs,}){
final _that = this;
switch (_that) {
case AlbumsSortingTerm() when albums != null:
return albums(_that);case ArtistsSortingTerm() when artists != null:
return artists(_that);case SongsSortingTerm() when songs != null:
return songs(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>({TResult Function( SortDirection dir,  AlbumsColumn by)?  albums,TResult Function( SortDirection dir,  ArtistsColumn by)?  artists,TResult Function( SortDirection dir,  SongsColumn by)?  songs,required TResult orElse(),}) {final _that = this;
switch (_that) {
case AlbumsSortingTerm() when albums != null:
return albums(_that.dir,_that.by);case ArtistsSortingTerm() when artists != null:
return artists(_that.dir,_that.by);case SongsSortingTerm() when songs != null:
return songs(_that.dir,_that.by);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>({required TResult Function( SortDirection dir,  AlbumsColumn by)  albums,required TResult Function( SortDirection dir,  ArtistsColumn by)  artists,required TResult Function( SortDirection dir,  SongsColumn by)  songs,}) {final _that = this;
switch (_that) {
case AlbumsSortingTerm():
return albums(_that.dir,_that.by);case ArtistsSortingTerm():
return artists(_that.dir,_that.by);case SongsSortingTerm():
return songs(_that.dir,_that.by);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>({TResult? Function( SortDirection dir,  AlbumsColumn by)?  albums,TResult? Function( SortDirection dir,  ArtistsColumn by)?  artists,TResult? Function( SortDirection dir,  SongsColumn by)?  songs,}) {final _that = this;
switch (_that) {
case AlbumsSortingTerm() when albums != null:
return albums(_that.dir,_that.by);case ArtistsSortingTerm() when artists != null:
return artists(_that.dir,_that.by);case SongsSortingTerm() when songs != null:
return songs(_that.dir,_that.by);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class AlbumsSortingTerm implements SortingTerm {
  const AlbumsSortingTerm({required this.dir, required this.by, final  String? $type}): $type = $type ?? 'albums';
  factory AlbumsSortingTerm.fromJson(Map<String, dynamic> json) => _$AlbumsSortingTermFromJson(json);

@override final  SortDirection dir;
@override final  AlbumsColumn by;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of SortingTerm
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$AlbumsSortingTermCopyWith<AlbumsSortingTerm> get copyWith => _$AlbumsSortingTermCopyWithImpl<AlbumsSortingTerm>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$AlbumsSortingTermToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is AlbumsSortingTerm&&(identical(other.dir, dir) || other.dir == dir)&&(identical(other.by, by) || other.by == by));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,dir,by);

@override
String toString() {
  return 'SortingTerm.albums(dir: $dir, by: $by)';
}


}

/// @nodoc
abstract mixin class $AlbumsSortingTermCopyWith<$Res> implements $SortingTermCopyWith<$Res> {
  factory $AlbumsSortingTermCopyWith(AlbumsSortingTerm value, $Res Function(AlbumsSortingTerm) _then) = _$AlbumsSortingTermCopyWithImpl;
@override @useResult
$Res call({
 SortDirection dir, AlbumsColumn by
});




}
/// @nodoc
class _$AlbumsSortingTermCopyWithImpl<$Res>
    implements $AlbumsSortingTermCopyWith<$Res> {
  _$AlbumsSortingTermCopyWithImpl(this._self, this._then);

  final AlbumsSortingTerm _self;
  final $Res Function(AlbumsSortingTerm) _then;

/// Create a copy of SortingTerm
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? dir = null,Object? by = null,}) {
  return _then(AlbumsSortingTerm(
dir: null == dir ? _self.dir : dir // ignore: cast_nullable_to_non_nullable
as SortDirection,by: null == by ? _self.by : by // ignore: cast_nullable_to_non_nullable
as AlbumsColumn,
  ));
}


}

/// @nodoc
@JsonSerializable()

class ArtistsSortingTerm implements SortingTerm {
  const ArtistsSortingTerm({required this.dir, required this.by, final  String? $type}): $type = $type ?? 'artists';
  factory ArtistsSortingTerm.fromJson(Map<String, dynamic> json) => _$ArtistsSortingTermFromJson(json);

@override final  SortDirection dir;
@override final  ArtistsColumn by;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of SortingTerm
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$ArtistsSortingTermCopyWith<ArtistsSortingTerm> get copyWith => _$ArtistsSortingTermCopyWithImpl<ArtistsSortingTerm>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$ArtistsSortingTermToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is ArtistsSortingTerm&&(identical(other.dir, dir) || other.dir == dir)&&(identical(other.by, by) || other.by == by));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,dir,by);

@override
String toString() {
  return 'SortingTerm.artists(dir: $dir, by: $by)';
}


}

/// @nodoc
abstract mixin class $ArtistsSortingTermCopyWith<$Res> implements $SortingTermCopyWith<$Res> {
  factory $ArtistsSortingTermCopyWith(ArtistsSortingTerm value, $Res Function(ArtistsSortingTerm) _then) = _$ArtistsSortingTermCopyWithImpl;
@override @useResult
$Res call({
 SortDirection dir, ArtistsColumn by
});




}
/// @nodoc
class _$ArtistsSortingTermCopyWithImpl<$Res>
    implements $ArtistsSortingTermCopyWith<$Res> {
  _$ArtistsSortingTermCopyWithImpl(this._self, this._then);

  final ArtistsSortingTerm _self;
  final $Res Function(ArtistsSortingTerm) _then;

/// Create a copy of SortingTerm
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? dir = null,Object? by = null,}) {
  return _then(ArtistsSortingTerm(
dir: null == dir ? _self.dir : dir // ignore: cast_nullable_to_non_nullable
as SortDirection,by: null == by ? _self.by : by // ignore: cast_nullable_to_non_nullable
as ArtistsColumn,
  ));
}


}

/// @nodoc
@JsonSerializable()

class SongsSortingTerm implements SortingTerm {
  const SongsSortingTerm({required this.dir, required this.by, final  String? $type}): $type = $type ?? 'songs';
  factory SongsSortingTerm.fromJson(Map<String, dynamic> json) => _$SongsSortingTermFromJson(json);

@override final  SortDirection dir;
@override final  SongsColumn by;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of SortingTerm
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SongsSortingTermCopyWith<SongsSortingTerm> get copyWith => _$SongsSortingTermCopyWithImpl<SongsSortingTerm>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$SongsSortingTermToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SongsSortingTerm&&(identical(other.dir, dir) || other.dir == dir)&&(identical(other.by, by) || other.by == by));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,dir,by);

@override
String toString() {
  return 'SortingTerm.songs(dir: $dir, by: $by)';
}


}

/// @nodoc
abstract mixin class $SongsSortingTermCopyWith<$Res> implements $SortingTermCopyWith<$Res> {
  factory $SongsSortingTermCopyWith(SongsSortingTerm value, $Res Function(SongsSortingTerm) _then) = _$SongsSortingTermCopyWithImpl;
@override @useResult
$Res call({
 SortDirection dir, SongsColumn by
});




}
/// @nodoc
class _$SongsSortingTermCopyWithImpl<$Res>
    implements $SongsSortingTermCopyWith<$Res> {
  _$SongsSortingTermCopyWithImpl(this._self, this._then);

  final SongsSortingTerm _self;
  final $Res Function(SongsSortingTerm) _then;

/// Create a copy of SortingTerm
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? dir = null,Object? by = null,}) {
  return _then(SongsSortingTerm(
dir: null == dir ? _self.dir : dir // ignore: cast_nullable_to_non_nullable
as SortDirection,by: null == by ? _self.by : by // ignore: cast_nullable_to_non_nullable
as SongsColumn,
  ));
}


}


/// @nodoc
mixin _$AlbumsQuery {

 int get sourceId; IList<AlbumsFilter> get filter; IList<AlbumsSortingTerm> get sort; int? get limit; int? get offset;
/// Create a copy of AlbumsQuery
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$AlbumsQueryCopyWith<AlbumsQuery> get copyWith => _$AlbumsQueryCopyWithImpl<AlbumsQuery>(this as AlbumsQuery, _$identity);

  /// Serializes this AlbumsQuery to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is AlbumsQuery&&(identical(other.sourceId, sourceId) || other.sourceId == sourceId)&&const DeepCollectionEquality().equals(other.filter, filter)&&const DeepCollectionEquality().equals(other.sort, sort)&&(identical(other.limit, limit) || other.limit == limit)&&(identical(other.offset, offset) || other.offset == offset));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,sourceId,const DeepCollectionEquality().hash(filter),const DeepCollectionEquality().hash(sort),limit,offset);

@override
String toString() {
  return 'AlbumsQuery(sourceId: $sourceId, filter: $filter, sort: $sort, limit: $limit, offset: $offset)';
}


}

/// @nodoc
abstract mixin class $AlbumsQueryCopyWith<$Res>  {
  factory $AlbumsQueryCopyWith(AlbumsQuery value, $Res Function(AlbumsQuery) _then) = _$AlbumsQueryCopyWithImpl;
@useResult
$Res call({
 int sourceId, IList<AlbumsFilter> filter, IList<AlbumsSortingTerm> sort, int? limit, int? offset
});




}
/// @nodoc
class _$AlbumsQueryCopyWithImpl<$Res>
    implements $AlbumsQueryCopyWith<$Res> {
  _$AlbumsQueryCopyWithImpl(this._self, this._then);

  final AlbumsQuery _self;
  final $Res Function(AlbumsQuery) _then;

/// Create a copy of AlbumsQuery
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? sourceId = null,Object? filter = null,Object? sort = null,Object? limit = freezed,Object? offset = freezed,}) {
  return _then(_self.copyWith(
sourceId: null == sourceId ? _self.sourceId : sourceId // ignore: cast_nullable_to_non_nullable
as int,filter: null == filter ? _self.filter : filter // ignore: cast_nullable_to_non_nullable
as IList<AlbumsFilter>,sort: null == sort ? _self.sort : sort // ignore: cast_nullable_to_non_nullable
as IList<AlbumsSortingTerm>,limit: freezed == limit ? _self.limit : limit // ignore: cast_nullable_to_non_nullable
as int?,offset: freezed == offset ? _self.offset : offset // ignore: cast_nullable_to_non_nullable
as int?,
  ));
}

}


/// Adds pattern-matching-related methods to [AlbumsQuery].
extension AlbumsQueryPatterns on AlbumsQuery {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _AlbumsQuery value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _AlbumsQuery() when $default != null:
return $default(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _AlbumsQuery value)  $default,){
final _that = this;
switch (_that) {
case _AlbumsQuery():
return $default(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _AlbumsQuery value)?  $default,){
final _that = this;
switch (_that) {
case _AlbumsQuery() when $default != null:
return $default(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( int sourceId,  IList<AlbumsFilter> filter,  IList<AlbumsSortingTerm> sort,  int? limit,  int? offset)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _AlbumsQuery() when $default != null:
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( int sourceId,  IList<AlbumsFilter> filter,  IList<AlbumsSortingTerm> sort,  int? limit,  int? offset)  $default,) {final _that = this;
switch (_that) {
case _AlbumsQuery():
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( int sourceId,  IList<AlbumsFilter> filter,  IList<AlbumsSortingTerm> sort,  int? limit,  int? offset)?  $default,) {final _that = this;
switch (_that) {
case _AlbumsQuery() when $default != null:
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _AlbumsQuery implements AlbumsQuery {
  const _AlbumsQuery({required this.sourceId, this.filter = const IListConst([]), required this.sort, this.limit, this.offset});
  factory _AlbumsQuery.fromJson(Map<String, dynamic> json) => _$AlbumsQueryFromJson(json);

@override final  int sourceId;
@override@JsonKey() final  IList<AlbumsFilter> filter;
@override final  IList<AlbumsSortingTerm> sort;
@override final  int? limit;
@override final  int? offset;

/// Create a copy of AlbumsQuery
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$AlbumsQueryCopyWith<_AlbumsQuery> get copyWith => __$AlbumsQueryCopyWithImpl<_AlbumsQuery>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$AlbumsQueryToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _AlbumsQuery&&(identical(other.sourceId, sourceId) || other.sourceId == sourceId)&&const DeepCollectionEquality().equals(other.filter, filter)&&const DeepCollectionEquality().equals(other.sort, sort)&&(identical(other.limit, limit) || other.limit == limit)&&(identical(other.offset, offset) || other.offset == offset));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,sourceId,const DeepCollectionEquality().hash(filter),const DeepCollectionEquality().hash(sort),limit,offset);

@override
String toString() {
  return 'AlbumsQuery(sourceId: $sourceId, filter: $filter, sort: $sort, limit: $limit, offset: $offset)';
}


}

/// @nodoc
abstract mixin class _$AlbumsQueryCopyWith<$Res> implements $AlbumsQueryCopyWith<$Res> {
  factory _$AlbumsQueryCopyWith(_AlbumsQuery value, $Res Function(_AlbumsQuery) _then) = __$AlbumsQueryCopyWithImpl;
@override @useResult
$Res call({
 int sourceId, IList<AlbumsFilter> filter, IList<AlbumsSortingTerm> sort, int? limit, int? offset
});




}
/// @nodoc
class __$AlbumsQueryCopyWithImpl<$Res>
    implements _$AlbumsQueryCopyWith<$Res> {
  __$AlbumsQueryCopyWithImpl(this._self, this._then);

  final _AlbumsQuery _self;
  final $Res Function(_AlbumsQuery) _then;

/// Create a copy of AlbumsQuery
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? sourceId = null,Object? filter = null,Object? sort = null,Object? limit = freezed,Object? offset = freezed,}) {
  return _then(_AlbumsQuery(
sourceId: null == sourceId ? _self.sourceId : sourceId // ignore: cast_nullable_to_non_nullable
as int,filter: null == filter ? _self.filter : filter // ignore: cast_nullable_to_non_nullable
as IList<AlbumsFilter>,sort: null == sort ? _self.sort : sort // ignore: cast_nullable_to_non_nullable
as IList<AlbumsSortingTerm>,limit: freezed == limit ? _self.limit : limit // ignore: cast_nullable_to_non_nullable
as int?,offset: freezed == offset ? _self.offset : offset // ignore: cast_nullable_to_non_nullable
as int?,
  ));
}


}

AlbumsFilter _$AlbumsFilterFromJson(
  Map<String, dynamic> json
) {
        switch (json['runtimeType']) {
                  case 'artistId':
          return AlbumsFilterArtistId.fromJson(
            json
          );
                case 'yearEquals':
          return AlbumsFilterYearEquals.fromJson(
            json
          );
        
          default:
            throw CheckedFromJsonException(
  json,
  'runtimeType',
  'AlbumsFilter',
  'Invalid union type "${json['runtimeType']}"!'
);
        }
      
}

/// @nodoc
mixin _$AlbumsFilter {



  /// Serializes this AlbumsFilter to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is AlbumsFilter);
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => runtimeType.hashCode;

@override
String toString() {
  return 'AlbumsFilter()';
}


}

/// @nodoc
class $AlbumsFilterCopyWith<$Res>  {
$AlbumsFilterCopyWith(AlbumsFilter _, $Res Function(AlbumsFilter) __);
}


/// Adds pattern-matching-related methods to [AlbumsFilter].
extension AlbumsFilterPatterns on AlbumsFilter {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>({TResult Function( AlbumsFilterArtistId value)?  artistId,TResult Function( AlbumsFilterYearEquals value)?  yearEquals,required TResult orElse(),}){
final _that = this;
switch (_that) {
case AlbumsFilterArtistId() when artistId != null:
return artistId(_that);case AlbumsFilterYearEquals() when yearEquals != null:
return yearEquals(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>({required TResult Function( AlbumsFilterArtistId value)  artistId,required TResult Function( AlbumsFilterYearEquals value)  yearEquals,}){
final _that = this;
switch (_that) {
case AlbumsFilterArtistId():
return artistId(_that);case AlbumsFilterYearEquals():
return yearEquals(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>({TResult? Function( AlbumsFilterArtistId value)?  artistId,TResult? Function( AlbumsFilterYearEquals value)?  yearEquals,}){
final _that = this;
switch (_that) {
case AlbumsFilterArtistId() when artistId != null:
return artistId(_that);case AlbumsFilterYearEquals() when yearEquals != null:
return yearEquals(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>({TResult Function( String artistId)?  artistId,TResult Function( int year)?  yearEquals,required TResult orElse(),}) {final _that = this;
switch (_that) {
case AlbumsFilterArtistId() when artistId != null:
return artistId(_that.artistId);case AlbumsFilterYearEquals() when yearEquals != null:
return yearEquals(_that.year);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>({required TResult Function( String artistId)  artistId,required TResult Function( int year)  yearEquals,}) {final _that = this;
switch (_that) {
case AlbumsFilterArtistId():
return artistId(_that.artistId);case AlbumsFilterYearEquals():
return yearEquals(_that.year);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>({TResult? Function( String artistId)?  artistId,TResult? Function( int year)?  yearEquals,}) {final _that = this;
switch (_that) {
case AlbumsFilterArtistId() when artistId != null:
return artistId(_that.artistId);case AlbumsFilterYearEquals() when yearEquals != null:
return yearEquals(_that.year);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class AlbumsFilterArtistId implements AlbumsFilter {
  const AlbumsFilterArtistId(this.artistId, {final  String? $type}): $type = $type ?? 'artistId';
  factory AlbumsFilterArtistId.fromJson(Map<String, dynamic> json) => _$AlbumsFilterArtistIdFromJson(json);

 final  String artistId;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of AlbumsFilter
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$AlbumsFilterArtistIdCopyWith<AlbumsFilterArtistId> get copyWith => _$AlbumsFilterArtistIdCopyWithImpl<AlbumsFilterArtistId>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$AlbumsFilterArtistIdToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is AlbumsFilterArtistId&&(identical(other.artistId, artistId) || other.artistId == artistId));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,artistId);

@override
String toString() {
  return 'AlbumsFilter.artistId(artistId: $artistId)';
}


}

/// @nodoc
abstract mixin class $AlbumsFilterArtistIdCopyWith<$Res> implements $AlbumsFilterCopyWith<$Res> {
  factory $AlbumsFilterArtistIdCopyWith(AlbumsFilterArtistId value, $Res Function(AlbumsFilterArtistId) _then) = _$AlbumsFilterArtistIdCopyWithImpl;
@useResult
$Res call({
 String artistId
});




}
/// @nodoc
class _$AlbumsFilterArtistIdCopyWithImpl<$Res>
    implements $AlbumsFilterArtistIdCopyWith<$Res> {
  _$AlbumsFilterArtistIdCopyWithImpl(this._self, this._then);

  final AlbumsFilterArtistId _self;
  final $Res Function(AlbumsFilterArtistId) _then;

/// Create a copy of AlbumsFilter
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') $Res call({Object? artistId = null,}) {
  return _then(AlbumsFilterArtistId(
null == artistId ? _self.artistId : artistId // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}

/// @nodoc
@JsonSerializable()

class AlbumsFilterYearEquals implements AlbumsFilter {
  const AlbumsFilterYearEquals(this.year, {final  String? $type}): $type = $type ?? 'yearEquals';
  factory AlbumsFilterYearEquals.fromJson(Map<String, dynamic> json) => _$AlbumsFilterYearEqualsFromJson(json);

 final  int year;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of AlbumsFilter
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$AlbumsFilterYearEqualsCopyWith<AlbumsFilterYearEquals> get copyWith => _$AlbumsFilterYearEqualsCopyWithImpl<AlbumsFilterYearEquals>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$AlbumsFilterYearEqualsToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is AlbumsFilterYearEquals&&(identical(other.year, year) || other.year == year));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,year);

@override
String toString() {
  return 'AlbumsFilter.yearEquals(year: $year)';
}


}

/// @nodoc
abstract mixin class $AlbumsFilterYearEqualsCopyWith<$Res> implements $AlbumsFilterCopyWith<$Res> {
  factory $AlbumsFilterYearEqualsCopyWith(AlbumsFilterYearEquals value, $Res Function(AlbumsFilterYearEquals) _then) = _$AlbumsFilterYearEqualsCopyWithImpl;
@useResult
$Res call({
 int year
});




}
/// @nodoc
class _$AlbumsFilterYearEqualsCopyWithImpl<$Res>
    implements $AlbumsFilterYearEqualsCopyWith<$Res> {
  _$AlbumsFilterYearEqualsCopyWithImpl(this._self, this._then);

  final AlbumsFilterYearEquals _self;
  final $Res Function(AlbumsFilterYearEquals) _then;

/// Create a copy of AlbumsFilter
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') $Res call({Object? year = null,}) {
  return _then(AlbumsFilterYearEquals(
null == year ? _self.year : year // ignore: cast_nullable_to_non_nullable
as int,
  ));
}


}


/// @nodoc
mixin _$ArtistsQuery {

 int get sourceId; IList<ArtistsFilter> get filter; IList<ArtistsSortingTerm> get sort; int? get limit; int? get offset;
/// Create a copy of ArtistsQuery
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$ArtistsQueryCopyWith<ArtistsQuery> get copyWith => _$ArtistsQueryCopyWithImpl<ArtistsQuery>(this as ArtistsQuery, _$identity);

  /// Serializes this ArtistsQuery to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is ArtistsQuery&&(identical(other.sourceId, sourceId) || other.sourceId == sourceId)&&const DeepCollectionEquality().equals(other.filter, filter)&&const DeepCollectionEquality().equals(other.sort, sort)&&(identical(other.limit, limit) || other.limit == limit)&&(identical(other.offset, offset) || other.offset == offset));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,sourceId,const DeepCollectionEquality().hash(filter),const DeepCollectionEquality().hash(sort),limit,offset);

@override
String toString() {
  return 'ArtistsQuery(sourceId: $sourceId, filter: $filter, sort: $sort, limit: $limit, offset: $offset)';
}


}

/// @nodoc
abstract mixin class $ArtistsQueryCopyWith<$Res>  {
  factory $ArtistsQueryCopyWith(ArtistsQuery value, $Res Function(ArtistsQuery) _then) = _$ArtistsQueryCopyWithImpl;
@useResult
$Res call({
 int sourceId, IList<ArtistsFilter> filter, IList<ArtistsSortingTerm> sort, int? limit, int? offset
});




}
/// @nodoc
class _$ArtistsQueryCopyWithImpl<$Res>
    implements $ArtistsQueryCopyWith<$Res> {
  _$ArtistsQueryCopyWithImpl(this._self, this._then);

  final ArtistsQuery _self;
  final $Res Function(ArtistsQuery) _then;

/// Create a copy of ArtistsQuery
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? sourceId = null,Object? filter = null,Object? sort = null,Object? limit = freezed,Object? offset = freezed,}) {
  return _then(_self.copyWith(
sourceId: null == sourceId ? _self.sourceId : sourceId // ignore: cast_nullable_to_non_nullable
as int,filter: null == filter ? _self.filter : filter // ignore: cast_nullable_to_non_nullable
as IList<ArtistsFilter>,sort: null == sort ? _self.sort : sort // ignore: cast_nullable_to_non_nullable
as IList<ArtistsSortingTerm>,limit: freezed == limit ? _self.limit : limit // ignore: cast_nullable_to_non_nullable
as int?,offset: freezed == offset ? _self.offset : offset // ignore: cast_nullable_to_non_nullable
as int?,
  ));
}

}


/// Adds pattern-matching-related methods to [ArtistsQuery].
extension ArtistsQueryPatterns on ArtistsQuery {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _ArtistsQuery value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _ArtistsQuery() when $default != null:
return $default(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _ArtistsQuery value)  $default,){
final _that = this;
switch (_that) {
case _ArtistsQuery():
return $default(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _ArtistsQuery value)?  $default,){
final _that = this;
switch (_that) {
case _ArtistsQuery() when $default != null:
return $default(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( int sourceId,  IList<ArtistsFilter> filter,  IList<ArtistsSortingTerm> sort,  int? limit,  int? offset)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _ArtistsQuery() when $default != null:
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( int sourceId,  IList<ArtistsFilter> filter,  IList<ArtistsSortingTerm> sort,  int? limit,  int? offset)  $default,) {final _that = this;
switch (_that) {
case _ArtistsQuery():
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( int sourceId,  IList<ArtistsFilter> filter,  IList<ArtistsSortingTerm> sort,  int? limit,  int? offset)?  $default,) {final _that = this;
switch (_that) {
case _ArtistsQuery() when $default != null:
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _ArtistsQuery implements ArtistsQuery {
  const _ArtistsQuery({required this.sourceId, this.filter = const IListConst([]), required this.sort, this.limit, this.offset});
  factory _ArtistsQuery.fromJson(Map<String, dynamic> json) => _$ArtistsQueryFromJson(json);

@override final  int sourceId;
@override@JsonKey() final  IList<ArtistsFilter> filter;
@override final  IList<ArtistsSortingTerm> sort;
@override final  int? limit;
@override final  int? offset;

/// Create a copy of ArtistsQuery
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$ArtistsQueryCopyWith<_ArtistsQuery> get copyWith => __$ArtistsQueryCopyWithImpl<_ArtistsQuery>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$ArtistsQueryToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _ArtistsQuery&&(identical(other.sourceId, sourceId) || other.sourceId == sourceId)&&const DeepCollectionEquality().equals(other.filter, filter)&&const DeepCollectionEquality().equals(other.sort, sort)&&(identical(other.limit, limit) || other.limit == limit)&&(identical(other.offset, offset) || other.offset == offset));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,sourceId,const DeepCollectionEquality().hash(filter),const DeepCollectionEquality().hash(sort),limit,offset);

@override
String toString() {
  return 'ArtistsQuery(sourceId: $sourceId, filter: $filter, sort: $sort, limit: $limit, offset: $offset)';
}


}

/// @nodoc
abstract mixin class _$ArtistsQueryCopyWith<$Res> implements $ArtistsQueryCopyWith<$Res> {
  factory _$ArtistsQueryCopyWith(_ArtistsQuery value, $Res Function(_ArtistsQuery) _then) = __$ArtistsQueryCopyWithImpl;
@override @useResult
$Res call({
 int sourceId, IList<ArtistsFilter> filter, IList<ArtistsSortingTerm> sort, int? limit, int? offset
});




}
/// @nodoc
class __$ArtistsQueryCopyWithImpl<$Res>
    implements _$ArtistsQueryCopyWith<$Res> {
  __$ArtistsQueryCopyWithImpl(this._self, this._then);

  final _ArtistsQuery _self;
  final $Res Function(_ArtistsQuery) _then;

/// Create a copy of ArtistsQuery
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? sourceId = null,Object? filter = null,Object? sort = null,Object? limit = freezed,Object? offset = freezed,}) {
  return _then(_ArtistsQuery(
sourceId: null == sourceId ? _self.sourceId : sourceId // ignore: cast_nullable_to_non_nullable
as int,filter: null == filter ? _self.filter : filter // ignore: cast_nullable_to_non_nullable
as IList<ArtistsFilter>,sort: null == sort ? _self.sort : sort // ignore: cast_nullable_to_non_nullable
as IList<ArtistsSortingTerm>,limit: freezed == limit ? _self.limit : limit // ignore: cast_nullable_to_non_nullable
as int?,offset: freezed == offset ? _self.offset : offset // ignore: cast_nullable_to_non_nullable
as int?,
  ));
}


}

ArtistsFilter _$ArtistsFilterFromJson(
  Map<String, dynamic> json
) {
        switch (json['runtimeType']) {
                  case 'nameSearch':
          return ArtistsFilterNameSearch.fromJson(
            json
          );
                case 'starred':
          return ArtistsFilterStarred.fromJson(
            json
          );
        
          default:
            throw CheckedFromJsonException(
  json,
  'runtimeType',
  'ArtistsFilter',
  'Invalid union type "${json['runtimeType']}"!'
);
        }
      
}

/// @nodoc
mixin _$ArtistsFilter {



  /// Serializes this ArtistsFilter to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is ArtistsFilter);
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => runtimeType.hashCode;

@override
String toString() {
  return 'ArtistsFilter()';
}


}

/// @nodoc
class $ArtistsFilterCopyWith<$Res>  {
$ArtistsFilterCopyWith(ArtistsFilter _, $Res Function(ArtistsFilter) __);
}


/// Adds pattern-matching-related methods to [ArtistsFilter].
extension ArtistsFilterPatterns on ArtistsFilter {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>({TResult Function( ArtistsFilterNameSearch value)?  nameSearch,TResult Function( ArtistsFilterStarred value)?  starred,required TResult orElse(),}){
final _that = this;
switch (_that) {
case ArtistsFilterNameSearch() when nameSearch != null:
return nameSearch(_that);case ArtistsFilterStarred() when starred != null:
return starred(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>({required TResult Function( ArtistsFilterNameSearch value)  nameSearch,required TResult Function( ArtistsFilterStarred value)  starred,}){
final _that = this;
switch (_that) {
case ArtistsFilterNameSearch():
return nameSearch(_that);case ArtistsFilterStarred():
return starred(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>({TResult? Function( ArtistsFilterNameSearch value)?  nameSearch,TResult? Function( ArtistsFilterStarred value)?  starred,}){
final _that = this;
switch (_that) {
case ArtistsFilterNameSearch() when nameSearch != null:
return nameSearch(_that);case ArtistsFilterStarred() when starred != null:
return starred(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>({TResult Function( String name)?  nameSearch,TResult Function( bool starred)?  starred,required TResult orElse(),}) {final _that = this;
switch (_that) {
case ArtistsFilterNameSearch() when nameSearch != null:
return nameSearch(_that.name);case ArtistsFilterStarred() when starred != null:
return starred(_that.starred);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>({required TResult Function( String name)  nameSearch,required TResult Function( bool starred)  starred,}) {final _that = this;
switch (_that) {
case ArtistsFilterNameSearch():
return nameSearch(_that.name);case ArtistsFilterStarred():
return starred(_that.starred);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>({TResult? Function( String name)?  nameSearch,TResult? Function( bool starred)?  starred,}) {final _that = this;
switch (_that) {
case ArtistsFilterNameSearch() when nameSearch != null:
return nameSearch(_that.name);case ArtistsFilterStarred() when starred != null:
return starred(_that.starred);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class ArtistsFilterNameSearch implements ArtistsFilter {
  const ArtistsFilterNameSearch(this.name, {final  String? $type}): $type = $type ?? 'nameSearch';
  factory ArtistsFilterNameSearch.fromJson(Map<String, dynamic> json) => _$ArtistsFilterNameSearchFromJson(json);

 final  String name;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of ArtistsFilter
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$ArtistsFilterNameSearchCopyWith<ArtistsFilterNameSearch> get copyWith => _$ArtistsFilterNameSearchCopyWithImpl<ArtistsFilterNameSearch>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$ArtistsFilterNameSearchToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is ArtistsFilterNameSearch&&(identical(other.name, name) || other.name == name));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,name);

@override
String toString() {
  return 'ArtistsFilter.nameSearch(name: $name)';
}


}

/// @nodoc
abstract mixin class $ArtistsFilterNameSearchCopyWith<$Res> implements $ArtistsFilterCopyWith<$Res> {
  factory $ArtistsFilterNameSearchCopyWith(ArtistsFilterNameSearch value, $Res Function(ArtistsFilterNameSearch) _then) = _$ArtistsFilterNameSearchCopyWithImpl;
@useResult
$Res call({
 String name
});




}
/// @nodoc
class _$ArtistsFilterNameSearchCopyWithImpl<$Res>
    implements $ArtistsFilterNameSearchCopyWith<$Res> {
  _$ArtistsFilterNameSearchCopyWithImpl(this._self, this._then);

  final ArtistsFilterNameSearch _self;
  final $Res Function(ArtistsFilterNameSearch) _then;

/// Create a copy of ArtistsFilter
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') $Res call({Object? name = null,}) {
  return _then(ArtistsFilterNameSearch(
null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}

/// @nodoc
@JsonSerializable()

class ArtistsFilterStarred implements ArtistsFilter {
  const ArtistsFilterStarred(this.starred, {final  String? $type}): $type = $type ?? 'starred';
  factory ArtistsFilterStarred.fromJson(Map<String, dynamic> json) => _$ArtistsFilterStarredFromJson(json);

 final  bool starred;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of ArtistsFilter
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$ArtistsFilterStarredCopyWith<ArtistsFilterStarred> get copyWith => _$ArtistsFilterStarredCopyWithImpl<ArtistsFilterStarred>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$ArtistsFilterStarredToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is ArtistsFilterStarred&&(identical(other.starred, starred) || other.starred == starred));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,starred);

@override
String toString() {
  return 'ArtistsFilter.starred(starred: $starred)';
}


}

/// @nodoc
abstract mixin class $ArtistsFilterStarredCopyWith<$Res> implements $ArtistsFilterCopyWith<$Res> {
  factory $ArtistsFilterStarredCopyWith(ArtistsFilterStarred value, $Res Function(ArtistsFilterStarred) _then) = _$ArtistsFilterStarredCopyWithImpl;
@useResult
$Res call({
 bool starred
});




}
/// @nodoc
class _$ArtistsFilterStarredCopyWithImpl<$Res>
    implements $ArtistsFilterStarredCopyWith<$Res> {
  _$ArtistsFilterStarredCopyWithImpl(this._self, this._then);

  final ArtistsFilterStarred _self;
  final $Res Function(ArtistsFilterStarred) _then;

/// Create a copy of ArtistsFilter
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') $Res call({Object? starred = null,}) {
  return _then(ArtistsFilterStarred(
null == starred ? _self.starred : starred // ignore: cast_nullable_to_non_nullable
as bool,
  ));
}


}


/// @nodoc
mixin _$SongsQuery {

 int get sourceId; IList<SongsFilter> get filter; IList<SongsSortingTerm> get sort; int? get limit; int? get offset;
/// Create a copy of SongsQuery
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SongsQueryCopyWith<SongsQuery> get copyWith => _$SongsQueryCopyWithImpl<SongsQuery>(this as SongsQuery, _$identity);

  /// Serializes this SongsQuery to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SongsQuery&&(identical(other.sourceId, sourceId) || other.sourceId == sourceId)&&const DeepCollectionEquality().equals(other.filter, filter)&&const DeepCollectionEquality().equals(other.sort, sort)&&(identical(other.limit, limit) || other.limit == limit)&&(identical(other.offset, offset) || other.offset == offset));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,sourceId,const DeepCollectionEquality().hash(filter),const DeepCollectionEquality().hash(sort),limit,offset);

@override
String toString() {
  return 'SongsQuery(sourceId: $sourceId, filter: $filter, sort: $sort, limit: $limit, offset: $offset)';
}


}

/// @nodoc
abstract mixin class $SongsQueryCopyWith<$Res>  {
  factory $SongsQueryCopyWith(SongsQuery value, $Res Function(SongsQuery) _then) = _$SongsQueryCopyWithImpl;
@useResult
$Res call({
 int sourceId, IList<SongsFilter> filter, IList<SongsSortingTerm> sort, int? limit, int? offset
});




}
/// @nodoc
class _$SongsQueryCopyWithImpl<$Res>
    implements $SongsQueryCopyWith<$Res> {
  _$SongsQueryCopyWithImpl(this._self, this._then);

  final SongsQuery _self;
  final $Res Function(SongsQuery) _then;

/// Create a copy of SongsQuery
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? sourceId = null,Object? filter = null,Object? sort = null,Object? limit = freezed,Object? offset = freezed,}) {
  return _then(_self.copyWith(
sourceId: null == sourceId ? _self.sourceId : sourceId // ignore: cast_nullable_to_non_nullable
as int,filter: null == filter ? _self.filter : filter // ignore: cast_nullable_to_non_nullable
as IList<SongsFilter>,sort: null == sort ? _self.sort : sort // ignore: cast_nullable_to_non_nullable
as IList<SongsSortingTerm>,limit: freezed == limit ? _self.limit : limit // ignore: cast_nullable_to_non_nullable
as int?,offset: freezed == offset ? _self.offset : offset // ignore: cast_nullable_to_non_nullable
as int?,
  ));
}

}


/// Adds pattern-matching-related methods to [SongsQuery].
extension SongsQueryPatterns on SongsQuery {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _SongsQuery value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _SongsQuery() when $default != null:
return $default(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _SongsQuery value)  $default,){
final _that = this;
switch (_that) {
case _SongsQuery():
return $default(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _SongsQuery value)?  $default,){
final _that = this;
switch (_that) {
case _SongsQuery() when $default != null:
return $default(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( int sourceId,  IList<SongsFilter> filter,  IList<SongsSortingTerm> sort,  int? limit,  int? offset)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _SongsQuery() when $default != null:
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( int sourceId,  IList<SongsFilter> filter,  IList<SongsSortingTerm> sort,  int? limit,  int? offset)  $default,) {final _that = this;
switch (_that) {
case _SongsQuery():
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( int sourceId,  IList<SongsFilter> filter,  IList<SongsSortingTerm> sort,  int? limit,  int? offset)?  $default,) {final _that = this;
switch (_that) {
case _SongsQuery() when $default != null:
return $default(_that.sourceId,_that.filter,_that.sort,_that.limit,_that.offset);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _SongsQuery implements SongsQuery {
  const _SongsQuery({required this.sourceId, this.filter = const IListConst([]), required this.sort, this.limit, this.offset});
  factory _SongsQuery.fromJson(Map<String, dynamic> json) => _$SongsQueryFromJson(json);

@override final  int sourceId;
@override@JsonKey() final  IList<SongsFilter> filter;
@override final  IList<SongsSortingTerm> sort;
@override final  int? limit;
@override final  int? offset;

/// Create a copy of SongsQuery
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$SongsQueryCopyWith<_SongsQuery> get copyWith => __$SongsQueryCopyWithImpl<_SongsQuery>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$SongsQueryToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _SongsQuery&&(identical(other.sourceId, sourceId) || other.sourceId == sourceId)&&const DeepCollectionEquality().equals(other.filter, filter)&&const DeepCollectionEquality().equals(other.sort, sort)&&(identical(other.limit, limit) || other.limit == limit)&&(identical(other.offset, offset) || other.offset == offset));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,sourceId,const DeepCollectionEquality().hash(filter),const DeepCollectionEquality().hash(sort),limit,offset);

@override
String toString() {
  return 'SongsQuery(sourceId: $sourceId, filter: $filter, sort: $sort, limit: $limit, offset: $offset)';
}


}

/// @nodoc
abstract mixin class _$SongsQueryCopyWith<$Res> implements $SongsQueryCopyWith<$Res> {
  factory _$SongsQueryCopyWith(_SongsQuery value, $Res Function(_SongsQuery) _then) = __$SongsQueryCopyWithImpl;
@override @useResult
$Res call({
 int sourceId, IList<SongsFilter> filter, IList<SongsSortingTerm> sort, int? limit, int? offset
});




}
/// @nodoc
class __$SongsQueryCopyWithImpl<$Res>
    implements _$SongsQueryCopyWith<$Res> {
  __$SongsQueryCopyWithImpl(this._self, this._then);

  final _SongsQuery _self;
  final $Res Function(_SongsQuery) _then;

/// Create a copy of SongsQuery
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? sourceId = null,Object? filter = null,Object? sort = null,Object? limit = freezed,Object? offset = freezed,}) {
  return _then(_SongsQuery(
sourceId: null == sourceId ? _self.sourceId : sourceId // ignore: cast_nullable_to_non_nullable
as int,filter: null == filter ? _self.filter : filter // ignore: cast_nullable_to_non_nullable
as IList<SongsFilter>,sort: null == sort ? _self.sort : sort // ignore: cast_nullable_to_non_nullable
as IList<SongsSortingTerm>,limit: freezed == limit ? _self.limit : limit // ignore: cast_nullable_to_non_nullable
as int?,offset: freezed == offset ? _self.offset : offset // ignore: cast_nullable_to_non_nullable
as int?,
  ));
}


}

SongsFilter _$SongsFilterFromJson(
  Map<String, dynamic> json
) {
        switch (json['runtimeType']) {
                  case 'albumId':
          return SongsFilterAlbumId.fromJson(
            json
          );
                case 'playlistId':
          return SongsFilterPlaylistId.fromJson(
            json
          );
        
          default:
            throw CheckedFromJsonException(
  json,
  'runtimeType',
  'SongsFilter',
  'Invalid union type "${json['runtimeType']}"!'
);
        }
      
}

/// @nodoc
mixin _$SongsFilter {



  /// Serializes this SongsFilter to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SongsFilter);
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => runtimeType.hashCode;

@override
String toString() {
  return 'SongsFilter()';
}


}

/// @nodoc
class $SongsFilterCopyWith<$Res>  {
$SongsFilterCopyWith(SongsFilter _, $Res Function(SongsFilter) __);
}


/// Adds pattern-matching-related methods to [SongsFilter].
extension SongsFilterPatterns on SongsFilter {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>({TResult Function( SongsFilterAlbumId value)?  albumId,TResult Function( SongsFilterPlaylistId value)?  playlistId,required TResult orElse(),}){
final _that = this;
switch (_that) {
case SongsFilterAlbumId() when albumId != null:
return albumId(_that);case SongsFilterPlaylistId() when playlistId != null:
return playlistId(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>({required TResult Function( SongsFilterAlbumId value)  albumId,required TResult Function( SongsFilterPlaylistId value)  playlistId,}){
final _that = this;
switch (_that) {
case SongsFilterAlbumId():
return albumId(_that);case SongsFilterPlaylistId():
return playlistId(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>({TResult? Function( SongsFilterAlbumId value)?  albumId,TResult? Function( SongsFilterPlaylistId value)?  playlistId,}){
final _that = this;
switch (_that) {
case SongsFilterAlbumId() when albumId != null:
return albumId(_that);case SongsFilterPlaylistId() when playlistId != null:
return playlistId(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>({TResult Function( String albumId)?  albumId,TResult Function( String playlistId)?  playlistId,required TResult orElse(),}) {final _that = this;
switch (_that) {
case SongsFilterAlbumId() when albumId != null:
return albumId(_that.albumId);case SongsFilterPlaylistId() when playlistId != null:
return playlistId(_that.playlistId);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>({required TResult Function( String albumId)  albumId,required TResult Function( String playlistId)  playlistId,}) {final _that = this;
switch (_that) {
case SongsFilterAlbumId():
return albumId(_that.albumId);case SongsFilterPlaylistId():
return playlistId(_that.playlistId);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>({TResult? Function( String albumId)?  albumId,TResult? Function( String playlistId)?  playlistId,}) {final _that = this;
switch (_that) {
case SongsFilterAlbumId() when albumId != null:
return albumId(_that.albumId);case SongsFilterPlaylistId() when playlistId != null:
return playlistId(_that.playlistId);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class SongsFilterAlbumId implements SongsFilter {
  const SongsFilterAlbumId(this.albumId, {final  String? $type}): $type = $type ?? 'albumId';
  factory SongsFilterAlbumId.fromJson(Map<String, dynamic> json) => _$SongsFilterAlbumIdFromJson(json);

 final  String albumId;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of SongsFilter
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SongsFilterAlbumIdCopyWith<SongsFilterAlbumId> get copyWith => _$SongsFilterAlbumIdCopyWithImpl<SongsFilterAlbumId>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$SongsFilterAlbumIdToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SongsFilterAlbumId&&(identical(other.albumId, albumId) || other.albumId == albumId));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,albumId);

@override
String toString() {
  return 'SongsFilter.albumId(albumId: $albumId)';
}


}

/// @nodoc
abstract mixin class $SongsFilterAlbumIdCopyWith<$Res> implements $SongsFilterCopyWith<$Res> {
  factory $SongsFilterAlbumIdCopyWith(SongsFilterAlbumId value, $Res Function(SongsFilterAlbumId) _then) = _$SongsFilterAlbumIdCopyWithImpl;
@useResult
$Res call({
 String albumId
});




}
/// @nodoc
class _$SongsFilterAlbumIdCopyWithImpl<$Res>
    implements $SongsFilterAlbumIdCopyWith<$Res> {
  _$SongsFilterAlbumIdCopyWithImpl(this._self, this._then);

  final SongsFilterAlbumId _self;
  final $Res Function(SongsFilterAlbumId) _then;

/// Create a copy of SongsFilter
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') $Res call({Object? albumId = null,}) {
  return _then(SongsFilterAlbumId(
null == albumId ? _self.albumId : albumId // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}

/// @nodoc
@JsonSerializable()

class SongsFilterPlaylistId implements SongsFilter {
  const SongsFilterPlaylistId(this.playlistId, {final  String? $type}): $type = $type ?? 'playlistId';
  factory SongsFilterPlaylistId.fromJson(Map<String, dynamic> json) => _$SongsFilterPlaylistIdFromJson(json);

 final  String playlistId;

@JsonKey(name: 'runtimeType')
final String $type;


/// Create a copy of SongsFilter
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SongsFilterPlaylistIdCopyWith<SongsFilterPlaylistId> get copyWith => _$SongsFilterPlaylistIdCopyWithImpl<SongsFilterPlaylistId>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$SongsFilterPlaylistIdToJson(this, );
}

@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SongsFilterPlaylistId&&(identical(other.playlistId, playlistId) || other.playlistId == playlistId));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode => Object.hash(runtimeType,playlistId);

@override
String toString() {
  return 'SongsFilter.playlistId(playlistId: $playlistId)';
}


}

/// @nodoc
abstract mixin class $SongsFilterPlaylistIdCopyWith<$Res> implements $SongsFilterCopyWith<$Res> {
  factory $SongsFilterPlaylistIdCopyWith(SongsFilterPlaylistId value, $Res Function(SongsFilterPlaylistId) _then) = _$SongsFilterPlaylistIdCopyWithImpl;
@useResult
$Res call({
 String playlistId
});




}
/// @nodoc
class _$SongsFilterPlaylistIdCopyWithImpl<$Res>
    implements $SongsFilterPlaylistIdCopyWith<$Res> {
  _$SongsFilterPlaylistIdCopyWithImpl(this._self, this._then);

  final SongsFilterPlaylistId _self;
  final $Res Function(SongsFilterPlaylistId) _then;

/// Create a copy of SongsFilter
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') $Res call({Object? playlistId = null,}) {
  return _then(SongsFilterPlaylistId(
null == playlistId ? _self.playlistId : playlistId // ignore: cast_nullable_to_non_nullable
as String,
  ));
}


}

// dart format on
