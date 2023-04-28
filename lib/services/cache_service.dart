import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../cache/image_cache.dart';
import '../models/music.dart';
import '../models/support.dart';
import '../sources/music_source.dart';
import '../state/init.dart';
import '../state/settings.dart';

part 'cache_service.g.dart';

String _cacheKey(
  String type,
  int sourceId,
  String albumId, [
  bool thumbnail = false,
]) {
  return '$type${thumbnail ? 'Thumb' : ''}-$sourceId-$albumId';
}

String albumArtCacheKey(
  int sourceId,
  String albumId,
  bool thumbnail,
) {
  return _cacheKey('albumArt', sourceId, albumId, thumbnail);
}

String playlistArtCacheKey(
  int sourceId,
  String albumId,
  bool thumbnail,
) {
  return _cacheKey('playlistArt', sourceId, albumId, thumbnail);
}

String artistArtCacheKey(
  int sourceId,
  String albumId,
  bool thumbnail,
) {
  return _cacheKey('artistArt', sourceId, albumId, thumbnail);
}

@Riverpod(keepAlive: true)
CacheService cacheService(CacheServiceRef ref) {
  final imageCache = ref.watch(imageCacheProvider);
  final source = ref.watch(musicSourceProvider);
  final placeholderImageUri =
      ref.watch(placeholderImageUriProvider).requireValue;
  final placeholderThumbImageUri =
      ref.watch(placeholderThumbImageUriProvider).requireValue;

  return CacheService(
    imageCache: imageCache,
    source: source,
    placeholderImageUri: placeholderImageUri,
    placeholderThumbImageUri: placeholderThumbImageUri,
  );
}

class CacheService {
  final CacheManager imageCache;
  final MusicSource source;
  final Uri placeholderImageUri;
  final Uri placeholderThumbImageUri;

  CacheService({
    required this.imageCache,
    required this.source,
    required this.placeholderImageUri,
    required this.placeholderThumbImageUri,
  });

  UriCacheInfo albumArt(Album album, {bool thumbnail = true}) {
    final id = album.coverArt ?? album.id;
    return UriCacheInfo(
      uri: source.coverArtUri(id, thumbnail: thumbnail),
      cacheKey: albumArtCacheKey(source.id, album.id, thumbnail),
      cacheManager: imageCache,
    );
  }

  UriCacheInfo playlistArt(Playlist playlist, {bool thumbnail = true}) {
    final id = playlist.coverArt ?? playlist.id;
    return UriCacheInfo(
        uri: source.coverArtUri(id),
        cacheKey: playlistArtCacheKey(source.id, playlist.id, thumbnail),
        cacheManager: imageCache);
  }

  UriCacheInfo placeholder({bool thumbnail = true}) {
    final uri = thumbnail ? placeholderThumbImageUri : placeholderImageUri;
    return UriCacheInfo(
      uri: uri,
      cacheKey: uri.toString(),
      cacheManager: imageCache,
    );
  }

  Future<Uri?> artistArtUri(String artistId, {bool thumbnail = true}) {
    return source.artistArtUri(artistId, thumbnail: thumbnail);
  }

  CacheInfo artistArtCacheInfo(String artistId, {bool thumbnail = true}) {
    return CacheInfo(
      cacheKey: artistArtCacheKey(source.id, artistId, thumbnail),
      cacheManager: imageCache,
    );
  }
}
