import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../../../database/query.dart';
import '../../../sources/models.dart';
import '../../hooks/use_on_source.dart';
import '../../hooks/use_paging_controller.dart';
import '../../state/database.dart';
import '../../state/source.dart';
import 'items.dart';

const kPageSize = 60;

class AlbumsGrid extends HookConsumerWidget {
  const AlbumsGrid({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final controller = usePagingController<int, Album>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) => db.libraryDao.listAlbums(
        AlbumsQuery(
          sourceId: ref.read(sourceIdProvider),
          sort: IList([
            SortingTerm.albumsDesc(AlbumsColumn.created),
          ]),
          limit: kPageSize,
          offset: (pageKey - 1) * kPageSize,
        ),
      ),
    );

    useOnSourceChange(ref, (_) => controller.refresh());
    useOnSourceSync(ref, controller.refresh);

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
            builderDelegate: PagedChildBuilderDelegate<Album>(
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
