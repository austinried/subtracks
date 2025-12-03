import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:material_symbols_icons/symbols.dart';

import '../state/source.dart';

class CoverArtImage extends HookConsumerWidget {
  const CoverArtImage({
    super.key,
    this.coverArt,
    this.thumbnail = true,
    this.fit,
    this.height,
    this.width,
  });

  final String? coverArt;
  final bool thumbnail;
  final BoxFit? fit;
  final double? height;
  final double? width;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final source = ref.watch(sourceProvider);
    final sourceId = ref.watch(sourceIdProvider);

    final imageUrl = coverArt != null
        ? source.coverArtUri(coverArt!, thumbnail: thumbnail).toString()
        : 'https://placehold.net/400x400.png';

    return CachedNetworkImage(
      height: height,
      width: width,
      imageUrl: imageUrl,
      cacheKey: '$sourceId$coverArt$thumbnail',
      placeholder: (context, url) => Icon(Symbols.cached_rounded),
      errorWidget: (context, url, error) => Icon(Icons.error),
      fit: BoxFit.cover,
      fadeOutDuration: Duration(milliseconds: 100),
      fadeInDuration: Duration(milliseconds: 200),
    );
  }
}
