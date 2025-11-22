import 'package:drift/drift.dart';

import '../../sources/models.dart' as models;
import '../database.dart';

part 'library_dao.g.dart';

@DriftAccessor(include: {'../tables.drift'})
class LibraryDao extends DatabaseAccessor<SubtracksDatabase>
    with _$LibraryDaoMixin {
  LibraryDao(super.db);

  Future<List<models.Album>> listAlbums({
    required int limit,
    required int offset,
  }) {
    final query = albums.select()
      ..where(
        (f) => f.sourceId.equalsExp(
          subqueryExpression(db.sourcesDao.activeSourceId()),
        ),
      )
      ..limit(limit, offset: offset);

    return query.get();
  }

  Future<List<({models.Artist artist, int albumCount})>> listArtists({
    required int limit,
    required int offset,
  }) async {
    final albumCount = albums.id.count();

    final query =
        artists.select().join([
            leftOuterJoin(
              albums,
              albums.artistId.equalsExp(artists.id),
            ),
          ])
          ..addColumns([albumCount])
          ..where(
            artists.sourceId.equalsExp(
                  subqueryExpression(db.sourcesDao.activeSourceId()),
                ) &
                albums.sourceId.equalsExp(
                  subqueryExpression(db.sourcesDao.activeSourceId()),
                ),
          )
          ..groupBy([artists.sourceId, artists.id])
          ..orderBy([OrderingTerm.asc(artists.name)])
          ..limit(limit, offset: offset);

    return (await query.get())
        .map(
          (row) => (
            artist: row.readTable(artists),
            albumCount: row.read(albumCount) ?? 0,
          ),
        )
        .toList();
  }
}
