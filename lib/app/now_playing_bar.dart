import 'package:audio_service/audio_service.dart';
import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../cache/image_cache.dart';
import '../models/support.dart';
import '../services/audio_service.dart';
import '../state/audio.dart';
import '../state/theme.dart';
import 'app_router.dart';
import 'images.dart';
import 'pages/now_playing_page.dart';

class NowPlayingBar extends HookConsumerWidget {
  const NowPlayingBar({
    super.key,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = ref.watch(mediaItemThemeProvider).valueOrNull;
    final noItem = ref.watch(mediaItemProvider).valueOrNull == null;

    final widget = GestureDetector(
      onTap: () {
        context.navigateTo(const NowPlayingRoute());
      },
      child: Material(
        elevation: 3,
        color: colors?.darkBackground,
        // surfaceTintColor: theme?.colorScheme.background,
        child: const Column(
          children: [
            SizedBox(
              height: 70,
              child: Row(
                mainAxisSize: MainAxisSize.max,
                children: [
                  Padding(
                    padding: EdgeInsets.all(10),
                    child: _ArtImage(),
                  ),
                  Expanded(
                    child: Padding(
                      padding: EdgeInsets.only(right: 4),
                      child: _TrackInfo(),
                    ),
                  ),
                  Padding(
                    padding: EdgeInsets.only(right: 16, top: 2),
                    child: PlayPauseButton(size: 48),
                  ),
                ],
              ),
            ),
            _ProgressBar(),
          ],
        ),
      ),
    );

    if (noItem) {
      return Container();
    }

    if (colors != null) {
      return Theme(data: colors.theme, child: widget);
    } else {
      return widget;
    }
  }
}

class _ArtImage extends HookConsumerWidget {
  const _ArtImage();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final imageCache = ref.watch(imageCacheProvider);
    final uri =
        ref.watch(mediaItemProvider.select((e) => e.valueOrNull?.artUri));
    final cacheKey = ref.watch(mediaItemDataProvider.select(
      (value) => value?.artCache?.thumbnailArtCacheKey,
    ));

    UriCacheInfo? cache;
    if (uri != null && cacheKey != null) {
      cache = UriCacheInfo(
        uri: uri,
        cacheKey: cacheKey,
        cacheManager: imageCache,
      );
    }

    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 150),
      child: CardClip(
        key: ValueKey(cacheKey ?? 'default'),
        child: cache == null
            ? const PlaceholderImage()
            : UriCacheInfoImage(
                cache: cache,
              ),
      ),
    );
  }
}

class _TrackInfo extends HookConsumerWidget {
  const _TrackInfo();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final item = ref.watch(mediaItemProvider);

    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        ...item.when(
          data: (data) => [
            // Text(
            //   data?.title ?? 'Nothing!!!',
            //   maxLines: 1,
            //   softWrap: false,
            //   overflow: TextOverflow.fade,
            //   style: Theme.of(context).textTheme.labelLarge,
            // ),
            ScrollableText(
              data?.title ?? 'Nothing!!!',
              style: Theme.of(context).textTheme.labelLarge,
            ),
            const SizedBox(height: 2),
            Text(
              data?.artist ?? 'Nothing!!!',
              maxLines: 1,
              softWrap: false,
              overflow: TextOverflow.fade,
              style: Theme.of(context).textTheme.labelMedium,
            ),
          ],
          error: (_, __) => const [Text('Error!')],
          loading: () => const [Text('loading.....')],
        ),
      ],
    );
  }
}

class PlayPauseButton extends HookConsumerWidget {
  final double size;

  const PlayPauseButton({
    super.key,
    required this.size,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final playing = ref.watch(playingProvider);
    final state = ref.watch(processingStateProvider);

    Widget icon;
    if (state == AudioProcessingState.loading ||
        state == AudioProcessingState.buffering) {
      icon = Stack(
        alignment: Alignment.center,
        children: [
          const Icon(Icons.circle),
          SizedBox(
            height: size / 3,
            width: size / 3,
            child: CircularProgressIndicator(
              strokeWidth: size / 16,
              color: Theme.of(context).colorScheme.surface,
            ),
          ),
        ],
      );
    } else if (playing) {
      icon = const Icon(Icons.pause_circle_rounded);
    } else {
      icon = const Icon(Icons.play_circle_rounded);
    }

    return IconButton(
      iconSize: size,
      padding: EdgeInsets.zero,
      onPressed: () {
        if (playing) {
          ref.read(audioControlProvider).pause();
        } else {
          ref.read(audioControlProvider).play();
        }
      },
      icon: icon,
      color: Theme.of(context).colorScheme.surface,
    );
  }
}

class _ProgressBar extends HookConsumerWidget {
  const _ProgressBar();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = ref.watch(mediaItemThemeProvider).valueOrNull;
    final position = ref.watch(positionProvider);
    final duration = ref.watch(durationProvider);

    return Container(
      height: 4,
      color: colors?.darkerBackground,
      child: Row(
        children: [
          Flexible(
            flex: position,
            child: Container(color: colors?.onDarkerBackground),
          ),
          Flexible(flex: duration - position, child: Container()),
        ],
      ),
    );
  }
}
