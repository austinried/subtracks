import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../sources/subsonic/client.dart';
import '../../sources/subsonic/source.dart';
import '../../util/http.dart';
import 'database.dart';

final activeSourceInitializer = StreamProvider<(int, SubsonicSource)>((
  ref,
) async* {
  final db = ref.watch(databaseProvider);

  final activeSource = db.sourcesDao.activeSourceId().watchSingle();

  await for (final sourceId in activeSource) {
    final subsonicSettings = await db.managers.subsonicSettings
        .filter((f) => f.sourceId.equals(sourceId))
        .getSingle();

    yield (
      sourceId!,
      SubsonicSource(
        SubsonicClient(
          http: SubtracksHttpClient(),
          address: subsonicSettings.address,
          username: subsonicSettings.username,
          password: subsonicSettings.password,
          useTokenAuth: subsonicSettings.useTokenAuth,
        ),
      ),
    );
  }
});

final sourceProvider = Provider<SubsonicSource>((ref) {
  return ref.watch(activeSourceInitializer).value!.$2;
});

final sourceIdProvider = Provider<int>((ref) {
  return ref.watch(activeSourceInitializer).value!.$1;
});
