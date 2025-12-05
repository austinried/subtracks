// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'query.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlbumsSortingTerm _$AlbumsSortingTermFromJson(Map<String, dynamic> json) =>
    AlbumsSortingTerm(
      dir: $enumDecode(_$SortDirectionEnumMap, json['dir']),
      by: $enumDecode(_$AlbumsColumnEnumMap, json['by']),
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$AlbumsSortingTermToJson(AlbumsSortingTerm instance) =>
    <String, dynamic>{
      'dir': _$SortDirectionEnumMap[instance.dir]!,
      'by': _$AlbumsColumnEnumMap[instance.by]!,
      'runtimeType': instance.$type,
    };

const _$SortDirectionEnumMap = {
  SortDirection.asc: 'asc',
  SortDirection.desc: 'desc',
};

const _$AlbumsColumnEnumMap = {
  AlbumsColumn.name: 'name',
  AlbumsColumn.created: 'created',
  AlbumsColumn.year: 'year',
  AlbumsColumn.starred: 'starred',
};

ArtistsSortingTerm _$ArtistsSortingTermFromJson(Map<String, dynamic> json) =>
    ArtistsSortingTerm(
      dir: $enumDecode(_$SortDirectionEnumMap, json['dir']),
      by: $enumDecode(_$ArtistsColumnEnumMap, json['by']),
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$ArtistsSortingTermToJson(ArtistsSortingTerm instance) =>
    <String, dynamic>{
      'dir': _$SortDirectionEnumMap[instance.dir]!,
      'by': _$ArtistsColumnEnumMap[instance.by]!,
      'runtimeType': instance.$type,
    };

const _$ArtistsColumnEnumMap = {
  ArtistsColumn.name: 'name',
  ArtistsColumn.starred: 'starred',
  ArtistsColumn.albumCount: 'albumCount',
};

SongsSortingTerm _$SongsSortingTermFromJson(Map<String, dynamic> json) =>
    SongsSortingTerm(
      dir: $enumDecode(_$SortDirectionEnumMap, json['dir']),
      by: $enumDecode(_$SongsColumnEnumMap, json['by']),
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$SongsSortingTermToJson(SongsSortingTerm instance) =>
    <String, dynamic>{
      'dir': _$SortDirectionEnumMap[instance.dir]!,
      'by': _$SongsColumnEnumMap[instance.by]!,
      'runtimeType': instance.$type,
    };

const _$SongsColumnEnumMap = {
  SongsColumn.title: 'title',
  SongsColumn.starred: 'starred',
  SongsColumn.disc: 'disc',
  SongsColumn.track: 'track',
};

_AlbumsQuery _$AlbumsQueryFromJson(Map<String, dynamic> json) => _AlbumsQuery(
  sourceId: (json['sourceId'] as num).toInt(),
  filter: json['filter'] == null
      ? const IListConst([])
      : IList<AlbumsFilter>.fromJson(
          json['filter'],
          (value) => AlbumsFilter.fromJson(value as Map<String, dynamic>),
        ),
  sort: IList<AlbumsSortingTerm>.fromJson(
    json['sort'],
    (value) => AlbumsSortingTerm.fromJson(value as Map<String, dynamic>),
  ),
  limit: (json['limit'] as num?)?.toInt(),
  offset: (json['offset'] as num?)?.toInt(),
);

Map<String, dynamic> _$AlbumsQueryToJson(_AlbumsQuery instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'filter': instance.filter.toJson((value) => value),
      'sort': instance.sort.toJson((value) => value),
      'limit': instance.limit,
      'offset': instance.offset,
    };

AlbumsFilterArtistId _$AlbumsFilterArtistIdFromJson(
  Map<String, dynamic> json,
) => AlbumsFilterArtistId(
  json['artistId'] as String,
  $type: json['runtimeType'] as String?,
);

Map<String, dynamic> _$AlbumsFilterArtistIdToJson(
  AlbumsFilterArtistId instance,
) => <String, dynamic>{
  'artistId': instance.artistId,
  'runtimeType': instance.$type,
};

AlbumsFilterYearEquals _$AlbumsFilterYearEqualsFromJson(
  Map<String, dynamic> json,
) => AlbumsFilterYearEquals(
  (json['year'] as num).toInt(),
  $type: json['runtimeType'] as String?,
);

Map<String, dynamic> _$AlbumsFilterYearEqualsToJson(
  AlbumsFilterYearEquals instance,
) => <String, dynamic>{'year': instance.year, 'runtimeType': instance.$type};

_ArtistsQuery _$ArtistsQueryFromJson(Map<String, dynamic> json) =>
    _ArtistsQuery(
      sourceId: (json['sourceId'] as num).toInt(),
      filter: json['filter'] == null
          ? const IListConst([])
          : IList<ArtistsFilter>.fromJson(
              json['filter'],
              (value) => ArtistsFilter.fromJson(value as Map<String, dynamic>),
            ),
      sort: IList<ArtistsSortingTerm>.fromJson(
        json['sort'],
        (value) => ArtistsSortingTerm.fromJson(value as Map<String, dynamic>),
      ),
      limit: (json['limit'] as num?)?.toInt(),
      offset: (json['offset'] as num?)?.toInt(),
    );

Map<String, dynamic> _$ArtistsQueryToJson(_ArtistsQuery instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'filter': instance.filter.toJson((value) => value),
      'sort': instance.sort.toJson((value) => value),
      'limit': instance.limit,
      'offset': instance.offset,
    };

ArtistsFilterNameSearch _$ArtistsFilterNameSearchFromJson(
  Map<String, dynamic> json,
) => ArtistsFilterNameSearch(
  json['name'] as String,
  $type: json['runtimeType'] as String?,
);

Map<String, dynamic> _$ArtistsFilterNameSearchToJson(
  ArtistsFilterNameSearch instance,
) => <String, dynamic>{'name': instance.name, 'runtimeType': instance.$type};

ArtistsFilterStarred _$ArtistsFilterStarredFromJson(
  Map<String, dynamic> json,
) => ArtistsFilterStarred(
  json['starred'] as bool,
  $type: json['runtimeType'] as String?,
);

Map<String, dynamic> _$ArtistsFilterStarredToJson(
  ArtistsFilterStarred instance,
) => <String, dynamic>{
  'starred': instance.starred,
  'runtimeType': instance.$type,
};

_SongsQuery _$SongsQueryFromJson(Map<String, dynamic> json) => _SongsQuery(
  sourceId: (json['sourceId'] as num).toInt(),
  filter: json['filter'] == null
      ? const IListConst([])
      : IList<SongsFilter>.fromJson(
          json['filter'],
          (value) => SongsFilter.fromJson(value as Map<String, dynamic>),
        ),
  sort: IList<SongsSortingTerm>.fromJson(
    json['sort'],
    (value) => SongsSortingTerm.fromJson(value as Map<String, dynamic>),
  ),
  limit: (json['limit'] as num?)?.toInt(),
  offset: (json['offset'] as num?)?.toInt(),
);

Map<String, dynamic> _$SongsQueryToJson(_SongsQuery instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'filter': instance.filter.toJson((value) => value),
      'sort': instance.sort.toJson((value) => value),
      'limit': instance.limit,
      'offset': instance.offset,
    };

SongsFilterAlbumId _$SongsFilterAlbumIdFromJson(Map<String, dynamic> json) =>
    SongsFilterAlbumId(
      json['albumId'] as String,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$SongsFilterAlbumIdToJson(SongsFilterAlbumId instance) =>
    <String, dynamic>{
      'albumId': instance.albumId,
      'runtimeType': instance.$type,
    };

SongsFilterPlaylistId _$SongsFilterPlaylistIdFromJson(
  Map<String, dynamic> json,
) => SongsFilterPlaylistId(
  json['playlistId'] as String,
  $type: json['runtimeType'] as String?,
);

Map<String, dynamic> _$SongsFilterPlaylistIdToJson(
  SongsFilterPlaylistId instance,
) => <String, dynamic>{
  'playlistId': instance.playlistId,
  'runtimeType': instance.$type,
};
