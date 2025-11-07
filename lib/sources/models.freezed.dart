// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'models.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// dart format off
T _$identity<T>(T value) => value;
/// @nodoc
mixin _$Artist {

 String get id; String get name; DateTime? get starred; Uri? get smallImage; Uri? get largeImage;
/// Create a copy of Artist
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$ArtistCopyWith<Artist> get copyWith => _$ArtistCopyWithImpl<Artist>(this as Artist, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is Artist&&(identical(other.id, id) || other.id == id)&&(identical(other.name, name) || other.name == name)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.smallImage, smallImage) || other.smallImage == smallImage)&&(identical(other.largeImage, largeImage) || other.largeImage == largeImage));
}


@override
int get hashCode => Object.hash(runtimeType,id,name,starred,smallImage,largeImage);

@override
String toString() {
  return 'Artist(id: $id, name: $name, starred: $starred, smallImage: $smallImage, largeImage: $largeImage)';
}


}

/// @nodoc
abstract mixin class $ArtistCopyWith<$Res>  {
  factory $ArtistCopyWith(Artist value, $Res Function(Artist) _then) = _$ArtistCopyWithImpl;
@useResult
$Res call({
 String id, String name, DateTime? starred, Uri? smallImage, Uri? largeImage
});




}
/// @nodoc
class _$ArtistCopyWithImpl<$Res>
    implements $ArtistCopyWith<$Res> {
  _$ArtistCopyWithImpl(this._self, this._then);

  final Artist _self;
  final $Res Function(Artist) _then;

/// Create a copy of Artist
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? name = null,Object? starred = freezed,Object? smallImage = freezed,Object? largeImage = freezed,}) {
  return _then(_self.copyWith(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,name: null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,starred: freezed == starred ? _self.starred : starred // ignore: cast_nullable_to_non_nullable
as DateTime?,smallImage: freezed == smallImage ? _self.smallImage : smallImage // ignore: cast_nullable_to_non_nullable
as Uri?,largeImage: freezed == largeImage ? _self.largeImage : largeImage // ignore: cast_nullable_to_non_nullable
as Uri?,
  ));
}

}


/// Adds pattern-matching-related methods to [Artist].
extension ArtistPatterns on Artist {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _Artist value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _Artist() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _Artist value)  $default,){
final _that = this;
switch (_that) {
case _Artist():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _Artist value)?  $default,){
final _that = this;
switch (_that) {
case _Artist() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String id,  String name,  DateTime? starred,  Uri? smallImage,  Uri? largeImage)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _Artist() when $default != null:
return $default(_that.id,_that.name,_that.starred,_that.smallImage,_that.largeImage);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String id,  String name,  DateTime? starred,  Uri? smallImage,  Uri? largeImage)  $default,) {final _that = this;
switch (_that) {
case _Artist():
return $default(_that.id,_that.name,_that.starred,_that.smallImage,_that.largeImage);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String id,  String name,  DateTime? starred,  Uri? smallImage,  Uri? largeImage)?  $default,) {final _that = this;
switch (_that) {
case _Artist() when $default != null:
return $default(_that.id,_that.name,_that.starred,_that.smallImage,_that.largeImage);case _:
  return null;

}
}

}

/// @nodoc


class _Artist implements Artist {
  const _Artist({required this.id, required this.name, this.starred, this.smallImage, this.largeImage});
  

@override final  String id;
@override final  String name;
@override final  DateTime? starred;
@override final  Uri? smallImage;
@override final  Uri? largeImage;

/// Create a copy of Artist
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$ArtistCopyWith<_Artist> get copyWith => __$ArtistCopyWithImpl<_Artist>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _Artist&&(identical(other.id, id) || other.id == id)&&(identical(other.name, name) || other.name == name)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.smallImage, smallImage) || other.smallImage == smallImage)&&(identical(other.largeImage, largeImage) || other.largeImage == largeImage));
}


@override
int get hashCode => Object.hash(runtimeType,id,name,starred,smallImage,largeImage);

@override
String toString() {
  return 'Artist(id: $id, name: $name, starred: $starred, smallImage: $smallImage, largeImage: $largeImage)';
}


}

