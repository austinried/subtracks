import 'package:auto_route/auto_route.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:sliver_tools/sliver_tools.dart';

import '../../database/database.dart';
import '../../models/music.dart';
import '../../models/query.dart';
import '../../models/support.dart';
import '../../services/audio_service.dart';
import '../../services/cache_service.dart';
import '../../state/music.dart';
import '../../state/settings.dart';
import '../app_router.dart';
import '../buttons.dart';
import '../images.dart';
import '../items.dart';

part 'browse_page.g.dart';

@riverpod
Stream<List<Album>> albumsCategoryList(
  AlbumsCategoryListRef ref,
  ListQuery opt,
) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.albumsList(sourceId, opt).watch();
}

class BrowsePage extends HookConsumerWidget {
  const BrowsePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);

    final frequent = ref
        .watch(albumsCategoryListProvider(const ListQuery(
          page: Pagination(limit: 20),
          sort: SortBy(column: 'frequent_rank'),
          filters: IListConst([
            FilterWith.isNull(column: 'frequent_rank', invert: true),
          ]),
        )))
        .valueOrNull;
    final recent = ref
        .watch(albumsCategoryListProvider(const ListQuery(
          page: Pagination(limit: 20),
          sort: SortBy(column: 'recent_rank'),
          filters: IListConst([
            FilterWith.isNull(column: 'recent_rank', invert: true),
          ]),
        )))
        .valueOrNull;
    final starred = ref
        .watch(albumsCategoryListProvider(const ListQuery(
          page: Pagination(limit: 20),
          sort: SortBy(column: 'starred'),
          filters: IListConst([
            FilterWith.isNull(column: 'starred', invert: true),
          ]),
        )))
        .valueOrNull;
    final random = ref
        .watch(albumsCategoryListProvider(const ListQuery(
          page: Pagination(limit: 20),
          sort: SortBy(column: 'RANDOM()'),
        )))
        .valueOrNull;

    final genres = ref
        .watch(albumGenresProvider(const Pagination(
          limit: 20,
        )))
        .valueOrNull;

    return Scaffold(
      floatingActionButton: RadioPlayFab(
        onPressed: () {
          ref.read(audioControlProvider).playRadio(
                context: QueueContextType.library,
                getSongs: (query) => ref
                    .read(databaseProvider)
                    .songsList(ref.read(sourceIdProvider), query)
                    .get(),
              );
        },
      ),
      body: CustomScrollView(
        slivers: [
          const SliverSafeArea(
            sliver: SliverPadding(padding: EdgeInsets.only(top: 8)),
          ),
          _GenreCategory(
            title: 'Genres',
            items: genres?.toList() ?? [],
          ),
          _AlbumCategory(
            title: l.resourcesSortByFrequentlyPlayed,
            items: frequent ?? [],
          ),
          _AlbumCategory(
            title: l.resourcesSortByRecentlyPlayed,
            items: recent ?? [],
          ),
          _AlbumCategory(
            title: l.resourcesFilterStarred,
            items: starred ?? [],
          ),
          _AlbumCategory(
            title: l.resourcesSortByRandom,
            items: random ?? [],
          ),
          const SliverToBoxAdapter(child: FabPadding()),
        ],
      ),
    );
  }
}

class _GenreCategory extends HookConsumerWidget {
  final String title;
  final List<String> items;

  const _GenreCategory({
    required this.title,
    required this.items,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SliverPadding(
      padding: const EdgeInsets.only(bottom: 16),
      sliver: _Category(
        title: title,
        height: 140,
        itemWidth: 140,
        items: items.map((genre) => _GenreItem(genre: genre)).toList(),
      ),
    );
  }
}

class _GenreItem extends HookConsumerWidget {
  final String genre;

  const _GenreItem({
    required this.genre,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final albums = ref
        .watch(albumsByGenreProvider(
          genre,
          const Pagination(limit: 4),
        ))
        .valueOrNull;
    final cache = ref.watch(cacheServiceProvider);

    final theme = Theme.of(context);

    if (albums == null) {
      return Container();
    }

    return ImageCard(
      onTap: () {
        context.navigateTo(GenreSongsRoute(genre: genre));
      },
      child: Stack(
        alignment: AlignmentDirectional.center,
        children: [
          CardClip(
            child: MultiImage(
              cacheInfo: albums.map((album) => cache.albumArt(album)),
            ),
          ),
          Material(
            type: MaterialType.canvas,
            color: theme.colorScheme.secondaryContainer,
            elevation: 5,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
              child: SizedBox(
                width: double.infinity,
                child: Text(
                  genre,
                  textAlign: TextAlign.center,
                  style: theme.textTheme.labelLarge,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _AlbumCategory extends HookConsumerWidget {
  final String title;
  final List<Album> items;

  const _AlbumCategory({
    required this.title,
    required this.items,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return _Category(
      title: title,
      height: 190,
      itemWidth: 140,
      items: items
          .map(
            (album) => AlbumCard(
              album: album,
              onTap: () => context.navigateTo(
                AlbumSongsRoute(
                  id: album.id,
                ),
              ),
            ),
          )
          .toList(),
    );
  }
}

class _Category extends HookConsumerWidget {
  final String title;
  final List<Widget> items;
  final double height;
  final double itemWidth;

  const _Category({
    required this.title,
    required this.items,
    required this.height,
    required this.itemWidth,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    return MultiSliver(
      children: [
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
            child: Text(
              title,
              style: theme.textTheme.headlineMedium,
            ),
          ),
        ),
        SliverToBoxAdapter(
          child: SizedBox(
            height: height,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              scrollDirection: Axis.horizontal,
              itemCount: items.length,
              itemBuilder: (context, index) => SizedBox(
                width: itemWidth,
                child: items[index],
              ),
              separatorBuilder: (context, index) => const SizedBox(width: 8),
            ),
          ),
        ),
      ],
    );
  }
}
