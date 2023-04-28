import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/database.dart';
import '../../state/settings.dart';
import '../app_router.dart';
import '../hooks/use_list_query_paging_controller.dart';
import '../items.dart';
import '../lists.dart';

class LibraryAlbumsPage extends HookConsumerWidget {
  const LibraryAlbumsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final pagingController = useLibraryPagingController(
      ref,
      libraryTabIndex: 0,
      getItems: (query) {
        final db = ref.read(databaseProvider);
        final sourceId = ref.read(sourceIdProvider);

        return ref.read(offlineModeProvider)
            ? db.albumsListDownloaded(sourceId, query).get()
            : db.albumsList(sourceId, query).get();
      },
    );

    return PagedGridQueryView(
      pagingController: pagingController,
      refreshSyncAll: true,
      itemBuilder: (context, item, index, size) => AlbumCard(
        album: item,
        style:
            size == GridSize.small ? CardStyle.imageOnly : CardStyle.withText,
        onTap: () => context.navigateTo(AlbumSongsRoute(id: item.id)),
      ),
    );
  }
}
