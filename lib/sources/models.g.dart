// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'models.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceArtist _$SourceArtistFromJson(Map<String, dynamic> json) => SourceArtist(
  id: json['id'] as String,
  name: json['name'] as String,
  starred: json['starred'] == null
      ? null
      : DateTime.parse(json['starred'] as String),
  smallImage: json['smallImage'] == null
      ? null
      : Uri.parse(json['smallImage'] as String),
  largeImage: json['largeImage'] == null
      ? null
      : Uri.parse(json['largeImage'] as String),
  $type: json['runtimeType'] as String?,
);

Map<String, dynamic> _$SourceArtistToJson(SourceArtist instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'starred': instance.starred?.toIso8601String(),
      'smallImage': instance.smallImage?.toString(),
      'largeImage': instance.largeImage?.toString(),
      'runtimeType': instance.$type,
    };

SourceAlbum _$SourceAlbumFromJson(Map<String, dynamic> json) => SourceAlbum(
  id: json['id'] as String,
  artistId: json['artistId'] as String?,
  name: json['name'] as String,
  albumArtist: json['albumArtist'] as String?,
  created: DateTime.parse(json['created'] as String),
  coverArt: json['coverArt'] as String?,
  year: (json['year'] as num?)?.toInt(),
  starred: json['starred'] == null
      ? null
      : DateTime.parse(json['starred'] as String),
  genre: json['genre'] as String?,
  frequentRank: (json['frequentRank'] as num?)?.toInt(),
  recentRank: (json['recentRank'] as num?)?.toInt(),
  $type: json['runtimeType'] as String?,
);

Map<String, dynamic> _$SourceAlbumToJson(SourceAlbum instance) =>
    <String, dynamic>{
      'id': instance.id,
      'artistId': instance.artistId,
      'name': instance.name,
      'albumArtist': instance.albumArtist,
      'created': instance.created.toIso8601String(),
      'coverArt': instance.coverArt,
      'year': instance.year,
      'starred': instance.starred?.toIso8601String(),
      'genre': instance.genre,
      'frequentRank': instance.frequentRank,
      'recentRank': instance.recentRank,
      'runtimeType': instance.$type,
    };

SourcePlaylist _$SourcePlaylistFromJson(Map<String, dynamic> json) =>
    SourcePlaylist(
      id: json['id'] as String,
      name: json['name'] as String,
      comment: json['comment'] as String?,
      created: DateTime.parse(json['created'] as String),
      changed: DateTime.parse(json['changed'] as String),
      coverArt: json['coverArt'] as String?,
      owner: json['owner'] as String?,
      public: json['public'] as bool?,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$SourcePlaylistToJson(SourcePlaylist instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'comment': instance.comment,
      'created': instance.created.toIso8601String(),
      'changed': instance.changed.toIso8601String(),
      'coverArt': instance.coverArt,
      'owner': instance.owner,
      'public': instance.public,
      'runtimeType': instance.$type,
    };

SourceSong _$SourceSongFromJson(Map<String, dynamic> json) => SourceSong(
  id: json['id'] as String,
  albumId: json['albumId'] as String?,
  artistId: json['artistId'] as String?,
  title: json['title'] as String,
  artist: json['artist'] as String?,
  album: json['album'] as String?,
  duration: json['duration'] == null
      ? null
      : Duration(microseconds: (json['duration'] as num).toInt()),
  track: (json['track'] as num?)?.toInt(),
  disc: (json['disc'] as num?)?.toInt(),
  starred: json['starred'] == null
      ? null
      : DateTime.parse(json['starred'] as String),
  genre: json['genre'] as String?,
  coverArt: json['coverArt'] as String?,
  $type: json['runtimeType'] as String?,
);

Map<String, dynamic> _$SourceSongToJson(SourceSong instance) =>
    <String, dynamic>{
      'id': instance.id,
      'albumId': instance.albumId,
      'artistId': instance.artistId,
      'title': instance.title,
      'artist': instance.artist,
      'album': instance.album,
      'duration': instance.duration?.inMicroseconds,
      'track': instance.track,
      'disc': instance.disc,
      'starred': instance.starred?.toIso8601String(),
      'genre': instance.genre,
      'coverArt': instance.coverArt,
      'runtimeType': instance.$type,
    };

_SourcePlaylistSong _$SourcePlaylistSongFromJson(Map<String, dynamic> json) =>
    _SourcePlaylistSong(
      playlistId: json['playlistId'] as String,
      songId: json['songId'] as String,
      position: (json['position'] as num).toInt(),
    );

Map<String, dynamic> _$SourcePlaylistSongToJson(_SourcePlaylistSong instance) =>
    <String, dynamic>{
      'playlistId': instance.playlistId,
      'songId': instance.songId,
      'position': instance.position,
    };
