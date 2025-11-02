// ignore_for_file: annotate_overrides

import 'package:freezed_annotation/freezed_annotation.dart';

part 'models.freezed.dart';

mixin Starred {
  DateTime? get starred;
}

mixin CoverArt {
  String? get coverArt;
}

@freezed
abstract class SourceItem with _$SourceItem {
  @With<Starred>()
  const factory SourceItem.artist({
    required String id,
    required String name,
    DateTime? starred,
    Uri? smallImage,
    Uri? largeImage,
  }) = SourceArtist;

  @With<Starred>()
  @With<CoverArt>()
  const factory SourceItem.album({
    required String id,
    String? artistId,
    required String name,
    String? albumArtist,
    required DateTime created,
    String? coverArt,
    int? year,
    DateTime? starred,
    String? genre,
    int? frequentRank,
    int? recentRank,
  }) = SourceAlbum;

  @With<CoverArt>()
  const factory SourceItem.playlist({
    required String id,
    required String name,
    String? comment,
    required DateTime created,
    required DateTime changed,
    String? coverArt,
    String? owner,
    bool? public,
  }) = SourcePlaylist;

  @With<Starred>()
  @With<CoverArt>()
  const factory SourceItem.song({
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
    String? coverArt,
  }) = SourceSong;
}

@freezed
abstract class SourcePlaylistSong with _$SourcePlaylistSong {
  const factory SourcePlaylistSong({
    required String playlistId,
    required String songId,
    required int position,
  }) = _SourcePlaylistSong;
}
