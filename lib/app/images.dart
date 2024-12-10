import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../models/music.dart';
import '../models/support.dart';
import '../services/cache_service.dart';
import '../state/music.dart';
import '../state/settings.dart';
import '../state/theme.dart';

part 'images.g.dart';

@riverpod
CacheInfo _artistArtCacheInfo(
  _ArtistArtCacheInfoRef ref, {
  required String artistId,
  bool thumbnail = true,
}) {
  final cache = ref.watch(cacheServiceProvider);
  return cache.artistArtCacheInfo(artistId, thumbnail: thumbnail);
}

@riverpod
FutureOr<String?> _artistArtCachedUrl(
  _ArtistArtCachedUrlRef ref, {
  required String artistId,
  bool thumbnail = true,
}) async {
  final cache = ref.watch(_artistArtCacheInfoProvider(
    artistId: artistId,
    thumbnail: thumbnail,
  ));
  final file = await cache.cacheManager.getFileFromCache(cache.cacheKey);
  return file?.originalUrl;
}

@riverpod
FutureOr<UriCacheInfo> _artistArtUriCacheInfo(
  _ArtistArtUriCacheInfoRef ref, {
  required String artistId,
  bool thumbnail = true,
}) async {
  final cache = ref.watch(cacheServiceProvider);
  final info = ref.watch(_artistArtCacheInfoProvider(
    artistId: artistId,
    thumbnail: thumbnail,
  ));
  final cachedUrl = await ref.watch(_artistArtCachedUrlProvider(
    artistId: artistId,
    thumbnail: thumbnail,
  ).future);
  final offline = ref.watch(offlineModeProvider);

  // already cached, don't try to get the real url again
  if (cachedUrl != null) {
    return UriCacheInfo(
      uri: Uri.parse(cachedUrl),
      cacheKey: info.cacheKey,
      cacheManager: info.cacheManager,
    );
  }

  if (offline) {
    final file = await cache.imageCache.getFileFromCache(info.cacheKey);
    if (file != null) {
      return UriCacheInfo(
        uri: Uri.parse(file.originalUrl),
        cacheKey: info.cacheKey,
        cacheManager: info.cacheManager,
      );
    } else {
      return cache.placeholder(thumbnail: thumbnail);
    }
  }

  // assume the url is good or let this fail
  return UriCacheInfo(
    uri: (await cache.artistArtUri(artistId, thumbnail: thumbnail))!,
    cacheKey: info.cacheKey,
    cacheManager: info.cacheManager,
  );
}

class ArtistArtImage extends HookConsumerWidget {
  final String artistId;
  final bool thumbnail;
  final BoxFit fit;
  final PlaceholderStyle placeholderStyle;
  final double? height;
  final double? width;

  const ArtistArtImage({
    super.key,
    required this.artistId,
    this.thumbnail = true,
    this.fit = BoxFit.cover,
    this.placeholderStyle = PlaceholderStyle.color,
    this.height,
    this.width,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    final cache = ref.watch(_artistArtUriCacheInfoProvider(
      artistId: artistId,
      thumbnail: thumbnail,
    ));

    // TODO: figure out how to animate this without messing up with boxfit/ratio
    return cache.when(
      data: (data) => UriCacheInfoImage(
        cache: data,
        fit: fit,
        placeholderStyle: placeholderStyle,
        height: height,
        width: width,
      ),
      error: (_, __) => Container(
        color: Colors.red,
        height: height,
        width: width,
      ),
      loading: () => Container(
        color: theme.colorScheme.secondaryContainer,
        height: height,
        width: width,
      ),
    );
  }
}

class SongAlbumArt extends HookConsumerWidget {
  final Song song;
  final bool square;

  const SongAlbumArt({
    super.key,
    required this.song,
    this.square = true,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final album = ref.watch(albumProvider(song.albumId!)).valueOrNull;

    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 150),
      child: album != null ? AlbumArt(album: album) : const PlaceholderImage(),
    );
  }
}

class AlbumArt extends HookConsumerWidget {
  final Album album;
  final bool square;

  const AlbumArt({
    super.key,
    required this.album,
    this.square = true,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // generate the palette used in other views ahead of time
    ref.watch(albumArtPaletteProvider(album.id));
    final cache = ref.watch(cacheServiceProvider);

    Widget image = UriCacheInfoImage(cache: cache.albumArt(album));

    if (square) {
      image = AspectRatio(aspectRatio: 1.0, child: image);
    }

    return CardClip(child: image);
  }
}

class CircleClip extends StatelessWidget {
  final Widget child;