/// @nodoc
abstract mixin class _$ArtistCopyWith<$Res> implements $ArtistCopyWith<$Res> {
  factory _$ArtistCopyWith(_Artist value, $Res Function(_Artist) _then) = __$ArtistCopyWithImpl;
@override @useResult
$Res call({
 String id, String name, DateTime? starred, Uri? smallImage, Uri? largeImage
});




}
/// @nodoc
class __$ArtistCopyWithImpl<$Res>
    implements _$ArtistCopyWith<$Res> {
  __$ArtistCopyWithImpl(this._self, this._then);

  final _Artist _self;
  final $Res Function(_Artist) _then;

/// Create a copy of Artist
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? name = null,Object? starred = freezed,Object? smallImage = freezed,Object? largeImage = freezed,}) {
  return _then(_Artist(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,name: null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,starred: freezed == starred ? _self.starred : starred // ignore: cast_nullable_to_non_nullable
as DateTime?,smallImage: freezed == smallImage ? _self.smallImage : smallImage // ignore: cast_nullable_to_non_nullable
as Uri?,largeImage: freezed == largeImage ? _self.largeImage : largeImage // ignore: cast_nullable_to_non_nullable
as Uri?,
  ));
}


}

/// @nodoc
mixin _$Album {

 String get id; String? get artistId; String get name; String? get albumArtist; DateTime get created; String? get coverArt; int? get year; DateTime? get starred; String? get genre; int? get frequentRank; int? get recentRank;
/// Create a copy of Album
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$AlbumCopyWith<Album> get copyWith => _$AlbumCopyWithImpl<Album>(this as Album, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is Album&&(identical(other.id, id) || other.id == id)&&(identical(other.artistId, artistId) || other.artistId == artistId)&&(identical(other.name, name) || other.name == name)&&(identical(other.albumArtist, albumArtist) || other.albumArtist == albumArtist)&&(identical(other.created, created) || other.created == created)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt)&&(identical(other.year, year) || other.year == year)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.genre, genre) || other.genre == genre)&&(identical(other.frequentRank, frequentRank) || other.frequentRank == frequentRank)&&(identical(other.recentRank, recentRank) || other.recentRank == recentRank));
}


@override
int get hashCode => Object.hash(runtimeType,id,artistId,name,albumArtist,created,coverArt,year,starred,genre,frequentRank,recentRank);

@override
String toString() {
  return 'Album(id: $id, artistId: $artistId, name: $name, albumArtist: $albumArtist, created: $created, coverArt: $coverArt, year: $year, starred: $starred, genre: $genre, frequentRank: $frequentRank, recentRank: $recentRank)';
}


}

/// @nodoc
abstract mixin class $AlbumCopyWith<$Res>  {
  factory $AlbumCopyWith(Album value, $Res Function(Album) _then) = _$AlbumCopyWithImpl;
@useResult
$Res call({
 String id, String? artistId, String name, String? albumArtist, DateTime created, String? coverArt, int? year, DateTime? starred, String? genre, int? frequentRank, int? recentRank
});




}
/// @nodoc
class _$AlbumCopyWithImpl<$Res>
    implements $AlbumCopyWith<$Res> {
  _$AlbumCopyWithImpl(this._self, this._then);

  final Album _self;
  final $Res Function(Album) _then;

/// Create a copy of Album
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? artistId = freezed,Object? name = null,Object? albumArtist = freezed,Object? created = null,Object? coverArt = freezed,Object? year = freezed,Object? starred = freezed,Object? genre = freezed,Object? frequentRank = freezed,Object? recentRank = freezed,}) {
  return _then(_self.copyWith(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,artistId: freezed == artistId ? _self.artistId : artistId // ignore: cast_nullable_to_non_nullable
as String?,name: null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,albumArtist: freezed == albumArtist ? _self.albumArtist : albumArtist // ignore: cast_nullable_to_non_nullable
as String?,created: null == created ? _self.created : created // ignore: cast_nullable_to_non_nullable
as DateTime,coverArt: freezed == coverArt ? _self.coverArt : coverArt // ignore: cast_nullable_to_non_nullable
as String?,year: freezed == year ? _self.year : year // ignore: cast_nullable_to_non_nullable
as int?,starred: freezed == starred ? _self.starred : starred // ignore: cast_nullable_to_non_nullable
as DateTime?,genre: freezed == genre ? _self.genre : genre // ignore: cast_nullable_to_non_nullable
as String?,frequentRank: freezed == frequentRank ? _self.frequentRank : frequentRank // ignore: cast_nullable_to_non_nullable
as int?,recentRank: freezed == recentRank ? _self.recentRank : recentRank // ignore: cast_nullable_to_non_nullable
as int?,
  ));
}

}


