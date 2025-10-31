import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../hooks/use_paging_controller.dart';
import 'list_items.dart';

class AlbumsGrid extends HookWidget {
  const AlbumsGrid({super.key});

  @override
  Widget build(BuildContext context) {
    final controller = usePagingController<int, String>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) => List.generate(30, (_) => pageKey.toString()),
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
          builderDelegate: PagedChildBuilderDelegate<String>(
            itemBuilder: (context, item, index) => AlbumGridTile(
              onTap: () {
                context.push('/album');
              },
            ),
          ),
        );
      },
    );
  }
}
