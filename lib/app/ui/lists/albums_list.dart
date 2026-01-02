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
import '../menus.dart';
import 'items.dart';

const kPageSize = 30;

class AlbumsList extends HookConsumerWidget {
  const AlbumsList({
    super.key,
    required this.query,
  });

  final AlbumsQuery query;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
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

    useOnSourceChange(ref, (_) => controller.refresh());
    useOnSourceSync(ref, controller.refresh);

    return PagingListener(
      controller: controller,
      builder: (context, state, fetchNextPage) {
        return PagedSliverList(
          state: state,
          fetchNextPage: fetchNextPage,
          builderDelegate: PagedChildBuilderDelegate<Album>(
            noMoreItemsIndicatorBuilder: (context) => FabPadding(),
            itemBuilder: (context, item, index) {
              final tile = AlbumListTile(
                album: item,
                onTap: () {
                  context.push('/albums/${item.id}');
                },
              );

              final currentItemYear = item.year;
              final previousItemYear = index == 0
                  ? currentItemYear
                  : controller.items?.elementAtOrNull(index - 1)?.year;

              if (index == 0 || currentItemYear != previousItemYear) {
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.only(
                        top: 24,
                        bottom: 8,
                        left: 16,
                        right: 16,
                      ),
                      child: Text(
                        item.year?.toString() ?? 'Unknown year',
                        style: TextTheme.of(context).headlineMedium,
                      ),
                    ),
                    tile,
                  ],
                );
              }

              return tile;
            },
          ),
        );
      },
    );
  }
}
