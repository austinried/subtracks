import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../state/services.dart';
import '../state/source.dart';

void useOnSourceChange(WidgetRef ref, void Function(int sourceId) callback) {
  final sourceId = ref.watch(sourceIdProvider);

  useEffect(() {
    callback(sourceId);
    return;
  }, [sourceId]);
}

void useOnSourceSync(WidgetRef ref, void Function() callback) {
  final syncService = ref.watch(syncServiceProvider);

  useOnListenableChange(syncService, () {
    callback();
  });
}
