// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'support.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$QueueItemStateImpl _$$QueueItemStateImplFromJson(Map<String, dynamic> json) =>
    _$QueueItemStateImpl(
      id: json['id'] as String,
      contextType: $enumDecode(_$QueueContextTypeEnumMap, json['contextType']),
      contextId: json['contextId'] as String?,
      contextTitle: json['contextTitle'] as String?,
    );

Map<String, dynamic> _$$QueueItemStateImplToJson(
        _$QueueItemStateImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'contextType': _$QueueContextTypeEnumMap[instance.contextType]!,
      'contextId': instance.contextId,
      'contextTitle': instance.contextTitle,
    };

const _$QueueContextTypeEnumMap = {
  QueueContextType.song: 'song',
  QueueContextType.album: 'album',
  QueueContextType.playlist: 'playlist',
  QueueContextType.library: 'library',
  QueueContextType.genre: 'genre',
  QueueContextType.artist: 'artist',
};

_$MediaItemDataImpl _$$MediaItemDataImplFromJson(Map<String, dynamic> json) =>
    _$MediaItemDataImpl(
      sourceId: (json['sourceId'] as num).toInt(),
      albumId: json['albumId'] as String?,
      artCache:
          _$JsonConverterFromJson<Map<String, dynamic>, MediaItemArtCache>(
              json['artCache'], const MediaItemArtCacheConverter().fromJson),
      contextType: $enumDecode(_$QueueContextTypeEnumMap, json['contextType']),
      contextId: json['contextId'] as String?,
    );

Map<String, dynamic> _$$MediaItemDataImplToJson(_$MediaItemDataImpl instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'albumId': instance.albumId,
      'artCache':
          _$JsonConverterToJson<Map<String, dynamic>, MediaItemArtCache>(
              instance.artCache, const MediaItemArtCacheConverter().toJson),
      'contextType': _$QueueContextTypeEnumMap[instance.contextType]!,
      'contextId': instance.contextId,
    };

Value? _$JsonConverterFromJson<Json, Value>(
  Object? json,
  Value? Function(Json json) fromJson,
) =>
    json == null ? null : fromJson(json as Json);

Json? _$JsonConverterToJson<Json, Value>(
  Value? value,
  Json? Function(Value value) toJson,
) =>
    value == null ? null : toJson(value);

_$MediaItemArtCacheImpl _$$MediaItemArtCacheImplFromJson(
        Map<String, dynamic> json) =>
    _$MediaItemArtCacheImpl(
      fullArtUri: Uri.parse(json['fullArtUri'] as String),
      fullArtCacheKey: json['fullArtCacheKey'] as String,
      thumbnailArtUri: Uri.parse(json['thumbnailArtUri'] as String),
      thumbnailArtCacheKey: json['thumbnailArtCacheKey'] as String,
    );

Map<String, dynamic> _$$MediaItemArtCacheImplToJson(
        _$MediaItemArtCacheImpl instance) =>
    <String, dynamic>{
      'fullArtUri': instance.fullArtUri.toString(),
      'fullArtCacheKey': instance.fullArtCacheKey,
      'thumbnailArtUri': instance.thumbnailArtUri.toString(),
      'thumbnailArtCacheKey': instance.thumbnailArtCacheKey,
    };