/// Adds pattern-matching-related methods to [Album].
extension AlbumPatterns on Album {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _Album value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _Album() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _Album value)  $default,){
final _that = this;
switch (_that) {
case _Album():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _Album value)?  $default,){
final _that = this;
switch (_that) {
case _Album() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String id,  String? artistId,  String name,  String? albumArtist,  DateTime created,  String? coverArt,  int? year,  DateTime? starred,  String? genre,  int? frequentRank,  int? recentRank)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _Album() when $default != null:
return $default(_that.id,_that.artistId,_that.name,_that.albumArtist,_that.created,_that.coverArt,_that.year,_that.starred,_that.genre,_that.frequentRank,_that.recentRank);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String id,  String? artistId,  String name,  String? albumArtist,  DateTime created,  String? coverArt,  int? year,  DateTime? starred,  String? genre,  int? frequentRank,  int? recentRank)  $default,) {final _that = this;
switch (_that) {
case _Album():
return $default(_that.id,_that.artistId,_that.name,_that.albumArtist,_that.created,_that.coverArt,_that.year,_that.starred,_that.genre,_that.frequentRank,_that.recentRank);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String id,  String? artistId,  String name,  String? albumArtist,  DateTime created,  String? coverArt,  int? year,  DateTime? starred,  String? genre,  int? frequentRank,  int? recentRank)?  $default,) {final _that = this;
switch (_that) {
case _Album() when $default != null:
return $default(_that.id,_that.artistId,_that.name,_that.albumArtist,_that.created,_that.coverArt,_that.year,_that.starred,_that.genre,_that.frequentRank,_that.recentRank);case _:
  return null;

}
}

}

/// @nodoc


class _Album implements Album {
  const _Album({required this.id, this.artistId, required this.name, this.albumArtist, required this.created, this.coverArt, this.year, this.starred, this.genre, this.frequentRank, this.recentRank});
  

@override final  String id;
@override final  String? artistId;
@override final  String name;
@override final  String? albumArtist;
@override final  DateTime created;
@override final  String? coverArt;
@override final  int? year;
@override final  DateTime? starred;
@override final  String? genre;
@override final  int? frequentRank;
@override final  int? recentRank;

/// Create a copy of Album
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$AlbumCopyWith<_Album> get copyWith => __$AlbumCopyWithImpl<_Album>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _Album&&(identical(other.id, id) || other.id == id)&&(identical(other.artistId, artistId) || other.artistId == artistId)&&(identical(other.name, name) || other.name == name)&&(identical(other.albumArtist, albumArtist) || other.albumArtist == albumArtist)&&(identical(other.created, created) || other.created == created)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt)&&(identical(other.year, year) || other.year == year)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.genre, genre) || other.genre == genre)&&(identical(other.frequentRank, frequentRank) || other.frequentRank == frequentRank)&&(identical(other.recentRank, recentRank) || other.recentRank == recentRank));
}


@override
int get hashCode => Object.hash(runtimeType,id,artistId,name,albumArtist,created,coverArt,year,starred,genre,frequentRank,recentRank);

@override
String toString() {
  return 'Album(id: $id, artistId: $artistId, name: $name, albumArtist: $albumArtist, created: $created, coverArt: $coverArt, year: $year, starred: $starred, genre: $genre, frequentRank: $frequentRank, recentRank: $recentRank)';
}


}

