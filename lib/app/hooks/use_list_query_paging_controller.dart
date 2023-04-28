import 'dart:async';

import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../../models/query.dart';
import '../../services/sync_service.dart';
import '../../state/settings.dart';
import '../pages/library_page.dart';
import 'use_paging_controller.dart';

PagingController<int, T> useLibraryPagingController<T>(
  WidgetRef ref, {
  required int libraryTabIndex,
  required FutureOr<List<T>> Function(ListQuery query) getItems,
}) {
  final queryProvider = libraryListQueryProvider(libraryTabIndex).select(
    (value) => value.query,
  );
  final query = useState(ref.read(queryProvider));

  final onPageRequest = useCallback(
    (int pageKey, PagingController<int, T> pagingController) =>
        _pageRequest(getItems, query.value, pageKey, pagingController),
    [query.value],
  );

  final pagingController = usePagingController<int, T>(
    firstPageKey: query.value.page.offset,
    onPageRequest: onPageRequest,
  );

  ref.listen(queryProvider, (_, next) {
    query.value = next;
    pagingController.refresh();
  });

  ref.listen(syncServiceProvider, (_, __) => pagingController.refresh());
  ref.listen(sourceIdProvider, (_, __) => pagingController.refresh());
  ref.listen(offlineModeProvider, (_, __) => pagingController.refresh());

  return pagingController;
}

PagingController<int, T> useListQueryPagingController<T>(
  WidgetRef ref, {
  required ListQuery query,
  required FutureOr<List<T>> Function(ListQuery query) getItems,
}) {
  final onPageRequest = useCallback(
    (int pageKey, PagingController<int, T> pagingController) =>
        _pageRequest(getItems, query, pageKey, pagingController),
    [query],
  );

  final pagingController = usePagingController<int, T>(
    firstPageKey: query.page.offset,
    onPageRequest: onPageRequest,
  );

  return pagingController;
}

Future<void> _pageRequest<T>(
  FutureOr<List<T>> Function(ListQuery query) getItems,
  ListQuery query,
  int pageKey,
  PagingController<int, dynamic> pagingController,
) async {
  try {
    final newItems = await getItems(query.copyWith.page(offset: pageKey));

    final isFirstPage = newItems.isNotEmpty && pageKey == 0;
    final alreadyHasItems = pagingController.itemList != null &&
        pagingController.itemList!.isNotEmpty;
    if (isFirstPage && alreadyHasItems) {
      return;
    }

    final isLastPage = newItems.length < query.page.limit;
    if (isLastPage) {
      pagingController.appendLastPage(newItems);
    } else {
      final nextPageKey = pageKey + newItems.length;
      pagingController.appendPage(newItems, nextPageKey);
    }
  } catch (error) {
    pagingController.error = error;
  }
}
