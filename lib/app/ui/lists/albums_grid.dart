import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../../../sources/models.dart';
import '../../hooks/use_on_source.dart';
import '../../hooks/use_paging_controller.dart';
import '../../state/database.dart';
import '../../state/lists.dart';
import '../../state/source.dart';
import '../menus.dart';
import 'items.dart';

const kPageSize = 60;

class AlbumsGrid extends HookConsumerWidget {
  const AlbumsGrid({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final query = ref.watch(albumsQueryProvider);

    final controller = usePagingController<int, Album>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) => db.libraryDao.listAlbums(
        query.copyWith(
          sourceId: ref.read(sourceIdProvider),
          limit: kPageSize,
          offset: (pageKey - 1) * kPageSize,
        ),
      ),
    );

    useOnSourceSync(ref, controller.refresh);
    useValueChanged(query, (_, _) => controller.refresh());

    return PagingListener(
      controller: controller,
      builder: (context, state, fetchNextPage) {
        return SliverPadding(
          padding: const EdgeInsets.all(8.0),
          sliver: PagedSliverGrid(
            state: state,
            fetchNextPage: fetchNextPage,
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
            ),
            showNoMoreItemsIndicatorAsGridChild: false,
            builderDelegate: PagedChildBuilderDelegate<Album>(
              noMoreItemsIndicatorBuilder: (context) => FabPadding(),
              itemBuilder: (context, item, index) => AlbumGridTile(
                album: item,
                onTap: () async {
                  context.push('/albums/${item.id}');
                },
              ),
            ),
          ),
        );
      },
    );
  }
}

class AlbumsGridFilters extends HookConsumerWidget {
  const AlbumsGridFilters({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ListView(
      children: [],
    );
  }
}
