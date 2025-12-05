import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../../../database/query.dart';
import '../../../sources/models.dart';
import '../../hooks/use_on_source.dart';
import '../../hooks/use_paging_controller.dart';
import '../../state/database.dart';
import 'items.dart';

const kPageSize = 30;

class SongsList extends HookConsumerWidget {
  const SongsList({
    super.key,
    required this.query,
  });

  final SongsQuery query;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final controller = usePagingController<int, Song>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) => db.libraryDao.listSongs(
        query.copyWith(
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
          builderDelegate: PagedChildBuilderDelegate<Song>(
            itemBuilder: (context, item, index) {
              return SongListTile(
                song: item,
                onTap: () async {},
              );
            },
          ),
        );
      },
    );
  }
}
