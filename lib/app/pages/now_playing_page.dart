import 'dart:math';

import 'package:audio_service/audio_service.dart';
import 'package:auto_size_text/auto_size_text.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:text_scroll/text_scroll.dart';

import '../../cache/image_cache.dart';
import '../../models/support.dart';
import '../../services/audio_service.dart';
import '../../state/audio.dart';
import '../../state/theme.dart';
import '../context_menus.dart';
import '../gradient.dart';
import '../images.dart';
import '../now_playing_bar.dart';

class NowPlayingPage extends HookConsumerWidget {
  const NowPlayingPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = ref.watch(mediaItemThemeProvider).valueOrNull;
    final itemData = ref.watch(mediaItemDataProvider);

    final theme = Theme.of(context);

    final scaffold = AnnotatedRegion<SystemUiOverlayStyle>(
      value: SystemUiOverlayStyle.light.copyWith(
        systemNavigationBarColor: colors?.gradientLow,
        statusBarColor: Colors.transparent,
      ),
      child: Scaffold(
        appBar: AppBar(
          title: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                itemData?.contextType.value ?? '',
                style: theme.textTheme.labelMedium,
                maxLines: 1,
                softWrap: false,
                overflow: TextOverflow.fade,
              ),
              // Text(
              //   itemData?.contextTitle ?? '',
              //   style: theme.textTheme.titleMedium,
              //   maxLines: 1,
              //   softWrap: false,
              //   overflow: TextOverflow.fade,
              // ),
            ],
          ),
        ),
        body: const Stack(
          children: [
            MediaItemGradient(),
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 8, vertical: 8),
              child: Column(
                children: [
                  Expanded(
                    child: Padding(
                      padding: EdgeInsets.symmetric(horizontal: 16),
                      child: _Art(),
                    ),
                  ),
                  SizedBox(height: 24),
                  Padding(
                    padding: EdgeInsets.symmetric(horizontal: 16),
                    child: _TrackInfo(),
                  ),
                  SizedBox(height: 8),
                  _Progress(),
                  Padding(
                    padding: EdgeInsets.symmetric(horizontal: 16),
                    child: _Controls(),
                  ),
                  SizedBox(height: 64),
                ],
              ),
            ),
          ],
        ),
      ),
    );

    if (colors != null) {
      return Theme(data: colors.theme, child: scaffold);
    } else {
      return scaffold;
    }
  }
}

class _Art extends HookConsumerWidget {
  const _Art();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final itemData = ref.watch(mediaItemDataProvider);
    final imageCache = ref.watch(imageCacheProvider);

    UriCacheInfo? cacheInfo;
    if (itemData?.artCache != null) {
      cacheInfo = UriCacheInfo(
        uri: itemData!.artCache!.fullArtUri,
        cacheKey: itemData.artCache!.fullArtCacheKey,
        cacheManager: imageCache,
      );
    }

    return AnimatedSwitcher(
      duration: const Duration(milliseconds: 150),
      child: CardClip(
        key: ValueKey(cacheInfo?.cacheKey ?? 'default'),
        child: cacheInfo != null
            ? CardClip(
                square: false,
                child: UriCacheInfoImage(
                  // height: 300,
                  fit: BoxFit.contain,
                  placeholderStyle: PlaceholderStyle.spinner,
                  cache: cacheInfo,
                ),
              )
            : const PlaceholderImage(thumbnail: false),
      ),
    );
  }
}

class _TrackInfo extends HookConsumerWidget {
  const _TrackInfo();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final item = ref.watch(mediaItemProvider).valueOrNull;
    final theme = Theme.of(context);

    return Row(
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ScrollableText(
                item?.title ?? '',
                style: theme.textTheme.headlineSmall,
                speed: 50,
              ),
              Text(
                item?.artist ?? '',
                style: theme.textTheme.titleMedium!,
                maxLines: 1,
                softWrap: false,
                overflow: TextOverflow.fade,
              ),
            ],
          ),
        ),
        IconButton(
          icon: const Icon(
            Icons.star_outline_rounded,
            size: 36,
          ),
          onPressed: () {},
        ),
      ],
    );
  }
}

class ScrollableText extends StatelessWidget {
  final String text;
  final TextStyle? style;
  final double speed;

  const ScrollableText(
    this.text, {
    super.key,
    this.style,
    this.speed = 35,
  });

  @override
  Widget build(BuildContext context) {
    final defaultStyle = DefaultTextStyle.of(context);

    return AutoSizeText(
      text,
      presetFontSizes: style != null && style?.fontSize != null
          ? [style!.fontSize!]
          : [defaultStyle.style.fontSize ?? 12],
      style: style,
      maxLines: 1,
      // softWrap: false,
      overflowReplacement: TextScroll(
        '$text     ',
        style: style,
        delayBefore: const Duration(seconds: 3),
        pauseBetween: const Duration(seconds: 4),
        mode: TextScrollMode.endless,
        velocity: Velocity(pixelsPerSecond: Offset(speed, 0)),
      ),
    );
  }
}

