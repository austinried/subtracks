import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:material_symbols_icons/symbols.dart';

import '../app/state/source.dart';

class CoverArtImage extends HookConsumerWidget {
  const CoverArtImage({
    super.key,
    this.coverArt,
    this.thumbnail = false,
  });

  final String? coverArt;
  final bool thumbnail;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final source = ref.watch(sourceProvider);
    final sourceId = ref.watch(sourceIdProvider);

    final imageUrl = coverArt != null
        ? source.coverArtUri(coverArt!, thumbnail: thumbnail).toString()
        : 'https://placehold.net/400x400.png';

    return BaseImage(
      imageUrl: imageUrl,
      // can't use the URL because of token auth, which is a cache-buster
      cacheKey: '$sourceId$coverArt$thumbnail',
    );
  }
}

class BaseImage extends HookConsumerWidget {
  const BaseImage({
    super.key,
    required this.imageUrl,
    this.cacheKey,
    this.fit = BoxFit.cover,
  });

  final String imageUrl;
  final String? cacheKey;
  final BoxFit fit;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return CachedNetworkImage(
      imageUrl: imageUrl,
      cacheKey: cacheKey,
      placeholder: (context, url) => Icon(Symbols.cached_rounded),
      errorWidget: (context, url, error) => Icon(Icons.error),
      fit: BoxFit.cover,
      fadeOutDuration: Duration(milliseconds: 100),
      fadeInDuration: Duration(milliseconds: 200),
    );
  }
}
