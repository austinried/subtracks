import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../l10n/generated/app_localizations.dart';
import '../state/database.dart';
import '../state/source.dart';
import '../ui/cover_art_theme.dart';
import '../ui/images.dart';

class AlbumScreen extends HookConsumerWidget {
  const AlbumScreen({
    super.key,
    required this.id,
  });

  final String id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);

    final db = ref.watch(databaseProvider);
    final sourceId = ref.watch(sourceIdProvider);

    final getAlbum = useMemoized(
      () => db.libraryDao.getAlbum(sourceId, id).getSingle(),
    );
    final album = useFuture(getAlbum).data;

    if (album == null) {
      return Container();
    }

    return CoverArtTheme(
      coverArt: album.coverArt,
      child: Scaffold(
        body: Center(
          child: CustomScrollView(
            slivers: [
              SliverToBoxAdapter(
                child: SafeArea(
                  child: Padding(
                    padding: EdgeInsetsGeometry.symmetric(horizontal: 16),
                    child: _Header(
                      title: album.name,
                      subtitle: album.albumArtist,
                      coverArt: album.coverArt,
                      playText: l.resourcesAlbumActionsPlay,
                      onPlay: () {},
                      onMore: () {},
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _Header extends HookConsumerWidget {
  const _Header({
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
    // final inheritedStyle = DefaultTextStyle.of(context).style;
    final theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        const SizedBox(height: 16),
        CoverArtImage(
          height: 300,
          thumbnail: false,
          coverArt: coverArt,
          fit: BoxFit.contain,
        ),
        const SizedBox(height: 20),
        Column(
          children: [
            Text(
              title,
              style: theme.textTheme.headlineMedium,
              textAlign: TextAlign.center,
            ),
            Text(
              subtitle ?? '',
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
      ],
    );
  }
}
