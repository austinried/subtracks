// ignore_for_file: use_build_context_synchronously

import 'dart:async';

import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/music.dart';
import '../services/cache_service.dart';
import '../state/theme.dart';
import 'app_router.dart';
import 'hooks/use_download_actions.dart';
import 'images.dart';

enum MenuSize {
  small,
  medium,
}

Future<T?> showContextMenu<T>({
  required BuildContext context,
  required WidgetRef ref,
  required WidgetBuilder builder,
}) {
  return showModalBottomSheet<T>(
    backgroundColor: ref.read(baseThemeProvider).theme.colorScheme.surface,
    useRootNavigator: true,
    isScrollControlled: true,
    context: context,
    builder: builder,
  );
}

class BottomSheetMenu extends HookConsumerWidget {
  final Widget child;
  final MenuSize size;

  const BottomSheetMenu({
    super.key,
    required this.child,
    this.size = MenuSize.medium,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = ref.watch(baseThemeProvider);
    final height = size == MenuSize.medium ? 0.4 : 0.25;

    return Theme(
      data: theme.theme,
      child: DraggableScrollableSheet(
        expand: false,
        initialChildSize: height,
        maxChildSize: height,
        minChildSize: height - 0.05,
        snap: true,
        snapSizes: [height - 0.05, height],
        builder: (context, scrollController) {
          return PrimaryScrollController(
            controller: scrollController,
            child: SizedBox(
              child: Padding(
                padding: const EdgeInsets.only(top: 8),
                child: child,
              ),
            ),
          );
        },
      ),
    );
  }
}

class AlbumContextMenu extends HookConsumerWidget {
  final Album album;

  const AlbumContextMenu({
    super.key,
    required this.album,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final downloadActions = useAlbumDownloadActions(
      context: context,
      ref: ref,
      album: album,
    );

    return ListView(
      children: [
        _AlbumHeader(album: album),
        const SizedBox(height: 8),
        const _Star(),
        if (album.artistId != null) _ViewArtist(id: album.artistId!),
        for (var action in downloadActions)
          _DownloadAction(key: ValueKey(action.type), downloadAction: action),
      ],
    );
  }
}

class SongContextMenu extends HookConsumerWidget {
  final Song song;

  const SongContextMenu({
    super.key,
    required this.song,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ListView(
      children: [
        _SongHeader(song: song),
        const SizedBox(height: 8),
        const _Star(),
        if (song.artistId != null) _ViewArtist(id: song.artistId!),
        if (song.albumId != null) _ViewAlbum(id: song.albumId!),
        // const _DownloadAction(),
      ],
    );
  }
}

class ArtistContextMenu extends HookConsumerWidget {
  final Artist artist;

  const ArtistContextMenu({
    super.key,
    required this.artist,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ListView(
      children: [
        _ArtistHeader(artist: artist),
        const SizedBox(height: 8),
        const _Star(),
        // const _Download(),
      ],
    );
  }
}

class PlaylistContextMenu extends HookConsumerWidget {
  final Playlist playlist;

  const PlaylistContextMenu({
    super.key,
    required this.playlist,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final downloadActions = usePlaylistDownloadActions(
      context: context,
      ref: ref,
      playlist: playlist,
    );

    return ListView(
      children: [
        _PlaylistHeader(playlist: playlist),
        const SizedBox(height: 8),
        for (var action in downloadActions)
          _DownloadAction(key: ValueKey(action.type), downloadAction: action),
      ],
    );
  }
}

class _AlbumHeader extends HookConsumerWidget {
  final Album album;

  const _AlbumHeader({
    required this.album,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cache = ref.watch(cacheServiceProvider);

    return _Header(
      title: album.name,
      subtitle: album.albumArtist,
      image: CardClip(
        child: UriCacheInfoImage(
          cache: cache.albumArt(album, thumbnail: true),
        ),
      ),
    );
  }
}

class _SongHeader extends HookConsumerWidget {
  final Song song;

  const _SongHeader({
    required this.song,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return _Header(
      title: song.title,
      subtitle: song.artist,
      image: SongAlbumArt(song: song, square: false),
    );
  }
}

class _ArtistHeader extends HookConsumerWidget {
  final Artist artist;

  const _ArtistHeader({
    required this.artist,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    return _Header(
      title: artist.name,
      subtitle: l.resourcesAlbumCount(artist.albumCount),
      image: CircleClip(child: ArtistArtImage(artistId: artist.id)),
    );
  }
}

class _PlaylistHeader extends HookConsumerWidget {
  final Playlist playlist;

  const _PlaylistHeader({
    required this.playlist,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cache = ref.watch(cacheServiceProvider);
    final l = AppLocalizations.of(context);
    return _Header(
      title: playlist.name,
      subtitle: l.resourcesSongCount(playlist.songCount),
      image: CardClip(
        child: UriCacheInfoImage(
          cache: cache.playlistArt(playlist, thumbnail: true),
        ),
      ),
    );
  }
}

class _Header extends HookConsumerWidget {
  final String title;
  final String? subtitle;
  final Widget image;

  const _Header({
    required this.title,
    this.subtitle,
    required this.image,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(height: 80, width: 80, child: image),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: theme.textTheme.titleLarge),
                if (subtitle != null)
                  Text(subtitle!, style: theme.textTheme.titleSmall),
              ],
            ),
          )
        ],
      ),
    );
  }
}

class _Star extends HookConsumerWidget {
  const _Star();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    return _MenuItem(
      title: l.actionsStar,
      icon: const Icon(Icons.star_outline_rounded),
      onTap: () {},
    );
  }
}

class _DownloadAction extends HookConsumerWidget {
  final DownloadAction downloadAction;

  const _DownloadAction({
    super.key,
    required this.downloadAction,
  });

  String _actionText(AppLocalizations l) {
    switch (downloadAction.type) {
      case DownloadActionType.download:
        return l.actionsDownload;
      case DownloadActionType.cancel:
        return l.actionsDownloadCancel;
      case DownloadActionType.delete:
        return l.actionsDownloadDelete;
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    return _MenuItem(
      title: _actionText(l),
      icon: downloadAction.iconBuilder(context),
      onTap: downloadAction.action,
    );
  }
}

class _ViewArtist extends HookConsumerWidget {
  final String id;

  const _ViewArtist({
    required this.id,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    return _MenuItem(
      title: l.resourcesArtistActionsView,
      icon: const Icon(Icons.person_rounded),
      onTap: () async {
        final router = context.router;

        await router.pop();
        if (router.currentPath == '/now-playing') {
          await router.pop();
          await router.navigate(const LibraryRouter());
        }
        await router.navigate(ArtistRoute(id: id));
      },
    );
  }
}

class _ViewAlbum extends HookConsumerWidget {
  final String id;

  const _ViewAlbum({
    required this.id,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    return _MenuItem(
      title: l.resourcesAlbumActionsView,
      icon: const Icon(Icons.album_rounded),
      onTap: () async {
        final router = context.router;

        await router.pop();
        if (router.currentPath == '/now-playing') {
          await router.pop();
          await router.navigate(const LibraryRouter());
        }
        await router.navigate(AlbumSongsRoute(id: id));
      },
    );
  }
}

class _MenuItem extends StatelessWidget {
  final String title;
  final Widget icon;
  final FutureOr<void> Function()? onTap;

  const _MenuItem({
    required this.title,
    required this.icon,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      title: Text(title),
      leading: Padding(
        padding: const EdgeInsetsDirectional.only(start: 8),
        child: icon,
      ),
      onTap: onTap,
    );
  }
}
