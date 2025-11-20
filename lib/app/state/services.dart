import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../services/sync_service.dart';
import 'database.dart';
import 'source.dart';

final syncServiceProvider = Provider<SyncService>((ref) {
  final db = ref.watch(databaseProvider);
  final source = ref.watch(sourceProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return SyncService(source: source, db: db, sourceId: sourceId);
});
