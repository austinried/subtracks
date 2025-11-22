import 'package:drift/drift.dart';

import '../database.dart';

part 'sources_dao.g.dart';

@DriftAccessor(include: {'../tables.drift'})
class SourcesDao extends DatabaseAccessor<SubtracksDatabase>
    with _$SourcesDaoMixin {
  SourcesDao(super.db);

  JoinedSelectStatement<Sources, Source> activeSourceId() {
    return selectOnly(sources)
      ..addColumns([sources.id])
      ..where(sources.isActive.equals(true));
  }

  Stream<List<(Source, SubsonicSetting)>> listSources() {
    final query = select(sources).join([
      innerJoin(
        subsonicSettings,
        sources.id.equalsExp(subsonicSettings.sourceId),
      ),
    ]);

    return query.watch().map(
      (rows) => rows
          .map(
            (row) => (
              row.readTable(sources),
              row.readTable(subsonicSettings),
            ),
          )
          .toList(),
    );
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
