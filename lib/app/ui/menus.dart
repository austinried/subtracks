import 'package:flutter/material.dart';
import 'package:material_symbols_icons/symbols.dart';

Future<void> showContextMenu({
  required BuildContext context,
  required WidgetBuilder listBuilder,
}) => showModalBottomSheet(
  context: context,
  useRootNavigator: true,
  isScrollControlled: true,
  builder: (context) => DraggableScrollableSheet(
    expand: false,
    snap: true,
    initialChildSize: 0.3,
    minChildSize: 0.3,
    maxChildSize: 0.4,
    builder: (context, scrollController) => PrimaryScrollController(
      controller: scrollController,
      child: listBuilder(context),
    ),
  ),
);

class ContextMenuList extends StatelessWidget {
  const ContextMenuList({
    super.key,
    required this.children,
  });

  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return ListView(
      children: children,
    );
  }
}

class FabFilter extends StatelessWidget {
  const FabFilter({
    super.key,
    required this.listBuilder,
  });

  final WidgetBuilder listBuilder;

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton(
      onPressed: () {
        showContextMenu(
          context: context,
          listBuilder: listBuilder,
        );
      },
      child: Icon(
        Symbols.filter_list_rounded,
        weight: 500,
        opticalSize: 28,
        size: 28,
      ),
    );
  }
}

class FabPadding extends StatelessWidget {
  const FabPadding({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return const SizedBox(height: 86);
  }
}
