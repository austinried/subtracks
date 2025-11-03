import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../util/clip.dart';

class AlbumGridTile extends StatelessWidget {
  const AlbumGridTile({
    super.key,
    this.onTap,
  });

  final void Function()? onTap;

  @override
  Widget build(BuildContext context) {
    return CardTheme(
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadiusGeometry.circular(3),
      ),
      margin: EdgeInsets.all(2),
      child: ImageCard(
        onTap: onTap,
        child: CachedNetworkImage(
          imageUrl: 'https://placehold.net/400x400.png',
          placeholder: (context, url) => CircularProgressIndicator(),
          errorWidget: (context, url, error) => Icon(Icons.error),
        ),
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
