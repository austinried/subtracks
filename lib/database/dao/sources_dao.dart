import 'package:drift/drift.dart';

import '../database.dart';

part 'sources_dao.g.dart';

typedef SourceSetting = ({Source source, SubsonicSetting subsonicSetting});

@DriftAccessor(include: {'../tables.drift'})
class SourcesDao extends DatabaseAccessor<SubtracksDatabase>
    with _$SourcesDaoMixin {
  SourcesDao(super.db);

  Selectable<int?> activeSourceId() {
    return (selectOnly(sources)
          ..addColumns([sources.id])
          ..where(sources.isActive.equals(true)))
        .map((row) => row.read(sources.id));
  }

  Stream<List<SourceSetting>> listSources() {
    final query = select(sources).join([
      innerJoin(
        subsonicSettings,
        sources.id.equalsExp(subsonicSettings.sourceId),
      ),
    ]);

    return query
        .map(
          (row) => (
            source: row.readTable(sources),
            subsonicSetting: row.readTable(subsonicSettings),
          ),
        )
        .watch();
  }

  Selectable<SourceSetting> getSource(int id) {
    final query = select(sources).join([
      innerJoin(
        subsonicSettings,
        sources.id.equalsExp(subsonicSettings.sourceId),
      ),
    ])..where(sources.id.equals(id));

    return query.map(
      (row) => (
        source: row.readTable(sources),
        subsonicSetting: row.readTable(subsonicSettings),
      ),
    );
  }

  Future<void> deleteSource(int id) async {
    await sources.deleteWhere((f) => f.id.equals(id));
  }

  Future<void> updateSource(Source source, SubsonicSetting subsonic) async {
    await db.transaction(() async {
      await sources.update().replace(source);
      await subsonicSettings.update().replace(subsonic);
    });
  }

  Future<void> createSource(
    SourcesCompanion source,
    SubsonicSettingsCompanion subsonic,
  ) async {
    await db.transaction(() async {
      final sourceId = await sources.insertOnConflictUpdate(source);

      await subsonicSettings.insertOnConflictUpdate(
        subsonic.copyWith(sourceId: Value(sourceId)),
      );
    });
  }

  Future<void> setActiveSource(int id) async {
    await transaction(() async {
      await db.managers.sources.update((o) => o(isActive: Value(null)));
      await db.managers.sources
          .filter((f) => f.id.equals(id))
          .update((o) => o(isActive: Value(true)));
    });
  }
}
