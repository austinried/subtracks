import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:sliver_tools/sliver_tools.dart';

class ThemedGradient extends LinearGradient {
  const ThemedGradient({
    required super.colors,
    super.begin,
    super.end,
  });

  factory ThemedGradient.of(
    BuildContext context, {
    AlignmentGeometry begin = Alignment.topCenter,
    AlignmentGeometry end = Alignment.bottomCenter,
  }) {
    final colorScheme = Theme.of(context).colorScheme;

    return ThemedGradient(
      begin: begin,
      end: end,
      colors: [
        colorScheme.primaryContainer,
        colorScheme.surface,
      ],
    );
  }
}

class GradientScrollView extends HookConsumerWidget {
  const GradientScrollView({
    super.key,
    required this.slivers,
  });

  final List<Widget> slivers;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return CustomScrollView(
      slivers: [
        SliverStack(
          children: [
            SliverPositioned.directional(
              textDirection: TextDirection.ltr,
              start: 0,
              end: 0,
              top: 0,
              child: Ink(
                width: double.infinity,
                height: MediaQuery.heightOf(context),
                decoration: BoxDecoration(
                  gradient: ThemedGradient.of(context),
                ),
              ),
            ),
            MultiSliver(children: slivers),
          ],
        ),
      ],
    );
  }
}
