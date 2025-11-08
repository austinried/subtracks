import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../images/images.dart';
import '../../sources/models.dart';
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
        child: CoverArtImage(coverArt: album.coverArt),
      ),
    );
  }
}

class ArtistListTile extends StatelessWidget {
  const ArtistListTile({super.key});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: CircleClip(
        child: CachedNetworkImage(
          imageUrl: 'https://placehold.net/400x400.png',
          placeholder: (context, url) => CircularProgressIndicator(),
          errorWidget: (context, url, error) => Icon(Icons.error),
        ),
      ),
      title: Text('Some Artist'),
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
