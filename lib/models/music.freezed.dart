// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'music.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$SourceId {
  int get sourceId => throw _privateConstructorUsedError;
  String get id => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $SourceIdCopyWith<SourceId> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SourceIdCopyWith<$Res> {
  factory $SourceIdCopyWith(SourceId value, $Res Function(SourceId) then) =
      _$SourceIdCopyWithImpl<$Res, SourceId>;
  @useResult
  $Res call({int sourceId, String id});
}

/// @nodoc
class _$SourceIdCopyWithImpl<$Res, $Val extends SourceId>
    implements $SourceIdCopyWith<$Res> {
  _$SourceIdCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
  }) {
    return _then(_value.copyWith(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SourceIdImplCopyWith<$Res>
    implements $SourceIdCopyWith<$Res> {
  factory _$$SourceIdImplCopyWith(
          _$SourceIdImpl value, $Res Function(_$SourceIdImpl) then) =
      __$$SourceIdImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int sourceId, String id});
}

/// @nodoc
class __$$SourceIdImplCopyWithImpl<$Res>
    extends _$SourceIdCopyWithImpl<$Res, _$SourceIdImpl>
    implements _$$SourceIdImplCopyWith<$Res> {
  __$$SourceIdImplCopyWithImpl(
      _$SourceIdImpl _value, $Res Function(_$SourceIdImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
  }) {
    return _then(_$SourceIdImpl(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$SourceIdImpl with DiagnosticableTreeMixin implements _SourceId {
  const _$SourceIdImpl({required this.sourceId, required this.id});

  @override
  final int sourceId;
  @override
  final String id;

  @override
  String toString({DiagnosticLevel minLevel = DiagnosticLevel.info}) {
    return 'SourceId(sourceId: $sourceId, id: $id)';
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
      ..add(DiagnosticsProperty('type', 'SourceId'))
      ..add(DiagnosticsProperty('sourceId', sourceId))
      ..add(DiagnosticsProperty('id', id));
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SourceIdImpl &&
            (identical(other.sourceId, sourceId) ||
                other.sourceId == sourceId) &&
            (identical(other.id, id) || other.id == id));
  }

  @override
  int get hashCode => Object.hash(runtimeType, sourceId, id);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SourceIdImplCopyWith<_$SourceIdImpl> get copyWith =>
      __$$SourceIdImplCopyWithImpl<_$SourceIdImpl>(this, _$identity);
}

abstract class _SourceId implements SourceId {
  const factory _SourceId(
      {required final int sourceId, required final String id}) = _$SourceIdImpl;

  @override
  int get sourceId;
  @override
  String get id;
  @override
  @JsonKey(ignore: true)
  _$$SourceIdImplCopyWith<_$SourceIdImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$SourceIdSet {
  int get sourceId => throw _privateConstructorUsedError;
  ISet<String> get ids => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $SourceIdSetCopyWith<SourceIdSet> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SourceIdSetCopyWith<$Res> {
  factory $SourceIdSetCopyWith(
          SourceIdSet value, $Res Function(SourceIdSet) then) =
      _$SourceIdSetCopyWithImpl<$Res, SourceIdSet>;
  @useResult
  $Res call({int sourceId, ISet<String> ids});
}

/// @nodoc
class _$SourceIdSetCopyWithImpl<$Res, $Val extends SourceIdSet>
    implements $SourceIdSetCopyWith<$Res> {
  _$SourceIdSetCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? ids = null,
  }) {
    return _then(_value.copyWith(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      ids: null == ids
          ? _value.ids
          : ids // ignore: cast_nullable_to_non_nullable
              as ISet<String>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SourceIdSetImplCopyWith<$Res>
    implements $SourceIdSetCopyWith<$Res> {
  factory _$$SourceIdSetImplCopyWith(
          _$SourceIdSetImpl value, $Res Function(_$SourceIdSetImpl) then) =
      __$$SourceIdSetImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int sourceId, ISet<String> ids});
}

/// @nodoc
class __$$SourceIdSetImplCopyWithImpl<$Res>
    extends _$SourceIdSetCopyWithImpl<$Res, _$SourceIdSetImpl>
    implements _$$SourceIdSetImplCopyWith<$Res> {
  __$$SourceIdSetImplCopyWithImpl(
      _$SourceIdSetImpl _value, $Res Function(_$SourceIdSetImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? ids = null,
  }) {
    return _then(_$SourceIdSetImpl(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      ids: null == ids
          ? _value.ids
          : ids // ignore: cast_nullable_to_non_nullable
              as ISet<String>,
    ));
  }
}

/// @nodoc

class _$SourceIdSetImpl with DiagnosticableTreeMixin implements _SourceIdSet {
  const _$SourceIdSetImpl({required this.sourceId, required this.ids});

  @override
  final int sourceId;
  @override
  final ISet<String> ids;

  @override
  String toString({DiagnosticLevel minLevel = DiagnosticLevel.info}) {
    return 'SourceIdSet(sourceId: $sourceId, ids: $ids)';
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
      ..add(DiagnosticsProperty('type', 'SourceIdSet'))
      ..add(DiagnosticsProperty('sourceId', sourceId))
      ..add(DiagnosticsProperty('ids', ids));
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SourceIdSetImpl &&
            (identical(other.sourceId, sourceId) ||
                other.sourceId == sourceId) &&
            const DeepCollectionEquality().equals(other.ids, ids));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, sourceId, const DeepCollectionEquality().hash(ids));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SourceIdSetImplCopyWith<_$SourceIdSetImpl> get copyWith =>
      __$$SourceIdSetImplCopyWithImpl<_$SourceIdSetImpl>(this, _$identity);
}

abstract class _SourceIdSet implements SourceIdSet {
  const factory _SourceIdSet(
      {required final int sourceId,
      required final ISet<String> ids}) = _$SourceIdSetImpl;

  @override
  int get sourceId;
  @override
  ISet<String> get ids;
  @override
  @JsonKey(ignore: true)
  _$$SourceIdSetImplCopyWith<_$SourceIdSetImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$Artist {
  int get sourceId => throw _privateConstructorUsedError;
  String get id => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  int get albumCount => throw _privateConstructorUsedError;
  DateTime? get starred => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $ArtistCopyWith<Artist> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ArtistCopyWith<$Res> {
  factory $ArtistCopyWith(Artist value, $Res Function(Artist) then) =
      _$ArtistCopyWithImpl<$Res, Artist>;
  @useResult
  $Res call(
      {int sourceId,
      String id,
      String name,
      int albumCount,
      DateTime? starred});
}

/// @nodoc
class _$ArtistCopyWithImpl<$Res, $Val extends Artist>
    implements $ArtistCopyWith<$Res> {
  _$ArtistCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
    Object? name = null,
    Object? albumCount = null,
    Object? starred = freezed,
  }) {
    return _then(_value.copyWith(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      albumCount: null == albumCount
          ? _value.albumCount
          : albumCount // ignore: cast_nullable_to_non_nullable
              as int,
      starred: freezed == starred
          ? _value.starred
          : starred // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ArtistImplCopyWith<$Res> implements $ArtistCopyWith<$Res> {
  factory _$$ArtistImplCopyWith(
          _$ArtistImpl value, $Res Function(_$ArtistImpl) then) =
      __$$ArtistImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int sourceId,
      String id,
      String name,
      int albumCount,
      DateTime? starred});
}

/// @nodoc
class __$$ArtistImplCopyWithImpl<$Res>
    extends _$ArtistCopyWithImpl<$Res, _$ArtistImpl>
    implements _$$ArtistImplCopyWith<$Res> {
  __$$ArtistImplCopyWithImpl(
      _$ArtistImpl _value, $Res Function(_$ArtistImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
    Object? name = null,
    Object? albumCount = null,
    Object? starred = freezed,
  }) {
    return _then(_$ArtistImpl(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      albumCount: null == albumCount
          ? _value.albumCount
          : albumCount // ignore: cast_nullable_to_non_nullable
              as int,
      starred: freezed == starred
          ? _value.starred
          : starred // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }
}

/// @nodoc

class _$ArtistImpl with DiagnosticableTreeMixin implements _Artist {
  const _$ArtistImpl(
      {required this.sourceId,
      required this.id,
      required this.name,
      required this.albumCount,
      this.starred});

  @override
  final int sourceId;
  @override
  final String id;
  @override
  final String name;
  @override
  final int albumCount;
  @override
  final DateTime? starred;

  @override
  String toString({DiagnosticLevel minLevel = DiagnosticLevel.info}) {
    return 'Artist(sourceId: $sourceId, id: $id, name: $name, albumCount: $albumCount, starred: $starred)';
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
      ..add(DiagnosticsProperty('type', 'Artist'))
      ..add(DiagnosticsProperty('sourceId', sourceId))
      ..add(DiagnosticsProperty('id', id))
      ..add(DiagnosticsProperty('name', name))
      ..add(DiagnosticsProperty('albumCount', albumCount))
      ..add(DiagnosticsProperty('starred', starred));
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ArtistImpl &&
            (identical(other.sourceId, sourceId) ||
                other.sourceId == sourceId) &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.albumCount, albumCount) ||
                other.albumCount == albumCount) &&
            (identical(other.starred, starred) || other.starred == starred));
  }

  @override
  int get hashCode =>
      Object.hash(runtimeType, sourceId, id, name, albumCount, starred);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$ArtistImplCopyWith<_$ArtistImpl> get copyWith =>
      __$$ArtistImplCopyWithImpl<_$ArtistImpl>(this, _$identity);
}

abstract class _Artist implements Artist {
  const factory _Artist(
      {required final int sourceId,
      required final String id,
      required final String name,
      required final int albumCount,
      final DateTime? starred}) = _$ArtistImpl;

  @override
  int get sourceId;
  @override
  String get id;
  @override
  String get name;
  @override
  int get albumCount;
  @override
  DateTime? get starred;
  @override
  @JsonKey(ignore: true)
  _$$ArtistImplCopyWith<_$ArtistImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$Album {
  int get sourceId => throw _privateConstructorUsedError;
  String get id => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  String? get artistId => throw _privateConstructorUsedError;
  String? get albumArtist => throw _privateConstructorUsedError;
  DateTime get created => throw _privateConstructorUsedError;
  String? get coverArt => throw _privateConstructorUsedError;
  int? get year => throw _privateConstructorUsedError;
  DateTime? get starred =>
      throw _privateConstructorUsedError; // DateTime? synced,
  String? get genre => throw _privateConstructorUsedError;
  int get songCount => throw _privateConstructorUsedError;
  bool get isDeleted =>
      throw _privateConstructorUsedError; // @Default(IListConst([])) IList<Song> songs,
  int? get frequentRank => throw _privateConstructorUsedError;
  int? get recentRank => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $AlbumCopyWith<Album> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AlbumCopyWith<$Res> {
  factory $AlbumCopyWith(Album value, $Res Function(Album) then) =
      _$AlbumCopyWithImpl<$Res, Album>;
  @useResult
  $Res call(
      {int sourceId,
      String id,
      String name,
      String? artistId,
      String? albumArtist,
      DateTime created,
      String? coverArt,
      int? year,
      DateTime? starred,
      String? genre,
      int songCount,
      bool isDeleted,
      int? frequentRank,
      int? recentRank});
}

/// @nodoc
class _$AlbumCopyWithImpl<$Res, $Val extends Album>
    implements $AlbumCopyWith<$Res> {
  _$AlbumCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
    Object? name = null,
    Object? artistId = freezed,
    Object? albumArtist = freezed,
    Object? created = null,
    Object? coverArt = freezed,
    Object? year = freezed,
    Object? starred = freezed,
    Object? genre = freezed,
    Object? songCount = null,
    Object? isDeleted = null,
    Object? frequentRank = freezed,
    Object? recentRank = freezed,
  }) {
    return _then(_value.copyWith(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      artistId: freezed == artistId
          ? _value.artistId
          : artistId // ignore: cast_nullable_to_non_nullable
              as String?,
      albumArtist: freezed == albumArtist
          ? _value.albumArtist
          : albumArtist // ignore: cast_nullable_to_non_nullable
              as String?,
      created: null == created
          ? _value.created
          : created // ignore: cast_nullable_to_non_nullable
              as DateTime,
      coverArt: freezed == coverArt
          ? _value.coverArt
          : coverArt // ignore: cast_nullable_to_non_nullable
              as String?,
      year: freezed == year
          ? _value.year
          : year // ignore: cast_nullable_to_non_nullable
              as int?,
      starred: freezed == starred
          ? _value.starred
          : starred // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      genre: freezed == genre
          ? _value.genre
          : genre // ignore: cast_nullable_to_non_nullable
              as String?,
      songCount: null == songCount
          ? _value.songCount
          : songCount // ignore: cast_nullable_to_non_nullable
              as int,
      isDeleted: null == isDeleted
          ? _value.isDeleted
          : isDeleted // ignore: cast_nullable_to_non_nullable
              as bool,
      frequentRank: freezed == frequentRank
          ? _value.frequentRank
          : frequentRank // ignore: cast_nullable_to_non_nullable
              as int?,
      recentRank: freezed == recentRank
          ? _value.recentRank
          : recentRank // ignore: cast_nullable_to_non_nullable
              as int?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AlbumImplCopyWith<$Res> implements $AlbumCopyWith<$Res> {
  factory _$$AlbumImplCopyWith(
          _$AlbumImpl value, $Res Function(_$AlbumImpl) then) =
      __$$AlbumImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int sourceId,
      String id,
      String name,
      String? artistId,
      String? albumArtist,
      DateTime created,
      String? coverArt,
      int? year,
      DateTime? starred,
      String? genre,
      int songCount,
      bool isDeleted,
      int? frequentRank,
      int? recentRank});
}

/// @nodoc
class __$$AlbumImplCopyWithImpl<$Res>
    extends _$AlbumCopyWithImpl<$Res, _$AlbumImpl>
    implements _$$AlbumImplCopyWith<$Res> {
  __$$AlbumImplCopyWithImpl(
      _$AlbumImpl _value, $Res Function(_$AlbumImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
    Object? name = null,
    Object? artistId = freezed,
    Object? albumArtist = freezed,
    Object? created = null,
    Object? coverArt = freezed,
    Object? year = freezed,
    Object? starred = freezed,
    Object? genre = freezed,
    Object? songCount = null,
    Object? isDeleted = null,
    Object? frequentRank = freezed,
    Object? recentRank = freezed,
  }) {
    return _then(_$AlbumImpl(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      artistId: freezed == artistId
          ? _value.artistId
          : artistId // ignore: cast_nullable_to_non_nullable
              as String?,
      albumArtist: freezed == albumArtist
          ? _value.albumArtist
          : albumArtist // ignore: cast_nullable_to_non_nullable
              as String?,
      created: null == created
          ? _value.created
          : created // ignore: cast_nullable_to_non_nullable
              as DateTime,
      coverArt: freezed == coverArt
          ? _value.coverArt
          : coverArt // ignore: cast_nullable_to_non_nullable
              as String?,
      year: freezed == year
          ? _value.year
          : year // ignore: cast_nullable_to_non_nullable
              as int?,
      starred: freezed == starred
          ? _value.starred
          : starred // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      genre: freezed == genre
          ? _value.genre
          : genre // ignore: cast_nullable_to_non_nullable
              as String?,
      songCount: null == songCount
          ? _value.songCount
          : songCount // ignore: cast_nullable_to_non_nullable
              as int,
      isDeleted: null == isDeleted
          ? _value.isDeleted
          : isDeleted // ignore: cast_nullable_to_non_nullable
              as bool,
      frequentRank: freezed == frequentRank
          ? _value.frequentRank
          : frequentRank // ignore: cast_nullable_to_non_nullable
              as int?,
      recentRank: freezed == recentRank
          ? _value.recentRank
          : recentRank // ignore: cast_nullable_to_non_nullable
              as int?,
    ));
  }
}

/// @nodoc

class _$AlbumImpl with DiagnosticableTreeMixin implements _Album {
  const _$AlbumImpl(
      {required this.sourceId,
      required this.id,
      required this.name,
      this.artistId,
      this.albumArtist,
      required this.created,
      this.coverArt,
      this.year,
      this.starred,
      this.genre,
      required this.songCount,
      this.isDeleted = false,
      this.frequentRank,
      this.recentRank});

  @override
  final int sourceId;
  @override
  final String id;
  @override
  final String name;
  @override
  final String? artistId;
  @override
  final String? albumArtist;
  @override
  final DateTime created;
  @override
  final String? coverArt;
  @override
  final int? year;
  @override
  final DateTime? starred;
// DateTime? synced,
  @override
  final String? genre;
  @override
  final int songCount;
  @override
  @JsonKey()
  final bool isDeleted;
// @Default(IListConst([])) IList<Song> songs,
  @override
  final int? frequentRank;
  @override
  final int? recentRank;

  @override
  String toString({DiagnosticLevel minLevel = DiagnosticLevel.info}) {
    return 'Album(sourceId: $sourceId, id: $id, name: $name, artistId: $artistId, albumArtist: $albumArtist, created: $created, coverArt: $coverArt, year: $year, starred: $starred, genre: $genre, songCount: $songCount, isDeleted: $isDeleted, frequentRank: $frequentRank, recentRank: $recentRank)';
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
      ..add(DiagnosticsProperty('type', 'Album'))
      ..add(DiagnosticsProperty('sourceId', sourceId))
      ..add(DiagnosticsProperty('id', id))
      ..add(DiagnosticsProperty('name', name))
      ..add(DiagnosticsProperty('artistId', artistId))
      ..add(DiagnosticsProperty('albumArtist', albumArtist))
      ..add(DiagnosticsProperty('created', created))
      ..add(DiagnosticsProperty('coverArt', coverArt))
      ..add(DiagnosticsProperty('year', year))
      ..add(DiagnosticsProperty('starred', starred))
      ..add(DiagnosticsProperty('genre', genre))
      ..add(DiagnosticsProperty('songCount', songCount))
      ..add(DiagnosticsProperty('isDeleted', isDeleted))
      ..add(DiagnosticsProperty('frequentRank', frequentRank))
      ..add(DiagnosticsProperty('recentRank', recentRank));
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AlbumImpl &&
            (identical(other.sourceId, sourceId) ||
                other.sourceId == sourceId) &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.artistId, artistId) ||
                other.artistId == artistId) &&
            (identical(other.albumArtist, albumArtist) ||
                other.albumArtist == albumArtist) &&
            (identical(other.created, created) || other.created == created) &&
            (identical(other.coverArt, coverArt) ||
                other.coverArt == coverArt) &&
            (identical(other.year, year) || other.year == year) &&
            (identical(other.starred, starred) || other.starred == starred) &&
            (identical(other.genre, genre) || other.genre == genre) &&
            (identical(other.songCount, songCount) ||
                other.songCount == songCount) &&
            (identical(other.isDeleted, isDeleted) ||
                other.isDeleted == isDeleted) &&
            (identical(other.frequentRank, frequentRank) ||
                other.frequentRank == frequentRank) &&
            (identical(other.recentRank, recentRank) ||
                other.recentRank == recentRank));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      sourceId,
      id,
      name,
      artistId,
      albumArtist,
      created,
      coverArt,
      year,
      starred,
      genre,
      songCount,
      isDeleted,
      frequentRank,
      recentRank);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$AlbumImplCopyWith<_$AlbumImpl> get copyWith =>
      __$$AlbumImplCopyWithImpl<_$AlbumImpl>(this, _$identity);
}

abstract class _Album implements Album {
  const factory _Album(
      {required final int sourceId,
      required final String id,
      required final String name,
      final String? artistId,
      final String? albumArtist,
      required final DateTime created,
      final String? coverArt,
      final int? year,
      final DateTime? starred,
      final String? genre,
      required final int songCount,
      final bool isDeleted,
      final int? frequentRank,
      final int? recentRank}) = _$AlbumImpl;

  @override
  int get sourceId;
  @override
  String get id;
  @override
  String get name;
  @override
  String? get artistId;
  @override
  String? get albumArtist;
  @override
  DateTime get created;
  @override
  String? get coverArt;
  @override
  int? get year;
  @override
  DateTime? get starred;
  @override // DateTime? synced,
  String? get genre;
  @override
  int get songCount;
  @override
  bool get isDeleted;
  @override // @Default(IListConst([])) IList<Song> songs,
  int? get frequentRank;
  @override
  int? get recentRank;
  @override
  @JsonKey(ignore: true)
  _$$AlbumImplCopyWith<_$AlbumImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$Playlist {
  int get sourceId => throw _privateConstructorUsedError;
  String get id => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  String? get comment => throw _privateConstructorUsedError;
  String? get coverArt => throw _privateConstructorUsedError;
  int get songCount => throw _privateConstructorUsedError;
  DateTime get created => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $PlaylistCopyWith<Playlist> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $PlaylistCopyWith<$Res> {
  factory $PlaylistCopyWith(Playlist value, $Res Function(Playlist) then) =
      _$PlaylistCopyWithImpl<$Res, Playlist>;
  @useResult
  $Res call(
      {int sourceId,
      String id,
      String name,
      String? comment,
      String? coverArt,
      int songCount,
      DateTime created});
}

/// @nodoc
class _$PlaylistCopyWithImpl<$Res, $Val extends Playlist>
    implements $PlaylistCopyWith<$Res> {
  _$PlaylistCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
    Object? name = null,
    Object? comment = freezed,
    Object? coverArt = freezed,
    Object? songCount = null,
    Object? created = null,
  }) {
    return _then(_value.copyWith(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      comment: freezed == comment
          ? _value.comment
          : comment // ignore: cast_nullable_to_non_nullable
              as String?,
      coverArt: freezed == coverArt
          ? _value.coverArt
          : coverArt // ignore: cast_nullable_to_non_nullable
              as String?,
      songCount: null == songCount
          ? _value.songCount
          : songCount // ignore: cast_nullable_to_non_nullable
              as int,
      created: null == created
          ? _value.created
          : created // ignore: cast_nullable_to_non_nullable
              as DateTime,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$PlaylistImplCopyWith<$Res>
    implements $PlaylistCopyWith<$Res> {
  factory _$$PlaylistImplCopyWith(
          _$PlaylistImpl value, $Res Function(_$PlaylistImpl) then) =
      __$$PlaylistImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int sourceId,
      String id,
      String name,
      String? comment,
      String? coverArt,
      int songCount,
      DateTime created});
}

/// @nodoc
class __$$PlaylistImplCopyWithImpl<$Res>
    extends _$PlaylistCopyWithImpl<$Res, _$PlaylistImpl>
    implements _$$PlaylistImplCopyWith<$Res> {
  __$$PlaylistImplCopyWithImpl(
      _$PlaylistImpl _value, $Res Function(_$PlaylistImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
    Object? name = null,
    Object? comment = freezed,
    Object? coverArt = freezed,
    Object? songCount = null,
    Object? created = null,
  }) {
    return _then(_$PlaylistImpl(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      comment: freezed == comment
          ? _value.comment
          : comment // ignore: cast_nullable_to_non_nullable
              as String?,
      coverArt: freezed == coverArt
          ? _value.coverArt
          : coverArt // ignore: cast_nullable_to_non_nullable
              as String?,
      songCount: null == songCount
          ? _value.songCount
          : songCount // ignore: cast_nullable_to_non_nullable
              as int,
      created: null == created
          ? _value.created
          : created // ignore: cast_nullable_to_non_nullable
              as DateTime,
    ));
  }
}

/// @nodoc

class _$PlaylistImpl with DiagnosticableTreeMixin implements _Playlist {
  const _$PlaylistImpl(
      {required this.sourceId,
      required this.id,
      required this.name,
      this.comment,
      this.coverArt,
      required this.songCount,
      required this.created});

  @override
  final int sourceId;
  @override
  final String id;
  @override
  final String name;
  @override
  final String? comment;
  @override
  final String? coverArt;
  @override
  final int songCount;
  @override
  final DateTime created;

  @override
  String toString({DiagnosticLevel minLevel = DiagnosticLevel.info}) {
    return 'Playlist(sourceId: $sourceId, id: $id, name: $name, comment: $comment, coverArt: $coverArt, songCount: $songCount, created: $created)';
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
      ..add(DiagnosticsProperty('type', 'Playlist'))
      ..add(DiagnosticsProperty('sourceId', sourceId))
      ..add(DiagnosticsProperty('id', id))
      ..add(DiagnosticsProperty('name', name))
      ..add(DiagnosticsProperty('comment', comment))
      ..add(DiagnosticsProperty('coverArt', coverArt))
      ..add(DiagnosticsProperty('songCount', songCount))
      ..add(DiagnosticsProperty('created', created));
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$PlaylistImpl &&
            (identical(other.sourceId, sourceId) ||
                other.sourceId == sourceId) &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.comment, comment) || other.comment == comment) &&
            (identical(other.coverArt, coverArt) ||
                other.coverArt == coverArt) &&
            (identical(other.songCount, songCount) ||
                other.songCount == songCount) &&
            (identical(other.created, created) || other.created == created));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType, sourceId, id, name, comment, coverArt, songCount, created);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$PlaylistImplCopyWith<_$PlaylistImpl> get copyWith =>
      __$$PlaylistImplCopyWithImpl<_$PlaylistImpl>(this, _$identity);
}

abstract class _Playlist implements Playlist {
  const factory _Playlist(
      {required final int sourceId,
      required final String id,
      required final String name,
      final String? comment,
      final String? coverArt,
      required final int songCount,
      required final DateTime created}) = _$PlaylistImpl;

  @override
  int get sourceId;
  @override
  String get id;
  @override
  String get name;
  @override
  String? get comment;
  @override
  String? get coverArt;
  @override
  int get songCount;
  @override
  DateTime get created;
  @override
  @JsonKey(ignore: true)
  _$$PlaylistImplCopyWith<_$PlaylistImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$Song {
  int get sourceId => throw _privateConstructorUsedError;
  String get id => throw _privateConstructorUsedError;
  String? get albumId => throw _privateConstructorUsedError;
  String? get artistId => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String? get artist => throw _privateConstructorUsedError;
  String? get album => throw _privateConstructorUsedError;
  Duration? get duration => throw _privateConstructorUsedError;
  int? get track => throw _privateConstructorUsedError;
  int? get disc => throw _privateConstructorUsedError;
  DateTime? get starred => throw _privateConstructorUsedError;
  String? get genre => throw _privateConstructorUsedError;
  String? get downloadTaskId => throw _privateConstructorUsedError;
  String? get downloadFilePath => throw _privateConstructorUsedError;
  bool get isDeleted => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $SongCopyWith<Song> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SongCopyWith<$Res> {
  factory $SongCopyWith(Song value, $Res Function(Song) then) =
      _$SongCopyWithImpl<$Res, Song>;
  @useResult
  $Res call(
      {int sourceId,
      String id,
      String? albumId,
      String? artistId,
      String title,
      String? artist,
      String? album,
      Duration? duration,
      int? track,
      int? disc,
      DateTime? starred,
      String? genre,
      String? downloadTaskId,
      String? downloadFilePath,
      bool isDeleted});
}

/// @nodoc
class _$SongCopyWithImpl<$Res, $Val extends Song>
    implements $SongCopyWith<$Res> {
  _$SongCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
    Object? albumId = freezed,
    Object? artistId = freezed,
    Object? title = null,
    Object? artist = freezed,
    Object? album = freezed,
    Object? duration = freezed,
    Object? track = freezed,
    Object? disc = freezed,
    Object? starred = freezed,
    Object? genre = freezed,
    Object? downloadTaskId = freezed,
    Object? downloadFilePath = freezed,
    Object? isDeleted = null,
  }) {
    return _then(_value.copyWith(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      albumId: freezed == albumId
          ? _value.albumId
          : albumId // ignore: cast_nullable_to_non_nullable
              as String?,
      artistId: freezed == artistId
          ? _value.artistId
          : artistId // ignore: cast_nullable_to_non_nullable
              as String?,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      artist: freezed == artist
          ? _value.artist
          : artist // ignore: cast_nullable_to_non_nullable
              as String?,
      album: freezed == album
          ? _value.album
          : album // ignore: cast_nullable_to_non_nullable
              as String?,
      duration: freezed == duration
          ? _value.duration
          : duration // ignore: cast_nullable_to_non_nullable
              as Duration?,
      track: freezed == track
          ? _value.track
          : track // ignore: cast_nullable_to_non_nullable
              as int?,
      disc: freezed == disc
          ? _value.disc
          : disc // ignore: cast_nullable_to_non_nullable
              as int?,
      starred: freezed == starred
          ? _value.starred
          : starred // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      genre: freezed == genre
          ? _value.genre
          : genre // ignore: cast_nullable_to_non_nullable
              as String?,
      downloadTaskId: freezed == downloadTaskId
          ? _value.downloadTaskId
          : downloadTaskId // ignore: cast_nullable_to_non_nullable
              as String?,
      downloadFilePath: freezed == downloadFilePath
          ? _value.downloadFilePath
          : downloadFilePath // ignore: cast_nullable_to_non_nullable
              as String?,
      isDeleted: null == isDeleted
          ? _value.isDeleted
          : isDeleted // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SongImplCopyWith<$Res> implements $SongCopyWith<$Res> {
  factory _$$SongImplCopyWith(
          _$SongImpl value, $Res Function(_$SongImpl) then) =
      __$$SongImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int sourceId,
      String id,
      String? albumId,
      String? artistId,
      String title,
      String? artist,
      String? album,
      Duration? duration,
      int? track,
      int? disc,
      DateTime? starred,
      String? genre,
      String? downloadTaskId,
      String? downloadFilePath,
      bool isDeleted});
}

/// @nodoc
class __$$SongImplCopyWithImpl<$Res>
    extends _$SongCopyWithImpl<$Res, _$SongImpl>
    implements _$$SongImplCopyWith<$Res> {
  __$$SongImplCopyWithImpl(_$SongImpl _value, $Res Function(_$SongImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? sourceId = null,
    Object? id = null,
    Object? albumId = freezed,
    Object? artistId = freezed,
    Object? title = null,
    Object? artist = freezed,
    Object? album = freezed,
    Object? duration = freezed,
    Object? track = freezed,
    Object? disc = freezed,
    Object? starred = freezed,
    Object? genre = freezed,
    Object? downloadTaskId = freezed,
    Object? downloadFilePath = freezed,
    Object? isDeleted = null,
  }) {
    return _then(_$SongImpl(
      sourceId: null == sourceId
          ? _value.sourceId
          : sourceId // ignore: cast_nullable_to_non_nullable
              as int,
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      albumId: freezed == albumId
          ? _value.albumId
          : albumId // ignore: cast_nullable_to_non_nullable
              as String?,
      artistId: freezed == artistId
          ? _value.artistId
          : artistId // ignore: cast_nullable_to_non_nullable
              as String?,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      artist: freezed == artist
          ? _value.artist
          : artist // ignore: cast_nullable_to_non_nullable
              as String?,
      album: freezed == album
          ? _value.album
          : album // ignore: cast_nullable_to_non_nullable
              as String?,
      duration: freezed == duration
          ? _value.duration
          : duration // ignore: cast_nullable_to_non_nullable
              as Duration?,
      track: freezed == track
          ? _value.track
          : track // ignore: cast_nullable_to_non_nullable
              as int?,
      disc: freezed == disc
          ? _value.disc
          : disc // ignore: cast_nullable_to_non_nullable
              as int?,
      starred: freezed == starred
          ? _value.starred
          : starred // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      genre: freezed == genre
          ? _value.genre
          : genre // ignore: cast_nullable_to_non_nullable
              as String?,
      downloadTaskId: freezed == downloadTaskId
          ? _value.downloadTaskId
          : downloadTaskId // ignore: cast_nullable_to_non_nullable
              as String?,
      downloadFilePath: freezed == downloadFilePath
          ? _value.downloadFilePath
          : downloadFilePath // ignore: cast_nullable_to_non_nullable
              as String?,
      isDeleted: null == isDeleted
          ? _value.isDeleted
          : isDeleted // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$SongImpl with DiagnosticableTreeMixin implements _Song {
  const _$SongImpl(
      {required this.sourceId,
      required this.id,
      this.albumId,
      this.artistId,
      required this.title,
      this.artist,
      this.album,
      this.duration,
      this.track,
      this.disc,
      this.starred,
      this.genre,
      this.downloadTaskId,
      this.downloadFilePath,
      this.isDeleted = false});

  @override
  final int sourceId;
  @override
  final String id;
  @override
  final String? albumId;
  @override
  final String? artistId;
  @override
  final String title;
  @override
  final String? artist;
  @override
  final String? album;
  @override
  final Duration? duration;
  @override
  final int? track;
  @override
  final int? disc;
  @override
  final DateTime? starred;
  @override
  final String? genre;
  @override
  final String? downloadTaskId;
  @override
  final String? downloadFilePath;
  @override
  @JsonKey()
  final bool isDeleted;

  @override
  String toString({DiagnosticLevel minLevel = DiagnosticLevel.info}) {
    return 'Song(sourceId: $sourceId, id: $id, albumId: $albumId, artistId: $artistId, title: $title, artist: $artist, album: $album, duration: $duration, track: $track, disc: $disc, starred: $starred, genre: $genre, downloadTaskId: $downloadTaskId, downloadFilePath: $downloadFilePath, isDeleted: $isDeleted)';
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
      ..add(DiagnosticsProperty('type', 'Song'))
      ..add(DiagnosticsProperty('sourceId', sourceId))
      ..add(DiagnosticsProperty('id', id))
      ..add(DiagnosticsProperty('albumId', albumId))
      ..add(DiagnosticsProperty('artistId', artistId))
      ..add(DiagnosticsProperty('title', title))
      ..add(DiagnosticsProperty('artist', artist))
      ..add(DiagnosticsProperty('album', album))
      ..add(DiagnosticsProperty('duration', duration))
      ..add(DiagnosticsProperty('track', track))
      ..add(DiagnosticsProperty('disc', disc))
      ..add(DiagnosticsProperty('starred', starred))
      ..add(DiagnosticsProperty('genre', genre))
      ..add(DiagnosticsProperty('downloadTaskId', downloadTaskId))
      ..add(DiagnosticsProperty('downloadFilePath', downloadFilePath))
      ..add(DiagnosticsProperty('isDeleted', isDeleted));
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SongImpl &&
            (identical(other.sourceId, sourceId) ||
                other.sourceId == sourceId) &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.albumId, albumId) || other.albumId == albumId) &&
            (identical(other.artistId, artistId) ||
                other.artistId == artistId) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.artist, artist) || other.artist == artist) &&
            (identical(other.album, album) || other.album == album) &&
            (identical(other.duration, duration) ||
                other.duration == duration) &&
            (identical(other.track, track) || other.track == track) &&
            (identical(other.disc, disc) || other.disc == disc) &&
            (identical(other.starred, starred) || other.starred == starred) &&
            (identical(other.genre, genre) || other.genre == genre) &&
            (identical(other.downloadTaskId, downloadTaskId) ||
                other.downloadTaskId == downloadTaskId) &&
            (identical(other.downloadFilePath, downloadFilePath) ||
                other.downloadFilePath == downloadFilePath) &&
            (identical(other.isDeleted, isDeleted) ||
                other.isDeleted == isDeleted));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      sourceId,
      id,
      albumId,
      artistId,
      title,
      artist,
      album,
      duration,
      track,
      disc,
      starred,
      genre,
      downloadTaskId,
      downloadFilePath,
      isDeleted);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SongImplCopyWith<_$SongImpl> get copyWith =>
      __$$SongImplCopyWithImpl<_$SongImpl>(this, _$identity);
}

abstract class _Song implements Song {
  const factory _Song(
      {required final int sourceId,
      required final String id,
      final String? albumId,
      final String? artistId,
      required final String title,
      final String? artist,
      final String? album,
      final Duration? duration,
      final int? track,
      final int? disc,
      final DateTime? starred,
      final String? genre,
      final String? downloadTaskId,
      final String? downloadFilePath,
      final bool isDeleted}) = _$SongImpl;

  @override
  int get sourceId;
  @override
  String get id;
  @override
  String? get albumId;
  @override
  String? get artistId;
  @override
  String get title;
  @override
  String? get artist;
  @override
  String? get album;
  @override
  Duration? get duration;
  @override
  int? get track;
  @override
  int? get disc;
  @override
  DateTime? get starred;
  @override
  String? get genre;
  @override
  String? get downloadTaskId;
  @override
  String? get downloadFilePath;
  @override
  bool get isDeleted;
  @override
  @JsonKey(ignore: true)
  _$$SongImplCopyWith<_$SongImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$SearchResults {
  String? get query => throw _privateConstructorUsedError;
  IList<Song> get songs => throw _privateConstructorUsedError;
  IList<Album> get albums => throw _privateConstructorUsedError;
  IList<Artist> get artists => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $SearchResultsCopyWith<SearchResults> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SearchResultsCopyWith<$Res> {
  factory $SearchResultsCopyWith(
          SearchResults value, $Res Function(SearchResults) then) =
      _$SearchResultsCopyWithImpl<$Res, SearchResults>;
  @useResult
  $Res call(
      {String? query,
      IList<Song> songs,
      IList<Album> albums,
      IList<Artist> artists});
}

/// @nodoc
class _$SearchResultsCopyWithImpl<$Res, $Val extends SearchResults>
    implements $SearchResultsCopyWith<$Res> {
  _$SearchResultsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? query = freezed,
    Object? songs = null,
    Object? albums = null,
    Object? artists = null,
  }) {
    return _then(_value.copyWith(
      query: freezed == query
          ? _value.query
          : query // ignore: cast_nullable_to_non_nullable
              as String?,
      songs: null == songs
          ? _value.songs
          : songs // ignore: cast_nullable_to_non_nullable
              as IList<Song>,
      albums: null == albums
          ? _value.albums
          : albums // ignore: cast_nullable_to_non_nullable
              as IList<Album>,
      artists: null == artists
          ? _value.artists
          : artists // ignore: cast_nullable_to_non_nullable
              as IList<Artist>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SearchResultsImplCopyWith<$Res>
    implements $SearchResultsCopyWith<$Res> {
  factory _$$SearchResultsImplCopyWith(
          _$SearchResultsImpl value, $Res Function(_$SearchResultsImpl) then) =
      __$$SearchResultsImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? query,
      IList<Song> songs,
      IList<Album> albums,
      IList<Artist> artists});
}

/// @nodoc
class __$$SearchResultsImplCopyWithImpl<$Res>
    extends _$SearchResultsCopyWithImpl<$Res, _$SearchResultsImpl>
    implements _$$SearchResultsImplCopyWith<$Res> {
  __$$SearchResultsImplCopyWithImpl(
      _$SearchResultsImpl _value, $Res Function(_$SearchResultsImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? query = freezed,
    Object? songs = null,
    Object? albums = null,
    Object? artists = null,
  }) {
    return _then(_$SearchResultsImpl(
      query: freezed == query
          ? _value.query
          : query // ignore: cast_nullable_to_non_nullable
              as String?,
      songs: null == songs
          ? _value.songs
          : songs // ignore: cast_nullable_to_non_nullable
              as IList<Song>,
      albums: null == albums
          ? _value.albums
          : albums // ignore: cast_nullable_to_non_nullable
              as IList<Album>,
      artists: null == artists
          ? _value.artists
          : artists // ignore: cast_nullable_to_non_nullable
              as IList<Artist>,
    ));
  }
}

/// @nodoc

class _$SearchResultsImpl
    with DiagnosticableTreeMixin
    implements _SearchResults {
  const _$SearchResultsImpl(
      {this.query,
      this.songs = const IListConst([]),
      this.albums = const IListConst([]),
      this.artists = const IListConst([])});

  @override
  final String? query;
  @override
  @JsonKey()
  final IList<Song> songs;
  @override
  @JsonKey()
  final IList<Album> albums;
  @override
  @JsonKey()
  final IList<Artist> artists;

  @override
  String toString({DiagnosticLevel minLevel = DiagnosticLevel.info}) {
    return 'SearchResults(query: $query, songs: $songs, albums: $albums, artists: $artists)';
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
      ..add(DiagnosticsProperty('type', 'SearchResults'))
      ..add(DiagnosticsProperty('query', query))
      ..add(DiagnosticsProperty('songs', songs))
      ..add(DiagnosticsProperty('albums', albums))
      ..add(DiagnosticsProperty('artists', artists));
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SearchResultsImpl &&
            (identical(other.query, query) || other.query == query) &&
            const DeepCollectionEquality().equals(other.songs, songs) &&
            const DeepCollectionEquality().equals(other.albums, albums) &&
            const DeepCollectionEquality().equals(other.artists, artists));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      query,
      const DeepCollectionEquality().hash(songs),
      const DeepCollectionEquality().hash(albums),
      const DeepCollectionEquality().hash(artists));

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$SearchResultsImplCopyWith<_$SearchResultsImpl> get copyWith =>
      __$$SearchResultsImplCopyWithImpl<_$SearchResultsImpl>(this, _$identity);
}

abstract class _SearchResults implements SearchResults {
  const factory _SearchResults(
      {final String? query,
      final IList<Song> songs,
      final IList<Album> albums,
      final IList<Artist> artists}) = _$SearchResultsImpl;

  @override
  String? get query;
  @override
  IList<Song> get songs;
  @override
  IList<Album> get albums;
  @override
  IList<Artist> get artists;
  @override
  @JsonKey(ignore: true)
  _$$SearchResultsImplCopyWith<_$SearchResultsImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
