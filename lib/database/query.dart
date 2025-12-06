import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'query.freezed.dart';
part 'query.g.dart';

enum SortDirection {
  asc,
  desc,
}

enum AlbumsColumn {
  name,
  created,
  year,
  starred,
}

enum ArtistsColumn {
  name,
  starred,
  albumCount,
}

enum SongsColumn {
  title,
  starred,
  disc,
  track,
  album,
  artist,
  albumArtist,
}

enum PlaylistsColumn {
  name,
  created,
}

@freezed
abstract class SortingTerm with _$SortingTerm {
  const factory SortingTerm.albums({
    required SortDirection dir,
    required AlbumsColumn by,
  }) = AlbumsSortingTerm;

  static AlbumsSortingTerm albumsAsc(AlbumsColumn by) {
    return AlbumsSortingTerm(dir: SortDirection.asc, by: by);
  }

  static AlbumsSortingTerm albumsDesc(AlbumsColumn by) {
    return AlbumsSortingTerm(dir: SortDirection.desc, by: by);
  }

  const factory SortingTerm.artists({
    required SortDirection dir,
    required ArtistsColumn by,
  }) = ArtistsSortingTerm;

  static ArtistsSortingTerm artistsAsc(ArtistsColumn by) {
    return ArtistsSortingTerm(dir: SortDirection.asc, by: by);
  }

  static ArtistsSortingTerm artistsDesc(ArtistsColumn by) {
    return ArtistsSortingTerm(dir: SortDirection.desc, by: by);
  }

  const factory SortingTerm.songs({
    required SortDirection dir,
    required SongsColumn by,
  }) = SongsSortingTerm;

  static SongsSortingTerm songsAsc(SongsColumn by) {
    return SongsSortingTerm(dir: SortDirection.asc, by: by);
  }

  static SongsSortingTerm songsDesc(SongsColumn by) {
    return SongsSortingTerm(dir: SortDirection.desc, by: by);
  }

  const factory SortingTerm.playlists({
    required SortDirection dir,
    required PlaylistsColumn by,
  }) = PlaylistsSortingTerm;

  static PlaylistsSortingTerm playlistsAsc(PlaylistsColumn by) {
    return PlaylistsSortingTerm(dir: SortDirection.asc, by: by);
  }

  static PlaylistsSortingTerm playlistsDesc(PlaylistsColumn by) {
    return PlaylistsSortingTerm(dir: SortDirection.desc, by: by);
  }

  factory SortingTerm.fromJson(Map<String, Object?> json) =>
      _$SortingTermFromJson(json);
}

@freezed
abstract class AlbumsQuery with _$AlbumsQuery {
  const factory AlbumsQuery({
    required int sourceId,
    @Default(IListConst([])) IList<AlbumsFilter> filter,
    required IList<AlbumsSortingTerm> sort,
    int? limit,
    int? offset,
  }) = _AlbumsQuery;

  factory AlbumsQuery.fromJson(Map<String, Object?> json) =>
      _$AlbumsQueryFromJson(json);
}

@freezed
abstract class AlbumsFilter with _$AlbumsFilter {
  const factory AlbumsFilter.artistId(String artistId) = AlbumsFilterArtistId;
  const factory AlbumsFilter.yearEquals(int year) = AlbumsFilterYearEquals;

  factory AlbumsFilter.fromJson(Map<String, Object?> json) =>
      _$AlbumsFilterFromJson(json);
}

@freezed
abstract class ArtistsQuery with _$ArtistsQuery {
  const factory ArtistsQuery({
    required int sourceId,
    @Default(IListConst([])) IList<ArtistsFilter> filter,
    required IList<ArtistsSortingTerm> sort,
    int? limit,
    int? offset,
  }) = _ArtistsQuery;

  factory ArtistsQuery.fromJson(Map<String, Object?> json) =>
      _$ArtistsQueryFromJson(json);
}

@freezed
abstract class ArtistsFilter with _$ArtistsFilter {
  const factory ArtistsFilter.nameSearch(String name) = ArtistsFilterNameSearch;
  const factory ArtistsFilter.starred(bool starred) = ArtistsFilterStarred;

  factory ArtistsFilter.fromJson(Map<String, Object?> json) =>
      _$ArtistsFilterFromJson(json);
}

@freezed
abstract class SongsQuery with _$SongsQuery {
  const factory SongsQuery({
    required int sourceId,
    @Default(IListConst([])) IList<SongsFilter> filter,
    required IList<SongsSortingTerm> sort,
    int? limit,
    int? offset,
  }) = _SongsQuery;

  factory SongsQuery.fromJson(Map<String, Object?> json) =>
      _$SongsQueryFromJson(json);
}

@freezed
abstract class SongsFilter with _$SongsFilter {
  const factory SongsFilter.albumId(String albumId) = SongsFilterAlbumId;
  const factory SongsFilter.playlistId(String playlistId) =
      SongsFilterPlaylistId;

  factory SongsFilter.fromJson(Map<String, Object?> json) =>
      _$SongsFilterFromJson(json);
}

@freezed
abstract class PlaylistsQuery with _$PlaylistsQuery {
  const factory PlaylistsQuery({
    required int sourceId,
    @Default(IListConst([])) IList<PlaylistsFilter> filter,
    required IList<PlaylistsSortingTerm> sort,
    int? limit,
    int? offset,
  }) = _PlaylistsQuery;

  factory PlaylistsQuery.fromJson(Map<String, Object?> json) =>
      _$PlaylistsQueryFromJson(json);
}

@freezed
abstract class PlaylistsFilter with _$PlaylistsFilter {
  const factory PlaylistsFilter.nameSearch(String name) =
      PlaylistsFilterNameSearch;
  const factory PlaylistsFilter.public(bool public) = PlaylistsFilterPublic;

  factory PlaylistsFilter.fromJson(Map<String, Object?> json) =>
      _$PlaylistsFilterFromJson(json);
}
