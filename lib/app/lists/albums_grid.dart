import 'package:drift/drift.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../../sources/models.dart';
import '../hooks/use_paging_controller.dart';
import '../state/database.dart';
import '../state/settings.dart';
import 'list_items.dart';

const kPageSize = 30;

class AlbumsGrid extends HookConsumerWidget {
  const AlbumsGrid({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final sourceId = ref.watch(sourceIdProvider);

    final controller = usePagingController<int, Album>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) async {
        final query = db.albums.select()
          ..where((f) => f.sourceId.equals(sourceId))
          ..limit(kPageSize, offset: (pageKey - 1) * kPageSize);

        return await query.get();
      },
    );

    return PagingListener(
      controller: controller,
      builder: (context, state, fetchNextPage) {
        return PagedSliverGrid(
          state: state,
          fetchNextPage: fetchNextPage,
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 3,
          ),
          builderDelegate: PagedChildBuilderDelegate<Album>(
            itemBuilder: (context, item, index) => AlbumGridTile(
              album: item,
              onTap: () async {
                context.push('/album/${item.id}');
              },
            ),
          ),
        );
      },
    );
  }
}