/// @nodoc
abstract mixin class _$AlbumCopyWith<$Res> implements $AlbumCopyWith<$Res> {
  factory _$AlbumCopyWith(_Album value, $Res Function(_Album) _then) = __$AlbumCopyWithImpl;
@override @useResult
$Res call({
 String id, String? artistId, String name, String? albumArtist, DateTime created, String? coverArt, int? year, DateTime? starred, String? genre, int? frequentRank, int? recentRank
});




}
/// @nodoc
class __$AlbumCopyWithImpl<$Res>
    implements _$AlbumCopyWith<$Res> {
  __$AlbumCopyWithImpl(this._self, this._then);

  final _Album _self;
  final $Res Function(_Album) _then;

/// Create a copy of Album
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? artistId = freezed,Object? name = null,Object? albumArtist = freezed,Object? created = null,Object? coverArt = freezed,Object? year = freezed,Object? starred = freezed,Object? genre = freezed,Object? frequentRank = freezed,Object? recentRank = freezed,}) {
  return _then(_Album(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,artistId: freezed == artistId ? _self.artistId : artistId // ignore: cast_nullable_to_non_nullable
as String?,name: null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,albumArtist: freezed == albumArtist ? _self.albumArtist : albumArtist // ignore: cast_nullable_to_non_nullable
as String?,created: null == created ? _self.created : created // ignore: cast_nullable_to_non_nullable
as DateTime,coverArt: freezed == coverArt ? _self.coverArt : coverArt // ignore: cast_nullable_to_non_nullable
as String?,year: freezed == year ? _self.year : year // ignore: cast_nullable_to_non_nullable
as int?,starred: freezed == starred ? _self.starred : starred // ignore: cast_nullable_to_non_nullable
as DateTime?,genre: freezed == genre ? _self.genre : genre // ignore: cast_nullable_to_non_nullable
as String?,frequentRank: freezed == frequentRank ? _self.frequentRank : frequentRank // ignore: cast_nullable_to_non_nullable
as int?,recentRank: freezed == recentRank ? _self.recentRank : recentRank // ignore: cast_nullable_to_non_nullable
as int?,
  ));
}


}

/// @nodoc
mixin _$Playlist {

 String get id; String get name; String? get comment; DateTime get created; DateTime get changed; String? get coverArt; String? get owner; bool? get public;
/// Create a copy of Playlist
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$PlaylistCopyWith<Playlist> get copyWith => _$PlaylistCopyWithImpl<Playlist>(this as Playlist, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is Playlist&&(identical(other.id, id) || other.id == id)&&(identical(other.name, name) || other.name == name)&&(identical(other.comment, comment) || other.comment == comment)&&(identical(other.created, created) || other.created == created)&&(identical(other.changed, changed) || other.changed == changed)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt)&&(identical(other.owner, owner) || other.owner == owner)&&(identical(other.public, public) || other.public == public));
}


@override
int get hashCode => Object.hash(runtimeType,id,name,comment,created,changed,coverArt,owner,public);

@override
String toString() {
  return 'Playlist(id: $id, name: $name, comment: $comment, created: $created, changed: $changed, coverArt: $coverArt, owner: $owner, public: $public)';
}


}

/// @nodoc
abstract mixin class $PlaylistCopyWith<$Res>  {
  factory $PlaylistCopyWith(Playlist value, $Res Function(Playlist) _then) = _$PlaylistCopyWithImpl;
@useResult
$Res call({
 String id, String name, String? comment, DateTime created, DateTime changed, String? coverArt, String? owner, bool? public
});




}
/// @nodoc
class _$PlaylistCopyWithImpl<$Res>
    implements $PlaylistCopyWith<$Res> {
  _$PlaylistCopyWithImpl(this._self, this._then);

  final Playlist _self;
  final $Res Function(Playlist) _then;

/// Create a copy of Playlist
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? name = null,Object? comment = freezed,Object? created = null,Object? changed = null,Object? coverArt = freezed,Object? owner = freezed,Object? public = freezed,}) {
  return _then(_self.copyWith(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,name: null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,comment: freezed == comment ? _self.comment : comment // ignore: cast_nullable_to_non_nullable
as String?,created: null == created ? _self.created : created // ignore: cast_nullable_to_non_nullable
as DateTime,changed: null == changed ? _self.changed : changed // ignore: cast_nullable_to_non_nullable
as DateTime,coverArt: freezed == coverArt ? _self.coverArt : coverArt // ignore: cast_nullable_to_non_nullable
as String?,owner: freezed == owner ? _self.owner : owner // ignore: cast_nullable_to_non_nullable
as String?,public: freezed == public ? _self.public : public // ignore: cast_nullable_to_non_nullable
as bool?,
  ));
}

}


