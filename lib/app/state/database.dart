import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/database.dart';

final databaseProvider = Provider<SubtracksDatabase>((ref) {
  return SubtracksDatabase();
});
