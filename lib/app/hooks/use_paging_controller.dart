import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

PagingController<PageKeyType, ItemType>
usePagingController<PageKeyType, ItemType>({
  required PageKeyType? Function(PagingState<PageKeyType, ItemType>)
  getNextPageKey,
  required FutureOr<List<ItemType>> Function(PageKeyType) fetchPage,
}) {
  return use(
    _PagingControllerHook<PageKeyType, ItemType>(
      getNextPageKey: getNextPageKey,
      fetchPage: fetchPage,
    ),
  );
}

class _PagingControllerHook<PageKeyType, ItemType>
    extends Hook<PagingController<PageKeyType, ItemType>> {
  const _PagingControllerHook({
    super.keys,
    required this.getNextPageKey,
    required this.fetchPage,
  });

  final PageKeyType? Function(PagingState<PageKeyType, ItemType>)
  getNextPageKey;
  final FutureOr<List<ItemType>> Function(PageKeyType) fetchPage;

  @override
  HookState<
    PagingController<PageKeyType, ItemType>,
    Hook<PagingController<PageKeyType, ItemType>>
  >
  createState() => _PagingControllerHookState<PageKeyType, ItemType>();
}

class _PagingControllerHookState<PageKeyType, ItemType>
    extends
        HookState<
          PagingController<PageKeyType, ItemType>,
          _PagingControllerHook<PageKeyType, ItemType>
        > {
  late final controller = PagingController<PageKeyType, ItemType>(
    getNextPageKey: hook.getNextPageKey,
    fetchPage: hook.fetchPage,
  );

  @override
  PagingController<PageKeyType, ItemType> build(BuildContext context) =>
      controller;

  @override
  void dispose() => controller.dispose();

  @override
  String get debugLabel => 'usePagingController';
}
