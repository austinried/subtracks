import 'package:drift/drift.dart' show Value;
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/database.dart';

final databaseInitializer = FutureProvider<SubtracksDatabase>((ref) async {
  final db = SubtracksDatabase();

  await db
      .into(db.sources)
      .insertOnConflictUpdate(
        SourcesCompanion.insert(
          id: Value(1),
          name: 'test navidrome',
        ),
      );
  await db
      .into(db.subsonicSettings)
      .insertOnConflictUpdate(
        SubsonicSettingsCompanion.insert(
          sourceId: Value(1),
          address: Uri.parse('http://demo.subsonic.org'),
          username: 'guest1',
          password: 'guest',
          // address: Uri.parse('http://10.0.2.2:4533'),
          // username: 'admin',
          // password: 'password',
          useTokenAuth: Value(true),
        ),
      );

  return db;
});

final databaseProvider = Provider<SubtracksDatabase>((ref) {
  return ref.watch(databaseInitializer).requireValue;
});
