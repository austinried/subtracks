import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../sources/models.dart';
import '../ui/images.dart';
import '../util/clip.dart';

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
    this.albumCount,
    this.onTap,
  });

  final Artist artist;
  final int? albumCount;
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
      subtitle: albumCount != null ? Text('$albumCount albums') : null,
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
