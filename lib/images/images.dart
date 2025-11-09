import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:material_symbols_icons/symbols.dart';
import 'package:octo_image/octo_image.dart';

import '../app/state/settings.dart';
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

    final imageProviderKeys = [source, sourceId, coverArt, thumbnail];
    final buildImageProvider = useCallback(
      () => CachedNetworkImageProvider(
        coverArt != null
            ? source.coverArtUri(coverArt!, thumbnail: thumbnail).toString()
            : 'https://placehold.net/400x400.png',
        cacheKey: '$sourceId$coverArt$thumbnail',
      ),
      imageProviderKeys,
    );

    final imageProvider = useState(buildImageProvider());
    useEffect(
      () {
        imageProvider.value = buildImageProvider();
        return;
      },
      imageProviderKeys,
    );

    return BaseImage(
      image: imageProvider.value,
    );
  }
}

class CachedImage extends HookConsumerWidget {
  const CachedImage(
    this.uri, {
    super.key,
  });

  final Uri? uri;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final imageProviderKeys = [uri];
    final buildImageProvider = useCallback(
      () {
        final imageUrl = uri?.toString() ?? 'https://placehold.net/400x400.png';
        return CachedNetworkImageProvider(imageUrl, cacheKey: imageUrl);
      },
      imageProviderKeys,
    );

    final imageProvider = useState(buildImageProvider());
    useEffect(
      () {
        imageProvider.value = buildImageProvider();
        return;
      },
      imageProviderKeys,
    );

    return BaseImage(
      image: imageProvider.value,
    );
  }
}

class BaseImage extends HookConsumerWidget {
  const BaseImage({
    super.key,
    required this.image,
    this.fit = BoxFit.cover,
  });

  final ImageProvider image;
  final BoxFit fit;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return OctoImage(
      image: image,
      placeholderBuilder: (context) => Icon(Symbols.cached_rounded),
      errorBuilder: (context, error, trace) => Icon(Icons.error),
      fit: fit,
      fadeOutDuration: Duration(milliseconds: 100),
      fadeInDuration: Duration(milliseconds: 200),
    );
  }
}
