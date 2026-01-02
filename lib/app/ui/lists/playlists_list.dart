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
import '../menus.dart';
import 'items.dart';

const kPageSize = 30;

class PlaylistsList extends HookConsumerWidget {
  const PlaylistsList({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final controller = usePagingController<int, Playlist>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) => db.libraryDao.listPlaylists(
        PlaylistsQuery(
          sourceId: ref.read(sourceIdProvider),
          sort: IList([
            SortingTerm.playlistsDesc(PlaylistsColumn.created),
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
        return PagedSliverList(
          state: state,
          fetchNextPage: fetchNextPage,
          builderDelegate: PagedChildBuilderDelegate<Playlist>(
            noMoreItemsIndicatorBuilder: (context) => FabPadding(),
            itemBuilder: (context, item, index) {
              return PlaylistListTile(
                playlist: item,
                onTap: () {
                  context.push('/playlists/${item.id}');
                },
              );
            },
          ),
        );
      },
    );
  }
}