/// Adds pattern-matching-related methods to [Playlist].
extension PlaylistPatterns on Playlist {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _Playlist value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _Playlist() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _Playlist value)  $default,){
final _that = this;
switch (_that) {
case _Playlist():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _Playlist value)?  $default,){
final _that = this;
switch (_that) {
case _Playlist() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String id,  String name,  String? comment,  DateTime created,  DateTime changed,  String? coverArt,  String? owner,  bool? public)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _Playlist() when $default != null:
return $default(_that.id,_that.name,_that.comment,_that.created,_that.changed,_that.coverArt,_that.owner,_that.public);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String id,  String name,  String? comment,  DateTime created,  DateTime changed,  String? coverArt,  String? owner,  bool? public)  $default,) {final _that = this;
switch (_that) {
case _Playlist():
return $default(_that.id,_that.name,_that.comment,_that.created,_that.changed,_that.coverArt,_that.owner,_that.public);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String id,  String name,  String? comment,  DateTime created,  DateTime changed,  String? coverArt,  String? owner,  bool? public)?  $default,) {final _that = this;
switch (_that) {
case _Playlist() when $default != null:
return $default(_that.id,_that.name,_that.comment,_that.created,_that.changed,_that.coverArt,_that.owner,_that.public);case _:
  return null;

}
}

}

/// @nodoc


class _Playlist implements Playlist {
  const _Playlist({required this.id, required this.name, this.comment, required this.created, required this.changed, this.coverArt, this.owner, this.public});
  

@override final  String id;
@override final  String name;
@override final  String? comment;
@override final  DateTime created;
@override final  DateTime changed;
@override final  String? coverArt;
@override final  String? owner;
@override final  bool? public;

/// Create a copy of Playlist
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$PlaylistCopyWith<_Playlist> get copyWith => __$PlaylistCopyWithImpl<_Playlist>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _Playlist&&(identical(other.id, id) || other.id == id)&&(identical(other.name, name) || other.name == name)&&(identical(other.comment, comment) || other.comment == comment)&&(identical(other.created, created) || other.created == created)&&(identical(other.changed, changed) || other.changed == changed)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt)&&(identical(other.owner, owner) || other.owner == owner)&&(identical(other.public, public) || other.public == public));
}


@override
int get hashCode => Object.hash(runtimeType,id,name,comment,created,changed,coverArt,owner,public);

@override
String toString() {
  return 'Playlist(id: $id, name: $name, comment: $comment, created: $created, changed: $changed, coverArt: $coverArt, owner: $owner, public: $public)';
}


}

/// @nodoc
abstract mixin class _$PlaylistCopyWith<$Res> implements $PlaylistCopyWith<$Res> {
  factory _$PlaylistCopyWith(_Playlist value, $Res Function(_Playlist) _then) = __$PlaylistCopyWithImpl;
@override @useResult
$Res call({
 String id, String name, String? comment, DateTime created, DateTime changed, String? coverArt, String? owner, bool? public
});




}
/// @nodoc
class __$PlaylistCopyWithImpl<$Res>
    implements _$PlaylistCopyWith<$Res> {
  __$PlaylistCopyWithImpl(this._self, this._then);

  final _Playlist _self;
  final $Res Function(_Playlist) _then;

/// Create a copy of Playlist
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? name = null,Object? comment = freezed,Object? created = null,Object? changed = null,Object? coverArt = freezed,Object? owner = freezed,Object? public = freezed,}) {
  return _then(_Playlist(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,name: null == name ? _self.name : name // ignore: cast_nullable_to_non_nullable
as String,comment: freezed == comment ? _self.comment : comment // ignore: cast_nullable_to_non_nullable
as String?,created: null == created ? _self.created : created // ignore: cast_nullable_to_non_nullable
as DateTime,changed: null == changed ? _self.changed : changed // ignore: cast_nullable_to_non_nullable
as DateTime,coverArt: freezed == coverArt ? _self.coverArt : coverArt // ignore: cast_nullable_to_non_nullable
as String?,owner: freezed == owner ? _self.owner : owner // ignore: cast_nullable_to_non_nullable
as String?,public: freezed == public ? _self.public : public // ignore: cast_nullable_to_non_nullable
as bool?,
  ));
}


}

/// @nodoc
mixin _$Song {

 String get id; String? get albumId; String? get artistId; String get title; String? get artist; String? get album; Duration? get duration; int? get track; int? get disc; DateTime? get starred; String? get genre; String? get coverArt;
/// Create a copy of Song
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SongCopyWith<Song> get copyWith => _$SongCopyWithImpl<Song>(this as Song, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is Song&&(identical(other.id, id) || other.id == id)&&(identical(other.albumId, albumId) || other.albumId == albumId)&&(identical(other.artistId, artistId) || other.artistId == artistId)&&(identical(other.title, title) || other.title == title)&&(identical(other.artist, artist) || other.artist == artist)&&(identical(other.album, album) || other.album == album)&&(identical(other.duration, duration) || other.duration == duration)&&(identical(other.track, track) || other.track == track)&&(identical(other.disc, disc) || other.disc == disc)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.genre, genre) || other.genre == genre)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt));
}


