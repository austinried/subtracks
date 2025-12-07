import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../../sources/models.dart';
import '../../util/clip.dart';
import '../images.dart';

class AlbumGridTile extends HookConsumerWidget {
  const AlbumGridTile({
    super.key,
    required this.album,
    this.onTap,
  });

  final Album album;
  final void Function()? onTap;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return CardTheme(
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadiusGeometry.circular(3),
      ),
      margin: EdgeInsets.all(2),
      child: ImageCard(
        onTap: onTap,
        child: CoverArtImage(
          coverArt: album.coverArt,
          thumbnail: true,
        ),
      ),
    );
  }
}

class ArtistListTile extends StatelessWidget {
  const ArtistListTile({
    super.key,
    required this.artist,
    required this.albumCount,
    this.onTap,
  });

  final Artist artist;
  final int albumCount;
  final void Function()? onTap;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: CircleClip(
        child: CoverArtImage(
          coverArt: artist.coverArt,
          thumbnail: true,
        ),
      ),
      title: Text(artist.name),
      subtitle: Text('$albumCount albums'),
      onTap: onTap,
    );
  }
}

class AlbumListTile extends StatelessWidget {
  const AlbumListTile({
    super.key,
    required this.album,
    this.onTap,
  });

  final Album album;
  final void Function()? onTap;

  @override
  Widget build(BuildContext context) {
    final textTheme = TextTheme.of(context);

    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Padding(
              padding: const EdgeInsets.only(left: 8, right: 18),
              child: RoundedBoxClip(
                child: CoverArtImage(
                  coverArt: album.coverArt,
                  thumbnail: true,
                  width: 80,
                  height: 80,
                ),
              ),
            ),
            Flexible(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    album.name,
                    style: textTheme.bodyLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                    overflow: TextOverflow.ellipsis,
                    maxLines: 2,
                  ),
                  Text(
                    album.albumArtist ?? 'Unknown album artist',
                    style: textTheme.bodyMedium,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class PlaylistListTile extends StatelessWidget {
  const PlaylistListTile({
    super.key,
    required this.playlist,
    this.albumCount,
    this.onTap,
  });

  final Playlist playlist;
  final int? albumCount;
  final void Function()? onTap;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: RoundedBoxClip(
        child: CoverArtImage(
          coverArt: playlist.coverArt,
          thumbnail: true,
        ),
      ),
      title: Text(playlist.name),
      subtitle: Text(playlist.comment ?? ''),
      onTap: onTap,
    );
  }
}

class SongListTile extends StatelessWidget {
  const SongListTile({
    super.key,
    required this.song,
    this.coverArt,
    this.showLeading = false,
    this.onTap,
  });

  final Song song;
  final String? coverArt;
  final bool showLeading;
  final void Function()? onTap;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: showLeading
          ? RoundedBoxClip(
              child: CoverArtImage(
                coverArt: coverArt,
                thumbnail: true,
              ),
            )
          : null,
      title: Text(song.title),
      subtitle: Text(song.artist ?? ''),
      onTap: onTap,
    );
  }
}

class ImageCard extends StatelessWidget {
  const ImageCard({
    super.key,
    required this.child,
    this.onTap,
    this.onLongPress,
  });

  final Widget child;
  final void Function()? onTap;
  final void Function()? onLongPress;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Stack(
        fit: StackFit.passthrough,
        alignment: Alignment.center,
        children: [
          child,
          Positioned.fill(
            child: Material(
              type: MaterialType.transparency,
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
