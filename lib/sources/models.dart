import 'package:freezed_annotation/freezed_annotation.dart';

part 'models.freezed.dart';

@freezed
abstract class Artist with _$Artist {
  const factory Artist({
    required String id,
    required String name,
    DateTime? starred,
    Uri? smallImage,
    Uri? largeImage,
  }) = _Artist;
}

@freezed
abstract class Album with _$Album {
  const factory Album({
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
  }) = _Album;
}

@freezed
abstract class Playlist with _$Playlist {
  const factory Playlist({
    required String id,
    required String name,
    String? comment,
    required DateTime created,
    required DateTime changed,
    String? coverArt,
    String? owner,
    bool? public,
  }) = _Playlist;
}

@freezed
abstract class Song with _$Song {
  const factory Song({
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
  }) = _Song;
}

@freezed
abstract class PlaylistSong with _$PlaylistSong {
  const factory PlaylistSong({
    required String playlistId,
    required String songId,
    required int position,
  }) = _PlaylistSong;
}
