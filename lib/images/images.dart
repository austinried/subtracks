import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:material_symbols_icons/symbols.dart';
import 'package:octo_image/octo_image.dart';

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

    final imageProviderKeys = [source, coverArt, thumbnail];
    final buildImageProvider = useCallback(
      () => CachedNetworkImageProvider(
        coverArt != null
            ? source.coverArtUri(coverArt!, thumbnail: thumbnail).toString()
            : 'https://placehold.net/400x400.png',
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

    return OctoImage(
      image: imageProvider.value,
      placeholderBuilder: (context) => Icon(Symbols.album_rounded),
      errorBuilder: (context, error, trace) => Icon(Icons.error),
      fit: BoxFit.cover,
      fadeOutDuration: Duration(milliseconds: 100),
      fadeInDuration: Duration(milliseconds: 200),
    );
  }
}
