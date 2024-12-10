import 'package:collection/collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/music.dart';
import '../services/cache_service.dart';
import '../services/download_service.dart';
import '../state/audio.dart';
import '../state/music.dart';
import '../state/theme.dart';
import 'context_menus.dart';
import 'images.dart';
import 'pages/songs_page.dart';

enum CardStyle {
  imageOnly,
  withText,
}

enum AlbumSubtitle {
  artist,
  year,
}

class AlbumCard extends HookConsumerWidget {
  final Album album;
  final void Function()? onTap;
  final CardStyle style;
  final AlbumSubtitle subtitle;

  const AlbumCard({
    super.key,
    required this.album,
    this.onTap,
    this.style = CardStyle.withText,
    this.subtitle = AlbumSubtitle.artist,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // generate the palette used in other views ahead of time
    ref.watch(albumArtPaletteProvider(album.id));
    final cache = ref.watch(cacheServiceProvider);
    final info = cache.albumArt(album);

    final image = CardClip(child: UriCacheInfoImage(cache: info));
    Widget content;
    if (style == CardStyle.imageOnly) {
      content = image;
    } else {
      content = Column(
        children: [
          image,
          _AlbumCardText(album: album, subtitle: subtitle),
        ],
      );
    }

    return ImageCard(
      onTap: onTap,
      onLongPress: () {
        showContextMenu(
          context: context,
          ref: ref,
          builder: (context) => BottomSheetMenu(
            child: AlbumContextMenu(album: album),
          ),
        );
      },
      child: content,
    );
  }
}

class ImageCard extends StatelessWidget {
  final Widget child;
  final void Function()? onTap;
  final void Function()? onLongPress;