class _Progress extends HookConsumerWidget {
  const _Progress();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    final colors = ref.watch(mediaItemThemeProvider).valueOrNull;
    final position = ref.watch(positionProvider);
    final duration = ref.watch(durationProvider);
    final audio = ref.watch(audioControlProvider);

    final changeValue = useState(position.toDouble());
    final changing = useState(false);

    return Column(
      children: [
        Slider(
          value: changing.value ? changeValue.value : position.toDouble(),
          min: 0,
          max: max(duration.toDouble(), position.toDouble()),
          thumbColor: colors?.theme.colorScheme.surface,
          activeColor: colors?.theme.colorScheme.surface,
          inactiveColor: colors?.theme.colorScheme.surface,
          onChanged: (value) {
            changeValue.value = value;
          },
          onChangeStart: (value) {
            changing.value = true;
          },
          onChangeEnd: (value) {
            changing.value = false;
            audio.seek(Duration(seconds: value.toInt()));
          },
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: DefaultTextStyle(
            style: theme.textTheme.titleMedium!,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(Duration(
                        seconds: changing.value
                            ? changeValue.value.toInt()
                            : position)
                    .toString()
                    .substring(2, 7)),
                Text(Duration(seconds: duration).toString().substring(2, 7)),
              ],
            ),
          ),
        )
      ],
    );
  }
}

class RepeatButton extends HookConsumerWidget {
  final double size;

  const RepeatButton({
    super.key,
    required this.size,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final audio = ref.watch(audioControlProvider);
    final repeat = ref.watch(repeatModeProvider);

    IconData icon;
    void Function() action;

    switch (repeat) {
      case AudioServiceRepeatMode.all:
      case AudioServiceRepeatMode.group:
        icon = Icons.repeat_on_rounded;
        action = () => audio.setRepeatMode(AudioServiceRepeatMode.one);
        break;
      case AudioServiceRepeatMode.one:
        icon = Icons.repeat_one_on_rounded;
        action = () => audio.setRepeatMode(AudioServiceRepeatMode.none);
        break;
      default:
        icon = Icons.repeat_rounded;
        action = () => audio.setRepeatMode(AudioServiceRepeatMode.all);
        break;
    }

    return IconButton(
      icon: Icon(icon),
      padding: EdgeInsets.zero,
      iconSize: 30,
      onPressed: action,
    );
  }
}

class ShuffleButton extends HookConsumerWidget {
  final double size;

  const ShuffleButton({
    super.key,
    required this.size,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final audio = ref.watch(audioControlProvider);
    final shuffle = ref.watch(shuffleModeProvider);
    final queueMode = ref.watch(queueModeProvider).valueOrNull;

    IconData icon;
    void Function() action;

    switch (shuffle) {
      case AudioServiceShuffleMode.all:
      case AudioServiceShuffleMode.group:
        icon = Icons.shuffle_on_rounded;
        action = () => audio.setShuffleMode(AudioServiceShuffleMode.none);
        break;
      default:
        icon = Icons.shuffle_rounded;
        action = () => audio.setShuffleMode(AudioServiceShuffleMode.all);
        break;
    }

    return IconButton(
      icon: Icon(queueMode == QueueMode.radio ? Icons.radio_rounded : icon),
      padding: EdgeInsets.zero,
      iconSize: 30,
      onPressed: queueMode == QueueMode.radio ? null : action,
    );
  }
}

class _Controls extends HookConsumerWidget {
  const _Controls();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final base = ref.watch(baseThemeProvider);
    final audio = ref.watch(audioControlProvider);

    return IconTheme(
      data: IconThemeData(color: base.theme.colorScheme.surface),
      child: Column(
        children: [
          SizedBox(
            height: 100,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const RepeatButton(size: 30),
                IconButton(
                  icon: const Icon(Icons.skip_previous_rounded),
                  padding: EdgeInsets.zero,
                  iconSize: 60,
                  onPressed: () => audio.skipToPrevious(),
                ),
                const PlayPauseButton(size: 90),
                IconButton(
                  icon: const Icon(Icons.skip_next_rounded),
                  padding: EdgeInsets.zero,
                  iconSize: 60,
                  onPressed: () => audio.skipToNext(),
                ),
                const ShuffleButton(size: 30),
              ],
            ),
          ),
          SizedBox(
            height: 40,
            child: Row(
              // crossAxisAlignment: CrossAxisAlignment.stretch,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                IconButton(
                  icon: const Icon(Icons.queue_music_rounded),
                  padding: EdgeInsets.zero,
                  iconSize: 30,
                  onPressed: () {},
                ),
                const _MoreButton(),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _MoreButton extends HookConsumerWidget {
  const _MoreButton();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final song = ref.watch(mediaItemSongProvider).valueOrNull;

    return IconButton(
      icon: const Icon(Icons.more_horiz),
      padding: EdgeInsets.zero,
      iconSize: 30,
      onPressed: song != null
          ? () {
              showContextMenu(
                context: context,
                ref: ref,
                builder: (context) => BottomSheetMenu(
                  child: SongContextMenu(song: song),
                ),
              );
            }
          : null,
    );
  }
}
