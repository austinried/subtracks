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
mixin _$SourceItem {

 String get id;
/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SourceItemCopyWith<SourceItem> get copyWith => _$SourceItemCopyWithImpl<SourceItem>(this as SourceItem, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SourceItem&&(identical(other.id, id) || other.id == id));
}


@override
int get hashCode => Object.hash(runtimeType,id);

@override
String toString() {
  return 'SourceItem(id: $id)';
}


}

/// @nodoc
abstract mixin class $SourceItemCopyWith<$Res>  {
  factory $SourceItemCopyWith(SourceItem value, $Res Function(SourceItem) _then) = _$SourceItemCopyWithImpl;
@useResult
$Res call({
 String id
});




}
/// @nodoc
class _$SourceItemCopyWithImpl<$Res>
    implements $SourceItemCopyWith<$Res> {
  _$SourceItemCopyWithImpl(this._self, this._then);

  final SourceItem _self;
  final $Res Function(SourceItem) _then;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,}) {
  return _then(_self.copyWith(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as String,
  ));
}

}


/// Adds pattern-matching-related methods to [SourceItem].
extension SourceItemPatterns on SourceItem {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>({TResult Function( SourceArtist value)?  artist,TResult Function( SourceAlbum value)?  album,TResult Function( SourcePlaylist value)?  playlist,TResult Function( SourceSong value)?  song,required TResult orElse(),}){
final _that = this;
switch (_that) {
case SourceArtist() when artist != null:
return artist(_that);case SourceAlbum() when album != null:
return album(_that);case SourcePlaylist() when playlist != null:
return playlist(_that);case SourceSong() when song != null:
return song(_that);case _:
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

@optionalTypeArgs TResult map<TResult extends Object?>({required TResult Function( SourceArtist value)  artist,required TResult Function( SourceAlbum value)  album,required TResult Function( SourcePlaylist value)  playlist,required TResult Function( SourceSong value)  song,}){
final _that = this;
switch (_that) {
case SourceArtist():
return artist(_that);case SourceAlbum():
return album(_that);case SourcePlaylist():
return playlist(_that);case SourceSong():
return song(_that);case _:
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>({TResult? Function( SourceArtist value)?  artist,TResult? Function( SourceAlbum value)?  album,TResult? Function( SourcePlaylist value)?  playlist,TResult? Function( SourceSong value)?  song,}){
final _that = this;
switch (_that) {
case SourceArtist() when artist != null:
return artist(_that);case SourceAlbum() when album != null:
return album(_that);case SourcePlaylist() when playlist != null:
return playlist(_that);case SourceSong() when song != null:
return song(_that);case _:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>({TResult Function( String id,  String name,  DateTime? starred,  Uri? smallImage,  Uri? largeImage)?  artist,TResult Function( String id,  String? artistId,  String name,  String? albumArtist,  DateTime created,  String? coverArt,  int? year,  DateTime? starred,  String? genre,  int? frequentRank,  int? recentRank)?  album,TResult Function( String id,  String name,  String? comment,  DateTime created,  DateTime changed,  String? coverArt,  String? owner,  bool? public)?  playlist,TResult Function( String id,  String? albumId,  String? artistId,  String title,  String? artist,  String? album,  Duration? duration,  int? track,  int? disc,  DateTime? starred,  String? genre,  String? coverArt)?  song,required TResult orElse(),}) {final _that = this;
switch (_that) {
case SourceArtist() when artist != null:
return artist(_that.id,_that.name,_that.starred,_that.smallImage,_that.largeImage);case SourceAlbum() when album != null:
return album(_that.id,_that.artistId,_that.name,_that.albumArtist,_that.created,_that.coverArt,_that.year,_that.starred,_that.genre,_that.frequentRank,_that.recentRank);case SourcePlaylist() when playlist != null:
return playlist(_that.id,_that.name,_that.comment,_that.created,_that.changed,_that.coverArt,_that.owner,_that.public);case SourceSong() when song != null:
return song(_that.id,_that.albumId,_that.artistId,_that.title,_that.artist,_that.album,_that.duration,_that.track,_that.disc,_that.starred,_that.genre,_that.coverArt);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>({required TResult Function( String id,  String name,  DateTime? starred,  Uri? smallImage,  Uri? largeImage)  artist,required TResult Function( String id,  String? artistId,  String name,  String? albumArtist,  DateTime created,  String? coverArt,  int? year,  DateTime? starred,  String? genre,  int? frequentRank,  int? recentRank)  album,required TResult Function( String id,  String name,  String? comment,  DateTime created,  DateTime changed,  String? coverArt,  String? owner,  bool? public)  playlist,required TResult Function( String id,  String? albumId,  String? artistId,  String title,  String? artist,  String? album,  Duration? duration,  int? track,  int? disc,  DateTime? starred,  String? genre,  String? coverArt)  song,}) {final _that = this;
switch (_that) {
case SourceArtist():
return artist(_that.id,_that.name,_that.starred,_that.smallImage,_that.largeImage);case SourceAlbum():
return album(_that.id,_that.artistId,_that.name,_that.albumArtist,_that.created,_that.coverArt,_that.year,_that.starred,_that.genre,_that.frequentRank,_that.recentRank);case SourcePlaylist():
return playlist(_that.id,_that.name,_that.comment,_that.created,_that.changed,_that.coverArt,_that.owner,_that.public);case SourceSong():
return song(_that.id,_that.albumId,_that.artistId,_that.title,_that.artist,_that.album,_that.duration,_that.track,_that.disc,_that.starred,_that.genre,_that.coverArt);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>({TResult? Function( String id,  String name,  DateTime? starred,  Uri? smallImage,  Uri? largeImage)?  artist,TResult? Function( String id,  String? artistId,  String name,  String? albumArtist,  DateTime created,  String? coverArt,  int? year,  DateTime? starred,  String? genre,  int? frequentRank,  int? recentRank)?  album,TResult? Function( String id,  String name,  String? comment,  DateTime created,  DateTime changed,  String? coverArt,  String? owner,  bool? public)?  playlist,TResult? Function( String id,  String? albumId,  String? artistId,  String title,  String? artist,  String? album,  Duration? duration,  int? track,  int? disc,  DateTime? starred,  String? genre,  String? coverArt)?  song,}) {final _that = this;
switch (_that) {
case SourceArtist() when artist != null:
return artist(_that.id,_that.name,_that.starred,_that.smallImage,_that.largeImage);case SourceAlbum() when album != null:
return album(_that.id,_that.artistId,_that.name,_that.albumArtist,_that.created,_that.coverArt,_that.year,_that.starred,_that.genre,_that.frequentRank,_that.recentRank);case SourcePlaylist() when playlist != null:
return playlist(_that.id,_that.name,_that.comment,_that.created,_that.changed,_that.coverArt,_that.owner,_that.public);case SourceSong() when song != null:
return song(_that.id,_that.albumId,_that.artistId,_that.title,_that.artist,_that.album,_that.duration,_that.track,_that.disc,_that.starred,_that.genre,_that.coverArt);case _:
  return null;

}
}

}

/// @nodoc


class SourceArtist with Starred implements SourceItem {
  const SourceArtist({required this.id, required this.name, this.starred, this.smallImage, this.largeImage});
  

@override final  String id;
 final  String name;
 final  DateTime? starred;
 final  Uri? smallImage;
 final  Uri? largeImage;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SourceArtistCopyWith<SourceArtist> get copyWith => _$SourceArtistCopyWithImpl<SourceArtist>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SourceArtist&&(identical(other.id, id) || other.id == id)&&(identical(other.name, name) || other.name == name)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.smallImage, smallImage) || other.smallImage == smallImage)&&(identical(other.largeImage, largeImage) || other.largeImage == largeImage));
}


@override
int get hashCode => Object.hash(runtimeType,id,name,starred,smallImage,largeImage);

@override
String toString() {
  return 'SourceItem.artist(id: $id, name: $name, starred: $starred, smallImage: $smallImage, largeImage: $largeImage)';
}


}

/// @nodoc
abstract mixin class $SourceArtistCopyWith<$Res> implements $SourceItemCopyWith<$Res> {
  factory $SourceArtistCopyWith(SourceArtist value, $Res Function(SourceArtist) _then) = _$SourceArtistCopyWithImpl;
@override @useResult
$Res call({
 String id, String name, DateTime? starred, Uri? smallImage, Uri? largeImage
});




}
/// @nodoc
class _$SourceArtistCopyWithImpl<$Res>
    implements $SourceArtistCopyWith<$Res> {
  _$SourceArtistCopyWithImpl(this._self, this._then);

  final SourceArtist _self;
  final $Res Function(SourceArtist) _then;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? name = null,Object? starred = freezed,Object? smallImage = freezed,Object? largeImage = freezed,}) {
  return _then(SourceArtist(
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


class SourceAlbum with Starred, CoverArt implements SourceItem {
  const SourceAlbum({required this.id, this.artistId, required this.name, this.albumArtist, required this.created, this.coverArt, this.year, this.starred, this.genre, this.frequentRank, this.recentRank});
  

@override final  String id;
 final  String? artistId;
 final  String name;
 final  String? albumArtist;
 final  DateTime created;
 final  String? coverArt;
 final  int? year;
 final  DateTime? starred;
 final  String? genre;
 final  int? frequentRank;
 final  int? recentRank;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SourceAlbumCopyWith<SourceAlbum> get copyWith => _$SourceAlbumCopyWithImpl<SourceAlbum>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SourceAlbum&&(identical(other.id, id) || other.id == id)&&(identical(other.artistId, artistId) || other.artistId == artistId)&&(identical(other.name, name) || other.name == name)&&(identical(other.albumArtist, albumArtist) || other.albumArtist == albumArtist)&&(identical(other.created, created) || other.created == created)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt)&&(identical(other.year, year) || other.year == year)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.genre, genre) || other.genre == genre)&&(identical(other.frequentRank, frequentRank) || other.frequentRank == frequentRank)&&(identical(other.recentRank, recentRank) || other.recentRank == recentRank));
}


@override
int get hashCode => Object.hash(runtimeType,id,artistId,name,albumArtist,created,coverArt,year,starred,genre,frequentRank,recentRank);

@override
String toString() {
  return 'SourceItem.album(id: $id, artistId: $artistId, name: $name, albumArtist: $albumArtist, created: $created, coverArt: $coverArt, year: $year, starred: $starred, genre: $genre, frequentRank: $frequentRank, recentRank: $recentRank)';
}


}

/// @nodoc
abstract mixin class $SourceAlbumCopyWith<$Res> implements $SourceItemCopyWith<$Res> {
  factory $SourceAlbumCopyWith(SourceAlbum value, $Res Function(SourceAlbum) _then) = _$SourceAlbumCopyWithImpl;
@override @useResult
$Res call({
 String id, String? artistId, String name, String? albumArtist, DateTime created, String? coverArt, int? year, DateTime? starred, String? genre, int? frequentRank, int? recentRank
});




}
/// @nodoc
class _$SourceAlbumCopyWithImpl<$Res>
    implements $SourceAlbumCopyWith<$Res> {
  _$SourceAlbumCopyWithImpl(this._self, this._then);

  final SourceAlbum _self;
  final $Res Function(SourceAlbum) _then;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? artistId = freezed,Object? name = null,Object? albumArtist = freezed,Object? created = null,Object? coverArt = freezed,Object? year = freezed,Object? starred = freezed,Object? genre = freezed,Object? frequentRank = freezed,Object? recentRank = freezed,}) {
  return _then(SourceAlbum(
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


class SourcePlaylist with CoverArt implements SourceItem {
  const SourcePlaylist({required this.id, required this.name, this.comment, required this.created, required this.changed, this.coverArt, this.owner, this.public});
  

@override final  String id;
 final  String name;
 final  String? comment;
 final  DateTime created;
 final  DateTime changed;
 final  String? coverArt;
 final  String? owner;
 final  bool? public;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SourcePlaylistCopyWith<SourcePlaylist> get copyWith => _$SourcePlaylistCopyWithImpl<SourcePlaylist>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SourcePlaylist&&(identical(other.id, id) || other.id == id)&&(identical(other.name, name) || other.name == name)&&(identical(other.comment, comment) || other.comment == comment)&&(identical(other.created, created) || other.created == created)&&(identical(other.changed, changed) || other.changed == changed)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt)&&(identical(other.owner, owner) || other.owner == owner)&&(identical(other.public, public) || other.public == public));
}


@override
int get hashCode => Object.hash(runtimeType,id,name,comment,created,changed,coverArt,owner,public);

@override
String toString() {
  return 'SourceItem.playlist(id: $id, name: $name, comment: $comment, created: $created, changed: $changed, coverArt: $coverArt, owner: $owner, public: $public)';
}


}

/// @nodoc
abstract mixin class $SourcePlaylistCopyWith<$Res> implements $SourceItemCopyWith<$Res> {
  factory $SourcePlaylistCopyWith(SourcePlaylist value, $Res Function(SourcePlaylist) _then) = _$SourcePlaylistCopyWithImpl;
@override @useResult
$Res call({
 String id, String name, String? comment, DateTime created, DateTime changed, String? coverArt, String? owner, bool? public
});




}
/// @nodoc
class _$SourcePlaylistCopyWithImpl<$Res>
    implements $SourcePlaylistCopyWith<$Res> {
  _$SourcePlaylistCopyWithImpl(this._self, this._then);

  final SourcePlaylist _self;
  final $Res Function(SourcePlaylist) _then;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? name = null,Object? comment = freezed,Object? created = null,Object? changed = null,Object? coverArt = freezed,Object? owner = freezed,Object? public = freezed,}) {
  return _then(SourcePlaylist(
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


class SourceSong with Starred, CoverArt implements SourceItem {
  const SourceSong({required this.id, this.albumId, this.artistId, required this.title, this.artist, this.album, this.duration, this.track, this.disc, this.starred, this.genre, this.coverArt});
  

@override final  String id;
 final  String? albumId;
 final  String? artistId;
 final  String title;
 final  String? artist;
 final  String? album;
 final  Duration? duration;
 final  int? track;
 final  int? disc;
 final  DateTime? starred;
 final  String? genre;
 final  String? coverArt;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SourceSongCopyWith<SourceSong> get copyWith => _$SourceSongCopyWithImpl<SourceSong>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SourceSong&&(identical(other.id, id) || other.id == id)&&(identical(other.albumId, albumId) || other.albumId == albumId)&&(identical(other.artistId, artistId) || other.artistId == artistId)&&(identical(other.title, title) || other.title == title)&&(identical(other.artist, artist) || other.artist == artist)&&(identical(other.album, album) || other.album == album)&&(identical(other.duration, duration) || other.duration == duration)&&(identical(other.track, track) || other.track == track)&&(identical(other.disc, disc) || other.disc == disc)&&(identical(other.starred, starred) || other.starred == starred)&&(identical(other.genre, genre) || other.genre == genre)&&(identical(other.coverArt, coverArt) || other.coverArt == coverArt));
}


@override
int get hashCode => Object.hash(runtimeType,id,albumId,artistId,title,artist,album,duration,track,disc,starred,genre,coverArt);

@override
String toString() {
  return 'SourceItem.song(id: $id, albumId: $albumId, artistId: $artistId, title: $title, artist: $artist, album: $album, duration: $duration, track: $track, disc: $disc, starred: $starred, genre: $genre, coverArt: $coverArt)';
}


}

/// @nodoc
abstract mixin class $SourceSongCopyWith<$Res> implements $SourceItemCopyWith<$Res> {
  factory $SourceSongCopyWith(SourceSong value, $Res Function(SourceSong) _then) = _$SourceSongCopyWithImpl;
@override @useResult
$Res call({
 String id, String? albumId, String? artistId, String title, String? artist, String? album, Duration? duration, int? track, int? disc, DateTime? starred, String? genre, String? coverArt
});




}
/// @nodoc
class _$SourceSongCopyWithImpl<$Res>
    implements $SourceSongCopyWith<$Res> {
  _$SourceSongCopyWithImpl(this._self, this._then);

  final SourceSong _self;
  final $Res Function(SourceSong) _then;

/// Create a copy of SourceItem
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? albumId = freezed,Object? artistId = freezed,Object? title = null,Object? artist = freezed,Object? album = freezed,Object? duration = freezed,Object? track = freezed,Object? disc = freezed,Object? starred = freezed,Object? genre = freezed,Object? coverArt = freezed,}) {
  return _then(SourceSong(
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
mixin _$SourcePlaylistSong {

 String get playlistId; String get songId; int get position;
/// Create a copy of SourcePlaylistSong
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SourcePlaylistSongCopyWith<SourcePlaylistSong> get copyWith => _$SourcePlaylistSongCopyWithImpl<SourcePlaylistSong>(this as SourcePlaylistSong, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SourcePlaylistSong&&(identical(other.playlistId, playlistId) || other.playlistId == playlistId)&&(identical(other.songId, songId) || other.songId == songId)&&(identical(other.position, position) || other.position == position));
}


@override
int get hashCode => Object.hash(runtimeType,playlistId,songId,position);

@override
String toString() {
  return 'SourcePlaylistSong(playlistId: $playlistId, songId: $songId, position: $position)';
}


}

/// @nodoc
abstract mixin class $SourcePlaylistSongCopyWith<$Res>  {
  factory $SourcePlaylistSongCopyWith(SourcePlaylistSong value, $Res Function(SourcePlaylistSong) _then) = _$SourcePlaylistSongCopyWithImpl;
@useResult
$Res call({
 String playlistId, String songId, int position
});




}
/// @nodoc
class _$SourcePlaylistSongCopyWithImpl<$Res>
    implements $SourcePlaylistSongCopyWith<$Res> {
  _$SourcePlaylistSongCopyWithImpl(this._self, this._then);

  final SourcePlaylistSong _self;
  final $Res Function(SourcePlaylistSong) _then;

/// Create a copy of SourcePlaylistSong
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


/// Adds pattern-matching-related methods to [SourcePlaylistSong].
extension SourcePlaylistSongPatterns on SourcePlaylistSong {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _SourcePlaylistSong value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _SourcePlaylistSong() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _SourcePlaylistSong value)  $default,){
final _that = this;
switch (_that) {
case _SourcePlaylistSong():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _SourcePlaylistSong value)?  $default,){
final _that = this;
switch (_that) {
case _SourcePlaylistSong() when $default != null:
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
case _SourcePlaylistSong() when $default != null:
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
case _SourcePlaylistSong():
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
case _SourcePlaylistSong() when $default != null:
return $default(_that.playlistId,_that.songId,_that.position);case _:
  return null;

}
}

}

/// @nodoc


class _SourcePlaylistSong implements SourcePlaylistSong {
  const _SourcePlaylistSong({required this.playlistId, required this.songId, required this.position});
  

@override final  String playlistId;
@override final  String songId;
@override final  int position;

/// Create a copy of SourcePlaylistSong
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$SourcePlaylistSongCopyWith<_SourcePlaylistSong> get copyWith => __$SourcePlaylistSongCopyWithImpl<_SourcePlaylistSong>(this, _$identity);



@override
bool operator ==(Object other) {
  return identical(this, other) || (other.runtimeType == runtimeType&&other is _SourcePlaylistSong&&(identical(other.playlistId, playlistId) || other.playlistId == playlistId)&&(identical(other.songId, songId) || other.songId == songId)&&(identical(other.position, position) || other.position == position));
}


@override
int get hashCode => Object.hash(runtimeType,playlistId,songId,position);

@override
String toString() {
  return 'SourcePlaylistSong(playlistId: $playlistId, songId: $songId, position: $position)';
}


}

/// @nodoc
abstract mixin class _$SourcePlaylistSongCopyWith<$Res> implements $SourcePlaylistSongCopyWith<$Res> {
  factory _$SourcePlaylistSongCopyWith(_SourcePlaylistSong value, $Res Function(_SourcePlaylistSong) _then) = __$SourcePlaylistSongCopyWithImpl;
@override @useResult
$Res call({
 String playlistId, String songId, int position
});




}
/// @nodoc
class __$SourcePlaylistSongCopyWithImpl<$Res>
    implements _$SourcePlaylistSongCopyWith<$Res> {
  __$SourcePlaylistSongCopyWithImpl(this._self, this._then);

  final _SourcePlaylistSong _self;
  final $Res Function(_SourcePlaylistSong) _then;

/// Create a copy of SourcePlaylistSong
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? playlistId = null,Object? songId = null,Object? position = null,}) {
  return _then(_SourcePlaylistSong(
playlistId: null == playlistId ? _self.playlistId : playlistId // ignore: cast_nullable_to_non_nullable
as String,songId: null == songId ? _self.songId : songId // ignore: cast_nullable_to_non_nullable
as String,position: null == position ? _self.position : position // ignore: cast_nullable_to_non_nullable
as int,
  ));
}


}

// dart format on
