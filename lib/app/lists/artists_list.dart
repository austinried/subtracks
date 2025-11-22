import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../../sources/models.dart';
import '../hooks/use_paging_controller.dart';
import '../state/database.dart';
import '../state/source.dart';
import 'list_items.dart';

const kPageSize = 30;

typedef _ArtistItem = ({Artist artist, int? albumCount});

class ArtistsList extends HookConsumerWidget {
  const ArtistsList({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);
    final sourceId = ref.watch(sourceIdProvider);

    final controller = usePagingController<int, _ArtistItem>(
      getNextPageKey: (state) =>
          state.lastPageIsEmpty ? null : state.nextIntPageKey,
      fetchPage: (pageKey) => db.libraryDao.listArtists(
        limit: kPageSize,
        offset: (pageKey - 1) * kPageSize,
      ),
    );

    useEffect(() {
      controller.refresh();
      return;
    }, [sourceId]);

    return PagingListener(
      controller: controller,
      builder: (context, state, fetchNextPage) {
        return PagedSliverList(
          state: state,
          fetchNextPage: fetchNextPage,
          builderDelegate: PagedChildBuilderDelegate<_ArtistItem>(
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