  const CircleClip({
    super.key,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return ClipOval(
      clipBehavior: Clip.antiAlias,
      child: AspectRatio(
        aspectRatio: 1.0,
        child: child,
      ),
    );
  }
}

class CardClip extends StatelessWidget {
  final Widget child;
  final bool square;

  const CardClip({
    super.key,
    required this.child,
    this.square = true,
  });

  @override
  Widget build(BuildContext context) {
    final cardShape = Theme.of(context).cardTheme.shape;

    return ClipRRect(
      borderRadius: cardShape is RoundedRectangleBorder
          ? cardShape.borderRadius
          : BorderRadius.zero,
      child: !square
          ? child
          : AspectRatio(
              aspectRatio: 1.0,
              child: child,
            ),
    );
  }
}

enum PlaceholderStyle {
  color,
  spinner,
}

class UriCacheInfoImage extends StatelessWidget {
  final UriCacheInfo cache;
  final BoxFit fit;
  final PlaceholderStyle placeholderStyle;
  final double? height;
  final double? width;

  const UriCacheInfoImage({
    super.key,
    required this.cache,
    this.fit = BoxFit.cover,
    this.placeholderStyle = PlaceholderStyle.color,
    this.height,
    this.width,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return CachedNetworkImage(
      imageUrl: cache.uri.toString(),
      cacheKey: cache.cacheKey,
      cacheManager: cache.cacheManager,
      fit: fit,
      height: height,
      width: width,
      fadeInDuration: const Duration(milliseconds: 300),
      fadeOutDuration: const Duration(milliseconds: 500),
      placeholder: (context, url) =>
          placeholderStyle == PlaceholderStyle.spinner
              ? Container()
              : Container(
                  color: theme.colorScheme.secondaryContainer,
                ),
      errorWidget: (context, url, error) => PlaceholderImage(
        fit: fit,
        height: height,
        width: width,
      ),
    );
  }
}

class PlaceholderImage extends HookConsumerWidget {
  final BoxFit fit;
  final double? height;
  final double? width;
  final bool thumbnail;

  const PlaceholderImage({
    super.key,
    this.fit = BoxFit.cover,
    this.height,
    this.width,
    this.thumbnail = true,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Image.asset(
      thumbnail ? 'assets/placeholder_thumb.png' : 'assets/placeholder.png',
      fit: fit,
      height: height,
      width: width,
    );
  }
}

class _ExpandedRatio extends StatelessWidget {
  final Widget child;
  final double aspectRatio;

  const _ExpandedRatio({
    required this.child,
    this.aspectRatio = 1.0,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: AspectRatio(
        aspectRatio: aspectRatio,
        child: child,
      ),
    );
  }
}

class MultiImage extends HookConsumerWidget {
  final Iterable<UriCacheInfo> cacheInfo;

  const MultiImage({
    super.key,
    required this.cacheInfo,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final images = cacheInfo.map((cache) => UriCacheInfoImage(cache: cache));

    final row1 = <Widget>[];
    final row2 = <Widget>[];

    if (images.length >= 4) {
      row1.addAll([
        _ExpandedRatio(child: images.elementAt(0)),
        _ExpandedRatio(child: images.elementAt(1)),
      ]);
      row2.addAll([
        _ExpandedRatio(child: images.elementAt(2)),
        _ExpandedRatio(child: images.elementAt(3)),
      ]);
    }
    if (images.length == 3) {
      row1.addAll([
        _ExpandedRatio(child: images.elementAt(0)),
        _ExpandedRatio(child: images.elementAt(1)),
      ]);
      row2.addAll([
        _ExpandedRatio(aspectRatio: 2.0, child: images.elementAt(2)),
      ]);
    }
    if (images.length == 2) {
      row1.add(_ExpandedRatio(aspectRatio: 2.0, child: images.elementAt(0)));
      row2.add(_ExpandedRatio(aspectRatio: 2.0, child: images.elementAt(1)));
    }
    if (images.length == 1) {
      row1.addAll([_ExpandedRatio(child: images.elementAt(0))]);
    }

    return Column(
      children: [
        Row(children: row1),
        Row(children: row2),
      ],
    );
  }
}
