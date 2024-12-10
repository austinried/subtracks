import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/support.dart';
import '../state/theme.dart';

class MediaItemGradient extends ConsumerWidget {
  const MediaItemGradient({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = ref.watch(mediaItemThemeProvider).valueOrNull;
    return BackgroundGradient(colors: colors);
  }
}

class AlbumArtGradient extends ConsumerWidget {
  final String id;

  const AlbumArtGradient({
    super.key,
    required this.id,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = ref.watch(albumArtThemeProvider(id)).valueOrNull;
    return BackgroundGradient(colors: colors);
  }
}

class PlaylistArtGradient extends ConsumerWidget {
  final String id;

  const PlaylistArtGradient({
    super.key,
    required this.id,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = ref.watch(playlistArtThemeProvider(id)).valueOrNull;
    return BackgroundGradient(colors: colors);
  }
}

class BackgroundGradient extends HookConsumerWidget {
  final ColorTheme? colors;

  const BackgroundGradient({
    super.key,
    this.colors,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mediaQuery = MediaQuery.of(context);
    final base = ref.watch(baseThemeProvider);

    return SizedBox(
      width: double.infinity,
      height: mediaQuery.size.height,
      child: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              colors?.gradientHigh ?? base.gradientHigh,
              colors?.gradientLow ?? base.gradientLow,
            ],
          ),
        ),
      ),
    );
  }
}
