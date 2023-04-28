import 'package:audio_service/audio_service.dart' show MediaItem;
import 'package:flutter/material.dart';
import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:palette_generator/palette_generator.dart';

part 'support.freezed.dart';
part 'support.g.dart';

@freezed
class UriCacheInfo with _$UriCacheInfo {
  const factory UriCacheInfo({
    required Uri uri,
    required String cacheKey,
    required CacheManager cacheManager,
  }) = _UriCacheInfo;
}

@freezed
class CacheInfo with _$CacheInfo {
  const factory CacheInfo({
    required String cacheKey,
    required CacheManager cacheManager,
  }) = _CacheInfo;
}

@freezed
class Palette with _$Palette {
  const Palette._();

  const factory Palette({
    PaletteColor? vibrantColor,
    PaletteColor? dominantColor,
    PaletteColor? mutedColor,
    PaletteColor? darkMutedColor,
    PaletteColor? darkVibrantColor,
    PaletteColor? lightMutedColor,
    PaletteColor? lightVibrantColor,
  }) = _Palette;

  factory Palette.fromPaletteGenerator(PaletteGenerator generator) {
    return Palette(
      vibrantColor: generator.vibrantColor,
      dominantColor: generator.dominantColor,
      mutedColor: generator.mutedColor,
      darkMutedColor: generator.darkMutedColor,
      darkVibrantColor: generator.darkVibrantColor,
      lightMutedColor: generator.lightMutedColor,
      lightVibrantColor: generator.lightVibrantColor,
    );
  }
}

@freezed
class ColorTheme with _$ColorTheme {
  const factory ColorTheme({
    required ThemeData theme,
    required Color gradientHigh,
    required Color gradientLow,
    required Color darkBackground,
    required Color darkerBackground,
    required Color onDarkerBackground,
  }) = _ColorTheme;
}

enum QueueContextType {
  song('song'),
  album('album'),
  playlist('playlist'),
  library('library'),
  genre('genre');

  const QueueContextType(this.value);
  final String value;

  @override
  String toString() => value;
}

enum QueueMode {
  user('user'),
  radio('radio');

  const QueueMode(this.value);
  final String value;

  @override
  String toString() => value;
}

enum RepeatMode {
  none('none'),
  all('all'),
  one('one');

  const RepeatMode(this.value);
  final String value;

  @override
  String toString() => value;
}

@freezed
class QueueItemState with _$QueueItemState {
  const factory QueueItemState({
    required String id,
    required QueueContextType contextType,
    String? contextId,
    String? contextTitle,
  }) = _QueueItemState;

  factory QueueItemState.fromJson(Map<String, dynamic> json) =>
      _$QueueItemStateFromJson(json);
}

@freezed
class MediaItemData with _$MediaItemData {
  const factory MediaItemData({
    required int sourceId,
    String? albumId,
    @MediaItemArtCacheConverter() MediaItemArtCache? artCache,
    required QueueContextType contextType,
    String? contextId,
  }) = _MediaItemData;

  factory MediaItemData.fromJson(Map<String, dynamic> json) =>
      _$MediaItemDataFromJson(json);
}

@freezed
class MediaItemArtCache with _$MediaItemArtCache {
  const factory MediaItemArtCache({
    required Uri fullArtUri,
    required String fullArtCacheKey,
    required Uri thumbnailArtUri,
    required String thumbnailArtCacheKey,
  }) = _MediaItemArtCache;

  factory MediaItemArtCache.fromJson(Map<String, dynamic> json) =>
      _$MediaItemArtCacheFromJson(json);
}

class MediaItemArtCacheConverter
    implements JsonConverter<MediaItemArtCache, Map<String, dynamic>> {
  const MediaItemArtCacheConverter();

  @override
  MediaItemArtCache fromJson(Map<String, dynamic> json) =>
      MediaItemArtCache.fromJson(json);

  @override
  Map<String, dynamic> toJson(MediaItemArtCache object) => object.toJson();
}

extension MediaItemPlus on MediaItem {
  MediaItemData get data => MediaItemData.fromJson(extras!['data']);
  set data(MediaItemData data) {
    extras!['data'] = data.toJson();
  }
}

@freezed
class ListDownloadStatus with _$ListDownloadStatus {
  const factory ListDownloadStatus({
    required int total,
    required int downloaded,
    required int downloading,
  }) = _ListDownloadStatus;
}

@freezed
class MultiChoiceOption with _$MultiChoiceOption {
  const factory MultiChoiceOption({
    required String title,
  }) = _MultiChoiceOption;

  factory MultiChoiceOption.int({
    required String title,
    required int option,
  }) = _MultiChoiceOptionInt;

  factory MultiChoiceOption.string({
    required String title,
    required String option,
  }) = _MultiChoiceOptionString;
}
