import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../util/logger.dart';
import '../state/source.dart';
import '../util/color_scheme.dart';
import 'theme.dart';

class CoverArtTheme extends HookConsumerWidget {
  const CoverArtTheme({
    super.key,
    required this.coverArt,
    required this.child,
  });

  final String? coverArt;
  final Widget child;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final source = ref.watch(sourceProvider);
    final sourceId = ref.watch(sourceIdProvider);

    final getColorScheme = useMemoized(
      () async {
        try {
          return await colorSchemefromImageProvider(
            brightness: Brightness.dark,
            provider: CachedNetworkImageProvider(
              coverArt != null
                  ? source.coverArtUri(coverArt!, thumbnail: true).toString()
                  : 'https://placehold.net/400x400.png',
              cacheKey: coverArt != null
                  ? '$sourceId$coverArt${true}'
                  : 'https://placehold.net/400x400.png',
            ),
          );
        } catch (error, stackTrace) {
          logger.w(
            'Could not create color scheme from image provider',
            error: error,
            stackTrace: stackTrace,
          );
          return null;
        }
      },
      [source, sourceId, coverArt],
    );

    final colorScheme = useFuture(getColorScheme).data;

    return Theme(
      data: colorScheme == null
          ? Theme.of(context)
          : subtracksTheme(colorScheme),
      child: child,
    );
  }
}