  const ImageCard({
    super.key,
    required this.child,
    this.onTap,
    this.onLongPress,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      surfaceTintColor: Colors.transparent,
      margin: const EdgeInsets.all(0),
      child: Stack(
        fit: StackFit.passthrough,
        alignment: Alignment.bottomCenter,
        children: [
          child,
          Positioned.fill(
            child: Material(
              color: Colors.transparent,
              child: InkWell(
                onTap: onTap,
                onLongPress: onLongPress,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _AlbumCardText extends StatelessWidget {
  final Album album;
  final AlbumSubtitle subtitle;

  const _AlbumCardText({
    required this.album,
    required this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Container(
      padding: const EdgeInsets.only(top: 4, bottom: 8),
      width: double.infinity,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: double.infinity,
            child: Text(
              album.name,
              maxLines: 1,
              softWrap: false,
              overflow: TextOverflow.fade,
              textAlign: TextAlign.start,
              style: textTheme.bodyMedium!.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          Text(
            (subtitle == AlbumSubtitle.artist
                    ? album.albumArtist
                    : album.year?.toString()) ??
                '',
            maxLines: 1,
            softWrap: false,
            overflow: TextOverflow.fade,
            textAlign: TextAlign.start,
            style: textTheme.bodySmall,
          ),
        ],
      ),
    );
  }
}

class AlbumListTile extends HookConsumerWidget {
  final Album album;
  final void Function()? onTap;

  const AlbumListTile({
    super.key,
    required this.album,
    this.onTap,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final artist = ref.watch(albumProvider(album.artistId!)).valueOrNull;

    return ListTile(
      leading: AlbumArt(album: album),
      title: Text(album.name),
      subtitle: Text(album.albumArtist ?? artist!.name),
      onTap: onTap,
      onLongPress: () {
        showContextMenu(
          context: context,
          ref: ref,
          builder: (context) => BottomSheetMenu(
            size: MenuSize.small,
            child: AlbumContextMenu(album: album),
          ),
        );
      },
    );
  }
}

class ArtistListTile extends HookConsumerWidget {
  final Artist artist;
  final void Function()? onTap;

  const ArtistListTile({
    super.key,
    required this.artist,
    this.onTap,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);

    return ListTile(
      leading: CircleClip(
        child: ArtistArtImage(artistId: artist.id),
      ),
      title: Text(artist.name),
      subtitle: Text(l.resourcesAlbumCount(
        artist.albumCount,
      )),
      onTap: onTap,
      onLongPress: () {
        showContextMenu(
          context: context,
          ref: ref,
          builder: (context) => BottomSheetMenu(
            size: MenuSize.small,
            child: ArtistContextMenu(artist: artist),
          ),
        );
      },
    );
  }
}

class PlaylistListTile extends HookConsumerWidget {
  final Playlist playlist;
  final void Function()? onTap;

  const PlaylistListTile({
    super.key,
    required this.playlist,
    this.onTap,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);

    // generate the palette used in other views ahead of time
    ref.watch(playlistArtPaletteProvider(playlist.id));
    final cache = ref.watch(cacheServiceProvider).playlistArt(playlist);

    return ListTile(
      leading: CardClip(
        child: UriCacheInfoImage(cache: cache),
      ),
      title: Text(playlist.name),
      subtitle: Text(l.resourcesSongCount(
        playlist.songCount,
      )),
      onTap: onTap,
      onLongPress: () {
        showContextMenu(
          context: context,
          ref: ref,
          builder: (context) => BottomSheetMenu(
            size: MenuSize.small,
            child: PlaylistContextMenu(playlist: playlist),
          ),
        );
      },
    );
  }
}

class SongListTile extends HookConsumerWidget {
  final Song song;
  final void Function()? onTap;
  final bool image;

  const SongListTile({
    super.key,
    required this.song,
    this.onTap,
    this.image = false,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Material(
      type: MaterialType.transparency,
      child: ListTile(
        title: _SongTitle(song: song),
        subtitle: _SongSubtitle(song: song),
        leading: image ? SongAlbumArt(song: song) : null,
        trailing: IconButton(
          icon: const Icon(
            Icons.star_outline_rounded,
            size: 36,
          ),
          onPressed: () {},
        ),
        onTap: onTap,
        onLongPress: () {
          showContextMenu(
            context: context,
            ref: ref,
            builder: (context) => BottomSheetMenu(
              child: SongContextMenu(song: song),
            ),
          );
        },
      ),
    );
  }
}

class _SongSubtitle extends HookConsumerWidget {
  final Song song;

  const _SongSubtitle({
    required this.song,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final downloadTaskId = ref.watch(songProvider(song.id).select(
      (value) => value.valueOrNull?.downloadTaskId,
    ));
    final downloadFilePath = ref.watch(songProvider(song.id).select(
      (value) => value.valueOrNull?.downloadFilePath,
    ));
    final download = ref.watch(downloadServiceProvider.select(
      (value) => value.downloads.firstWhereOrNull(
        (e) => e.taskId == downloadTaskId,
      ),
    ));

    final inheritedStyle = DefaultTextStyle.of(context).style;

    Widget? downloadIndicator;
    if (downloadFilePath != null) {
      downloadIndicator = const Padding(
        padding: EdgeInsetsDirectional.only(end: 3),
        child: Icon(
          Icons.download_done_rounded,
          size: 20,
        ),
      );
    } else if (downloadTaskId != null || download != null) {
      downloadIndicator = Padding(
        padding: const EdgeInsetsDirectional.only(start: 4, end: 9),
        child: SizedBox(
          height: 10,
          width: 10,
          child: CircularProgressIndicator(
            strokeWidth: 2,
            value: download != null && download.progress > 0
                ? download.progress / 100
                : null,
          ),
        ),
      );
    }

    return Row(
      children: [
        if (downloadIndicator != null) downloadIndicator,
        Expanded(
          child: Text(
            song.artist ?? song.album ?? '',
            maxLines: 1,
            softWrap: false,
            overflow: TextOverflow.fade,
            style: TextStyle(
              color: inheritedStyle.color,
            ),
          ),
        ),
      ],
    );
  }
}

class _SongTitle extends HookConsumerWidget {
  final Song song;

  const _SongTitle({
    required this.song,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mediaItem = ref.watch(mediaItemProvider).valueOrNull;
    final mediaItemData = ref.watch(mediaItemDataProvider);
    final inheritedStyle = DefaultTextStyle.of(context).style;
    final theme = Theme.of(context);
    final queueContext = QueueContext.maybeOf(context);

    final playing = mediaItem != null &&
        mediaItemData != null &&
        mediaItem.id == song.id &&
        mediaItemData.contextId == queueContext?.id &&
        mediaItemData.contextType == queueContext?.type;

    return Row(
      children: [
        if (playing)
          Padding(
            padding: const EdgeInsetsDirectional.only(end: 2),
            child: Icon(
              Icons.play_arrow_rounded,
              size: 18,
              color: theme.colorScheme.primary,
            ),
          ),
        Expanded(
          child: Text(
            song.title,
            maxLines: 1,
            softWrap: false,
            overflow: TextOverflow.fade,
            style: TextStyle(
              color: playing ? theme.colorScheme.primary : inheritedStyle.color,
            ),
          ),
        ),
      ],
    );
  }
}

class FabPadding extends StatelessWidget {
  const FabPadding({super.key});

  @override
  Widget build(BuildContext context) {
    return const SizedBox(height: 86);
  }
}
