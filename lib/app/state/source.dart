import 'package:drift/drift.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../sources/music_source.dart';
import '../../sources/subsonic/client.dart';
import '../../sources/subsonic/source.dart';
import '../../util/http.dart';
import 'database.dart';
import 'settings.dart';

final sourceInitializer = FutureProvider<MusicSource>((ref) async {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  final query = db.sources.select().join([
    leftOuterJoin(
      db.subsonicSettings,
      db.subsonicSettings.sourceId.equalsExp(db.sources.id),
    ),
  ])..where(db.sources.id.equals(sourceId));

  final result = await query.getSingle();
  final subsonicSettings = result.readTable(db.subsonicSettings);

  return SubsonicSource(
    SubsonicClient(
      http: SubtracksHttpClient(),
      address: subsonicSettings.address,
      username: subsonicSettings.username,
      password: subsonicSettings.password,
      useTokenAuth: subsonicSettings.useTokenAuth,
    ),
  );
});

final sourceProvider = Provider<MusicSource>((ref) {
  return ref.watch(sourceInitializer).value!;
});
