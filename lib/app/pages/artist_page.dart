import 'dart:math';

import 'package:auto_route/auto_route.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/database.dart';
import '../../models/query.dart';
import '../../models/support.dart';
import '../../services/audio_service.dart';
import '../../state/music.dart';
import '../../state/settings.dart';
import '../app_router.dart';
import '../buttons.dart';
import '../images.dart';
import '../items.dart';

class ArtistPage extends HookConsumerWidget {
  final String id;

  const ArtistPage({
    super.key,
    @pathParam required this.id,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.listen(sourceIdProvider, (_, __) => context.router.popUntilRoot());

    final artist = ref.watch(artistProvider(id));
    final albums = ref.watch(albumsByArtistIdProvider(id));

    return Scaffold(
      floatingActionButton: RadioPlayFab(
        onPressed: () => artist.hasValue
            ? ref.read(audioControlProvider).playRadio(
                  context: QueueContextType.artist,
                  contextId: artist.valueOrNull!.id,
                  query: ListQuery(
                    filters: IList([
                      FilterWith.equals(
                        column: 'artist_id',
                        value: artist.valueOrNull!.id,
                      )
                    ]),
                  ),
                  getSongs: (query) => ref
                      .read(databaseProvider)
                      .songsList(ref.read(sourceIdProvider), query)
                      .get(),
                )
            : null,
      ),
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Stack(
              alignment: Alignment.bottomCenter,
              fit: StackFit.passthrough,
              children: [
                ArtistArtImage(
                  artistId: id,
                  thumbnail: false,
                  height: 400,
                ),
                Padding(
                  padding: const EdgeInsets.all(12),
                  child: _Title(text: artist.valueOrNull?.name ?? ''),
                ),
              ],
            ),
          ),
          albums.when(
            data: (albums) {
              albums = albums.sort((a, b) => (b.year ?? 0) - (a.year ?? 0));
              return SliverPadding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 24, vertical: 24),
                sliver: SliverAlignedGrid.count(
                  crossAxisCount: 2,
                  mainAxisSpacing: 12,
                  crossAxisSpacing: 24,
                  itemCount: albums.length,
                  itemBuilder: (context, i) {
                    final album = albums.elementAt(i);
                    return AlbumCard(
                      album: album,
                      subtitle: AlbumSubtitle.year,
                      onTap: () => context.navigateTo(AlbumSongsRoute(
                        id: album.id,
                      )),
                    );
                  },
                ),
              );
            },
            error: (_, __) => SliverToBoxAdapter(
              child: Container(color: Colors.red),
            ),
            loading: () => const SliverToBoxAdapter(
              child: CircularProgressIndicator(),
            ),
          ),
        ],
      ),
    );
  }
}

class _Title extends StatelessWidget {
  final String text;

  const _Title({
    required this.text,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Text(
      text,
      textAlign: TextAlign.center,
      style: theme.textTheme.displayMedium!.copyWith(
        fontWeight: FontWeight.bold,
        color: Colors.white,
        shadows: [
          Shadow(
            offset: Offset.fromDirection(pi / 4, 3),
            blurRadius: 16,
            color: Colors.black26,
          ),
          Shadow(
            offset: Offset.fromDirection(3 * pi / 4, 3),
            blurRadius: 16,
            color: Colors.black26,
          ),
          Shadow(
            offset: Offset.fromDirection(5 * pi / 4, 3),
            blurRadius: 16,
            color: Colors.black26,
          ),
          Shadow(
            offset: Offset.fromDirection(7 * pi / 4, 3),
            blurRadius: 16,
            color: Colors.black26,
          ),
        ],
      ),
    );
  }
}
