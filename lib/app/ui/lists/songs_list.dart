import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../../../database/dao/library_dao.dart';
import '../../../database/query.dart';
import '../../hooks/use_on_source.dart';
import '../../hooks/use_paging_controller.dart';
import '../../state/database.dart';
import '../../state/source.dart';

const kPageSize = 30;

class SongsList extends HookConsumerWidget {
  const SongsList({
    super.key,
    required this.query,
    required this.itemBuilder,
  });

  final SongsQuery query;
  final Widget Function(BuildContext context, SongListItem item, int index)
  itemBuilder;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final controller = usePagingController<int, SongListItem>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) => db.libraryDao.listSongs(
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
          builderDelegate: PagedChildBuilderDelegate<SongListItem>(
            itemBuilder: itemBuilder,
          ),
        );
      },
    );
  }
}
