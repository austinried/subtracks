import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/foundation.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'music.freezed.dart';

abstract class SourceIdentifiable {
  int get sourceId;
  String get id;
}

abstract class SongList extends SourceIdentifiable {
  String get name;
  int get songCount;
}

@freezed
class SourceId with _$SourceId implements SourceIdentifiable {
  const factory SourceId({
    required int sourceId,
    required String id,
  }) = _SourceId;

  factory SourceId.from(SourceIdentifiable item) {
    return SourceId(sourceId: item.sourceId, id: item.id);
  }
}

@freezed
class SourceIdSet with _$SourceIdSet {
  const factory SourceIdSet({
    required int sourceId,
    required ISet<String> ids,
  }) = _SourceIdSet;
}

@freezed
class Artist with _$Artist {
  const factory Artist({
    required int sourceId,
    required String id,
    required String name,
    required int albumCount,
    DateTime? starred,
    // @Default(IListConst([])) IList<Album> albums,
  }) = _Artist;
}

@freezed
class Album with _$Album implements SongList {
  const factory Album({
    required int sourceId,
    required String id,
    required String name,
    String? artistId,
    String? albumArtist,
    required DateTime created,
    String? coverArt,
    int? year,
    DateTime? starred,
    // DateTime? synced,
    String? genre,
    required int songCount,
    @Default(false) bool isDeleted,
    // @Default(IListConst([])) IList<Song> songs,
    int? frequentRank,
    int? recentRank,
  }) = _Album;
}

@freezed
class Playlist with _$Playlist implements SongList {
  const factory Playlist({
    required int sourceId,
    required String id,
    required String name,
    String? comment,
    String? coverArt,
    required int songCount,
    required DateTime created,
    // DateTime? synced,
    // @Default(IListConst([])) IList<Song> songs,
  }) = _Playlist;
}

@freezed
class Song with _$Song implements SourceIdentifiable {
  const factory Song({
    required int sourceId,
    required String id,
    String? albumId,
    String? artistId,
    required String title,
    String? artist,
    String? album,
    Duration? duration,
    int? track,
    int? disc,
    DateTime? starred,
    String? genre,
    String? downloadTaskId,
    String? downloadFilePath,
    @Default(false) bool isDeleted,
  }) = _Song;
}

@freezed
class SearchResults with _$SearchResults {
  const factory SearchResults({
    String? query,
    @Default(IListConst([])) IList<Song> songs,
    @Default(IListConst([])) IList<Album> albums,
    @Default(IListConst([])) IList<Artist> artists,
  }) = _SearchResults;
}