@override
int get hashCode => Object.hash(runtimeType,id,albumId,artistId,title,artist,album,duration,track,disc,starred,genre,coverArt);

@override
String toString() {
  return 'Song(id: $id, albumId: $albumId, artistId: $artistId, title: $title, artist: $artist, album: $album, duration: $duration, track: $track, disc: $disc, starred: $starred, genre: $genre, coverArt: $coverArt)';
}


}

/// @nodoc
abstract mixin class $SongCopyWith<$Res>  {
  factory $SongCopyWith(Song value, $Res Function(Song) _then) = _$SongCopyWithImpl;
@useResult
$Res call({
 String id, String? albumId, String? artistId, String title, String? artist, String? album, Duration? duration, int? track, int? disc, DateTime? starred, String? genre, String? coverArt
});




}
/// @nodoc
class _$SongCopyWithImpl<$Res>
    implements $SongCopyWith<$Res> {
  _$SongCopyWithImpl(this._self, this._then);

  final Song _self;
  final $Res Function(Song) _then;

/// Create a copy of Song
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? albumId = freezed,Object? artistId = freezed,Object? title = null,Object? artist = freezed,Object? album = freezed,Object? duration = freezed,Object? track = freezed,Object? disc = freezed,Object? starred = freezed,Object? genre = freezed,Object? coverArt = freezed,}) {
  return _then(_self.copyWith(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,albumId: freezed == albumId ? _self.albumId : albumId // ignore: cast_nullable_to_non_nullable
as String?,artistId: freezed == artistId ? _self.artistId : artistId // ignore: cast_nullable_to_non_nullable
as String?,title: null == title ? _self.title : title // ignore: cast_nullable_to_non_nullable
as String,artist: freezed == artist ? _self.artist : artist // ignore: cast_nullable_to_non_nullable
as String?,album: freezed == album ? _self.album : album // ignore: cast_nullable_to_non_nullable
as String?,duration: freezed == duration ? _self.duration : duration // ignore: cast_nullable_to_non_nullable
as Duration?,track: freezed == track ? _self.track : track // ignore: cast_nullable_to_non_nullable
as int?,disc: freezed == disc ? _self.disc : disc // ignore: cast_nullable_to_non_nullable
as int?,starred: freezed == starred ? _self.starred : starred // ignore: cast_nullable_to_non_nullable
as DateTime?,genre: freezed == genre ? _self.genre : genre // ignore: cast_nullable_to_non_nullable
as String?,coverArt: freezed == coverArt ? _self.coverArt : coverArt // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}

}


/// Adds pattern-matching-related methods to [Song].
extension SongPatterns on Song {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _Song value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _Song() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _Song value)  $default,){
final _that = this;
switch (_that) {
case _Song():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _Song value)?  $default,){
final _that = this;
switch (_that) {
case _Song() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String id,  String? albumId,  String? artistId,  String title,  String? artist,  String? album,  Duration? duration,  int? track,  int? disc,  DateTime? starred,  String? genre,  String? coverArt)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _Song() when $default != null:
return $default(_that.id,_that.albumId,_that.artistId,_that.title,_that.artist,_that.album,_that.duration,_that.track,_that.disc,_that.starred,_that.genre,_that.coverArt);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String id,  String? albumId,  String? artistId,  String title,  String? artist,  String? album,  Duration? duration,  int? track,  int? disc,  DateTime? starred,  String? genre,  String? coverArt)  $default,) {final _that = this;
switch (_that) {
case _Song():
return $default(_that.id,_that.albumId,_that.artistId,_that.title,_that.artist,_that.album,_that.duration,_that.track,_that.disc,_that.starred,_that.genre,_that.coverArt);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String id,  String? albumId,  String? artistId,  String title,  String? artist,  String? album,  Duration? duration,  int? track,  int? disc,  DateTime? starred,  String? genre,  String? coverArt)?  $default,) {final _that = this;
switch (_that) {
case _Song() when $default != null:
return $default(_that.id,_that.albumId,_that.artistId,_that.title,_that.artist,_that.album,_that.duration,_that.track,_that.disc,_that.starred,_that.genre,_that.coverArt);case _:
  return null;

}
}

}

