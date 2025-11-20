import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../state/database.dart';
import '../state/settings.dart';
import '../state/source.dart';

class PreloadScreen extends HookConsumerWidget {
  const PreloadScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final initializers = [
      ref.watch(databaseInitializer),
      ref.watch(activeSourceInitializer),
      ref.watch(packageInfoInitializer),
    ];

    if (initializers.every((v) => v.hasValue)) {
      Future.delayed(Duration(microseconds: 1)).then((_) {
        if (context.mounted) {
          context.replace('/');
        }
      });
    }

    return Container();
  }
}
