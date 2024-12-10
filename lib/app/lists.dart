import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

import '../services/sync_service.dart';
import 'items.dart';
import 'snackbars.dart';

class PagedListQueryView<T> extends HookConsumerWidget {
  final PagingController<int, T> pagingController;
  final bool refreshSyncAll;
  final bool fabPadding;
  final bool useSliver;
  final Widget Function(BuildContext context, T item, int index) itemBuilder;

  const PagedListQueryView({
    super.key,
    required this.pagingController,
    this.refreshSyncAll = false,
    this.fabPadding = true,
    this.useSliver = false,
    required this.itemBuilder,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final builderDelegate = PagedChildBuilderDelegate<T>(
      itemBuilder: (context, item, index) => itemBuilder(context, item, index),
      noMoreItemsIndicatorBuilder:
          fabPadding ? (context) => const FabPadding() : null,
    );

    final listView = useSliver
        ? PagedSliverList<int, T>(
            pagingController: pagingController,
            builderDelegate: builderDelegate,
          )
        : PagedListView<int, T>(
            pagingController: pagingController,
            builderDelegate: builderDelegate,
          );

    if (refreshSyncAll) {
      return SyncAllRefresh(child: listView);
    } else {
      return listView;
    }
  }
}

enum GridSize {
  small,
  large,
}

class PagedGridQueryView<T> extends HookConsumerWidget {
  final PagingController<int, T> pagingController;
  final bool refreshSyncAll;
  final bool fabPadding;
  final GridSize size;
  final Widget Function(BuildContext context, T item, int index, GridSize size)
      itemBuilder;

  const PagedGridQueryView({
    super.key,
    required this.pagingController,
    this.refreshSyncAll = false,
    this.fabPadding = true,
    this.size = GridSize.small,
    required this.itemBuilder,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    SliverGridDelegate gridDelegate;
    double spacing;

    if (size == GridSize.small) {
      spacing = 4;
      gridDelegate = SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        mainAxisSpacing: spacing,
        crossAxisSpacing: spacing,
      );
    } else {
      spacing = 12;
      gridDelegate = SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: spacing,
        crossAxisSpacing: spacing,
      );
    }

    final listView = PagedGridView<int, T>(
      padding: MediaQuery.of(context).padding + EdgeInsets.all(spacing),
      pagingController: pagingController,
      builderDelegate: PagedChildBuilderDelegate(
        itemBuilder: (context, item, index) =>
            itemBuilder(context, item, index, size),
        noMoreItemsIndicatorBuilder:
            fabPadding ? (context) => const FabPadding() : null,
      ),
      gridDelegate: gridDelegate,
      showNoMoreItemsIndicatorAsGridChild: false,
    );

    if (refreshSyncAll) {
      return SyncAllRefresh(child: listView);
    } else {
      return listView;
    }
  }
}

class SyncAllRefresh extends HookConsumerWidget {
  final Widget child;

  const SyncAllRefresh({
    super.key,
    required this.child,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return RefreshIndicator(
      onRefresh: () async {
        try {
          await ref.read(syncServiceProvider.notifier).syncAll();
        } catch (e) {
          if (!context.mounted) return;
          showErrorSnackbar(context, e.toString());
        }
      },
      child: child,
    );
  }
}