/// @nodoc


class _Song implements Song {
  const _Song({required this.id, this.albumId, this.artistId, required this.title, this.artist, this.album, this.duration, this.track, this.disc, this.starred, this.genre, this.coverArt});
  

@override final  String id;
@override final  String? albumId;
@override final  String? artistId;
@override final  String title;
@override final  String? artist;
@override final  String? album;
@override final  Duration? duration;
@override final  int? track;
@override final  int? disc;
@override final  DateTime? starred;
@override final  String? genre;
@override final  String? coverArt;

/// Create a copy of Song
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$SongCopyWith<_Song> get copyWith => __$SongCopyWithImpl<_Song>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _Song&&(identical(other.id, id) || other.id == id)&&(identical(other.albumId, albumId) || other.albumId == albumId)&&(identical(other.artistId, artistId) || other.artistId == artistId)&&(identical(other.title, title) || other.title == title)&&(identical(other.artist, artist) || other.artist == artist)&&(identical(other.album, album) || other.album == album)&&(identical(other.duration, duration) || other.duration == duration)&&(identical(other.track, track) || other.track == track)&&(identical(other.disc, disc) || other.disc == disc)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.genre, genre) || other.genre == genre)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt));
}


@override
int get hashCode => Object.hash(runtimeType,id,albumId,artistId,title,artist,album,duration,track,disc,starred,genre,coverArt);

@override
String toString() {
  return 'Song(id: $id, albumId: $albumId, artistId: $artistId, title: $title, artist: $artist, album: $album, duration: $duration, track: $track, disc: $disc, starred: $starred, genre: $genre, coverArt: $coverArt)';
}


}

/// @nodoc
abstract mixin class _$SongCopyWith<$Res> implements $SongCopyWith<$Res> {
  factory _$SongCopyWith(_Song value, $Res Function(_Song) _then) = __$SongCopyWithImpl;
@override @useResult
$Res call({
 String id, String? albumId, String? artistId, String title, String? artist, String? album, Duration? duration, int? track, int? disc, DateTime? starred, String? genre, String? coverArt
});




}
/// @nodoc
class __$SongCopyWithImpl<$Res>
    implements _$SongCopyWith<$Res> {
  __$SongCopyWithImpl(this._self, this._then);

  final _Song _self;
  final $Res Function(_Song) _then;

/// Create a copy of Song
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? albumId = freezed,Object? artistId = freezed,Object? title = null,Object? artist = freezed,Object? album = freezed,Object? duration = freezed,Object? track = freezed,Object? disc = freezed,Object? starred = freezed,Object? genre = freezed,Object? coverArt = freezed,}) {
  return _then(_Song(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,albumId: freezed == albumId ? _self.albumId : albumId // ignore: cast_nullable_to_non_nullable
as String?,artistId: freezed == artistId ? _self.artistId : artistId // ignore: cast_nullable_to_non_nullable
as String?,title: null == title ? _self.title : title // ignore: cast_nullable_to_non_nullable
as String,artist: freezed == artist ? _self.artist : artist // ignore: cast_nullable_to_non_nullable
as String?,album: freezed == album ? _self.album : album // ignore: cast_nullable_to_non_nullable
as String?,duration: freezed == duration ? _self.duration : duration // ignore: cast_nullable_to_non_nullable
as Duration?,track: freezed == track ? _self.track : track // ignore: cast_nullable_to_non_nullable
as int?,disc: freezed == disc ? _self.disc : disc // ignore: cast_nullable_to_non_nullable
as int?,starred: freezed == starred ? _self.starred : starred // ignore: cast_nullable_to_non_nullable
as DateTime?,genre: freezed == genre ? _self.genre : genre // ignore: cast_nullable_to_non_nullable
as String?,coverArt: freezed == coverArt ? _self.coverArt : coverArt // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}


}

/// @nodoc
mixin _$PlaylistSong {

 String get playlistId; String get songId; int get position;
/// Create a copy of PlaylistSong
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$PlaylistSongCopyWith<PlaylistSong> get copyWith => _$PlaylistSongCopyWithImpl<PlaylistSong>(this as PlaylistSong, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is PlaylistSong&&(identical(other.playlistId, playlistId) || other.playlistId == playlistId)&&(identical(other.songId, songId) || other.songId == songId)&&(identical(other.position, position) || other.position == position));
}


@override
int get hashCode => Object.hash(runtimeType,playlistId,songId,position);

@override
String toString() {
  return 'PlaylistSong(playlistId: $playlistId, songId: $songId, position: $position)';
}


}

/// @nodoc
abstract mixin class $PlaylistSongCopyWith<$Res>  {
  factory $PlaylistSongCopyWith(PlaylistSong value, $Res Function(PlaylistSong) _then) = _$PlaylistSongCopyWithImpl;
@useResult
$Res call({
 String playlistId, String songId, int position
});




}
/// @nodoc
class _$PlaylistSongCopyWithImpl<$Res>
    implements $PlaylistSongCopyWith<$Res> {
  _$PlaylistSongCopyWithImpl(this._self, this._then);

  final PlaylistSong _self;
  final $Res Function(PlaylistSong) _then;

/// Create a copy of PlaylistSong
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? playlistId = null,Object? songId = null,Object? position = null,}) {
  return _then(_self.copyWith(
playlistId: null == playlistId ? _self.playlistId : playlistId // ignore: cast_nullable_to_non_nullable
as String,songId: null == songId ? _self.songId : songId // ignore: cast_nullable_to_non_nullable
as String,position: null == position ? _self.position : position // ignore: cast_nullable_to_non_nullable
as int,
  ));
}

}


