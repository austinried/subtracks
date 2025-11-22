import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../../database/dao/library_dao.dart';
import '../hooks/use_on_source.dart';
import '../hooks/use_paging_controller.dart';
import '../state/database.dart';
import 'list_items.dart';

const kPageSize = 30;

class ArtistsList extends HookConsumerWidget {
  const ArtistsList({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final controller = usePagingController<int, AristListItem>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) => db.libraryDao.listArtists(
        limit: kPageSize,
        offset: (pageKey - 1) * kPageSize,
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
          builderDelegate: PagedChildBuilderDelegate<AristListItem>(
            itemBuilder: (context, item, index) {
              final (:artist, :albumCount) = item;

              return ArtistListTile(
                artist: artist,
                albumCount: albumCount,
                onTap: () async {
                  context.push('/artist/${artist.id}');
                },
              );
            },
          ),
        );
      },
    );
  }
}
