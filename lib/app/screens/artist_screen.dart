import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/query.dart';
import '../../sources/models.dart';
import '../state/database.dart';
import '../state/source.dart';
import '../ui/cover_art_theme.dart';
import '../ui/gradient.dart';
import '../ui/images.dart';
import '../ui/lists/albums_list.dart';

class ArtistScreen extends HookConsumerWidget {
  const ArtistScreen({
    super.key,
    required this.id,
  });

  final String id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final sourceId = ref.watch(sourceIdProvider);

    final getArtist = useMemoized(
      () => db.libraryDao.getArtist(sourceId, id).getSingle(),
    );
    final artist = useFuture(getArtist).data;

    if (artist == null) {
      return Container();
    }

    final query = AlbumsQuery(
      sourceId: sourceId,
      filter: IList([AlbumsFilter.artistId(artist.id)]),
      sort: IList([
        SortingTerm.albumsDesc(AlbumsColumn.year),
        SortingTerm.albumsAsc(AlbumsColumn.name),
      ]),
    );

    return CoverArtTheme(
      coverArt: artist.coverArt,
      child: Scaffold(
        body: GradientScrollView(
          slivers: [
            ArtistHeader(artist: artist),
            AlbumsList(query: query),
          ],
        ),
      ),
    );
  }
}

class ArtistHeader extends StatelessWidget {
  const ArtistHeader({
    super.key,
    required this.artist,
  });

  final Artist artist;

  @override
  Widget build(BuildContext context) {
    final textTheme = TextTheme.of(context);
    final colorScheme = ColorScheme.of(context);

    return SliverToBoxAdapter(
      child: Stack(
        fit: StackFit.passthrough,
        alignment: AlignmentGeometry.bottomCenter,
        children: [
          CoverArtImage(
            fit: BoxFit.cover,
            coverArt: artist.coverArt,
            thumbnail: false,
            height: 350,
          ),
          Column(
            children: [
              Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: AlignmentGeometry.centerRight,
                    end: AlignmentGeometry.centerLeft,
                    colors: [
                      colorScheme.surface.withAlpha(220),
                      colorScheme.surface.withAlpha(150),
                      colorScheme.surface.withAlpha(20),
                    ],
                  ),
                ),
                child: Padding(
                  padding: EdgeInsetsGeometry.symmetric(
                    vertical: 12,
                    horizontal: 16,
                  ),
                  child: Text(
                    artist.name,
                    textAlign: TextAlign.right,
                    style: textTheme.headlineLarge?.copyWith(
                      shadows: [
                        Shadow(
                          blurRadius: 20,
                          color: colorScheme.surface,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