/// Adds pattern-matching-related methods to [PlaylistSong].
extension PlaylistSongPatterns on PlaylistSong {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _PlaylistSong value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _PlaylistSong() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _PlaylistSong value)  $default,){
final _that = this;
switch (_that) {
case _PlaylistSong():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _PlaylistSong value)?  $default,){
final _that = this;
switch (_that) {
case _PlaylistSong() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String playlistId,  String songId,  int position)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _PlaylistSong() when $default != null:
return $default(_that.playlistId,_that.songId,_that.position);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String playlistId,  String songId,  int position)  $default,) {final _that = this;
switch (_that) {
case _PlaylistSong():
return $default(_that.playlistId,_that.songId,_that.position);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String playlistId,  String songId,  int position)?  $default,) {final _that = this;
switch (_that) {
case _PlaylistSong() when $default != null:
return $default(_that.playlistId,_that.songId,_that.position);case _:
  return null;

}
}

}

/// @nodoc


class _PlaylistSong implements PlaylistSong {
  const _PlaylistSong({required this.playlistId, required this.songId, required this.position});
  

@override final  String playlistId;
@override final  String songId;
@override final  int position;

/// Create a copy of PlaylistSong
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$PlaylistSongCopyWith<_PlaylistSong> get copyWith => __$PlaylistSongCopyWithImpl<_PlaylistSong>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _PlaylistSong&&(identical(other.playlistId, playlistId) || other.playlistId == playlistId)&&(identical(other.songId, songId) || other.songId == songId)&&(identical(other.position, position) || other.position == position));
}


@override
int get hashCode => Object.hash(runtimeType,playlistId,songId,position);

@override
String toString() {
  return 'PlaylistSong(playlistId: $playlistId, songId: $songId, position: $position)';
}


}

/// @nodoc
abstract mixin class _$PlaylistSongCopyWith<$Res> implements $PlaylistSongCopyWith<$Res> {
  factory _$PlaylistSongCopyWith(_PlaylistSong value, $Res Function(_PlaylistSong) _then) = __$PlaylistSongCopyWithImpl;
@override @useResult
$Res call({
 String playlistId, String songId, int position
});




}
/// @nodoc
class __$PlaylistSongCopyWithImpl<$Res>
    implements _$PlaylistSongCopyWith<$Res> {
  __$PlaylistSongCopyWithImpl(this._self, this._then);

  final _PlaylistSong _self;
  final $Res Function(_PlaylistSong) _then;

/// Create a copy of PlaylistSong
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? playlistId = null,Object? songId = null,Object? position = null,}) {
  return _then(_PlaylistSong(
playlistId: null == playlistId ? _self.playlistId : playlistId // ignore: cast_nullable_to_non_nullable
as String,songId: null == songId ? _self.songId : songId // ignore: cast_nullable_to_non_nullable
as String,position: null == position ? _self.position : position // ignore: cast_nullable_to_non_nullable
as int,
  ));
}


}

// dart format on
