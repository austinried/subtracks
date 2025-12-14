import 'package:drift/drift.dart' show InsertMode, Value;
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/database.dart';

final databaseInitializer = FutureProvider<SubtracksDatabase>((ref) async {
  final db = SubtracksDatabase();

  await db.batch((batch) {
    batch.insertAll(
      db.sources,
      [
        SourcesCompanion.insert(
          id: Value(1),
          name: 'test subsonic',
          isActive: Value(true),
        ),
        SourcesCompanion.insert(
          id: Value(2),
          name: 'test navidrome',
          isActive: Value(null),
        ),
      ],
      mode: InsertMode.insertOrIgnore,
    );
    batch.insertAllOnConflictUpdate(db.subsonicSettings, [
      SubsonicSettingsCompanion.insert(
        sourceId: Value(1),
        address: Uri.parse('http://demo.subsonic.org'),
        username: 'guest1',
        password: 'guest',
        useTokenAuth: Value(true),
      ),
      SubsonicSettingsCompanion.insert(
        sourceId: Value(2),
        address: Uri.parse('http://10.0.2.2:4533'),
        username: 'admin',
        password: 'password',
        useTokenAuth: Value(true),
      ),
    ]);
  });

  return db;
});

final databaseProvider = Provider<SubtracksDatabase>((ref) {
  return ref.watch(databaseInitializer).requireValue;
});
