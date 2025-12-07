import 'dart:async';

import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../images.dart';

class SongsListHeader extends HookConsumerWidget {
  const SongsListHeader({
    super.key,
    required this.title,
    this.subtitle,
    this.coverArt,
    this.playText,
    this.onPlay,
    this.onMore,
    // required this.downloadActions,
  });

  final String title;
  final String? subtitle;
  final String? coverArt;
  final String? playText;
  final void Function()? onPlay;
  final FutureOr<void> Function()? onMore;
  // final List<DownloadAction> downloadActions;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    return SafeArea(
      minimum: EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          const SizedBox(height: 24),
          Container(
            height: 300,
            width: 300,
            decoration: BoxDecoration(
              boxShadow: [
                BoxShadow(
                  blurRadius: 20,
                  blurStyle: BlurStyle.normal,
                  color: Colors.black.withAlpha(100),
                  offset: Offset.zero,
                  spreadRadius: 2,
                ),
              ],
            ),
            child: CoverArtImage(
              thumbnail: false,
              coverArt: coverArt,
              fit: BoxFit.contain,
            ),
          ),
          const SizedBox(height: 20),
          Column(
            children: [
              Text(
                title,
                style: theme.textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
              if (subtitle != null)
                Text(
                  subtitle!,
                  style: theme.textTheme.headlineSmall,
                  textAlign: TextAlign.center,
                ),
            ],
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              IconButton(
                onPressed: () {},
                icon: const Icon(Icons.download_done_rounded),
              ),
              if (onPlay != null)
                FilledButton.icon(
                  onPressed: onPlay,
                  icon: const Icon(Icons.play_arrow_rounded),
                  label: Text(
                    playText ?? '',
                    // style: theme.textTheme.bodyLarge?.copyWith(
                    //   color: theme.colorScheme.onPrimary,
                    // ),
                  ),
                ),
              if (onMore != null)
                IconButton(
                  onPressed: onMore,
                  icon: const Icon(Icons.more_horiz),
                ),
            ],
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }
}
