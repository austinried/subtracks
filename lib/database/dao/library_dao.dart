import 'package:drift/drift.dart';

import '../../sources/models.dart' as models;
import '../database.dart';
import '../query.dart';

part 'library_dao.g.dart';

typedef AristListItem = ({
  models.Artist artist,
  int albumCount,
});

typedef SongListItem = ({
  models.Song song,
  String? albumCoverArt,
});

extension on SortDirection {
  OrderingMode toMode() => switch (this) {
    SortDirection.asc => OrderingMode.asc,
    SortDirection.desc => OrderingMode.desc,
  };
}

@DriftAccessor(include: {'../tables.drift'})
class LibraryDao extends DatabaseAccessor<SubtracksDatabase>
    with _$LibraryDaoMixin {
  LibraryDao(super.db);

  Future<List<models.Album>> listAlbums(AlbumsQuery q) {
    final query = albums.select()
      ..where((albums) {
        var filter = albums.sourceId.equals(q.sourceId);
        for (final queryFilter in q.filter) {
          filter &= switch (queryFilter) {
            AlbumsFilterArtistId(:final artistId) => albums.artistId.equals(
              artistId,
            ),
            AlbumsFilterYearEquals(:final year) => albums.year.equals(year),
            _ => CustomExpression(''),
          };
        }

        return filter;
      })
      ..orderBy(
        q.sort
            .map(
              (sort) =>
                  (albums) => OrderingTerm(
                    expression: switch (sort.by) {
                      AlbumsColumn.name => albums.name,
                      AlbumsColumn.created => albums.created,
                      AlbumsColumn.year => albums.year,
                      AlbumsColumn.starred => albums.starred,
                    },
                    mode: sort.dir.toMode(),
                  ),
            )
            .toList(),
      );

    _limitQuery(query: query, limit: q.limit, offset: q.offset);

    return query.get();
  }

  Future<List<AristListItem>> listArtists(ArtistsQuery q) {
    final albumCount = albums.id.count();

    var filter =
        artists.sourceId.equals(q.sourceId) &
        albums.sourceId.equals(q.sourceId);

    for (final queryFilter in q.filter) {
      filter &= switch (queryFilter) {
        ArtistsFilterStarred(:final starred) =>
          starred ? artists.starred.isNotNull() : artists.starred.isNull(),
        ArtistsFilterNameSearch() => CustomExpression(''),
        _ => CustomExpression(''),
      };
    }

    final query =
        artists.select().join([
            leftOuterJoin(
              albums,
              albums.artistId.equalsExp(artists.id),
            ),
          ])
          ..addColumns([albumCount])
          ..where(filter)
          ..groupBy([artists.sourceId, artists.id])
          ..orderBy(
            q.sort
                .map(
                  (sort) => OrderingTerm(
                    expression: switch (sort.by) {
                      ArtistsColumn.name => artists.name,
                      ArtistsColumn.starred => artists.starred,
                      ArtistsColumn.albumCount => albumCount,
                    },
                    mode: sort.dir.toMode(),
                  ),
                )
                .toList(),
          );

    _limitQuery(query: query, limit: q.limit, offset: q.offset);

    return query
        .map(
          (row) => (
            artist: row.readTable(artists),
            albumCount: row.read(albumCount) ?? 0,
          ),
        )
        .get();
  }

  Future<List<SongListItem>> listSongs(SongsQuery q) {
    var joinPlaylistSongs = false;
    var filter = songs.sourceId.equals(q.sourceId);

    for (final queryFilter in q.filter) {
      switch (queryFilter) {
        case SongsFilterAlbumId(:final albumId):
          filter &= songs.albumId.equals(albumId);
        case SongsFilterPlaylistId(:final playlistId):
          joinPlaylistSongs = true;
          filter &= playlistSongs.playlistId.equals(playlistId);
      }
    }

    final query =
        songs.select().join([
            leftOuterJoin(
              albums,
              albums.id.equalsExp(songs.albumId) &
                  albums.sourceId.equals(q.sourceId),
            ),
            if (joinPlaylistSongs)
              leftOuterJoin(
                playlistSongs,
                playlistSongs.sourceId.equals(q.sourceId) &
                    playlistSongs.songId.equalsExp(songs.id),
              ),
          ])
          ..addColumns([
            albums.coverArt,
          ])
          ..where(filter)
          ..orderBy(
            q.sort
                .map(
                  (sort) => OrderingTerm(
                    expression: switch (sort.by) {
                      SongsColumn.title => songs.title,
                      SongsColumn.starred => songs.starred,
                      SongsColumn.disc => songs.disc,
                      SongsColumn.track => songs.track,
                      SongsColumn.album => songs.album,
                      SongsColumn.artist => songs.artist,
                      SongsColumn.albumArtist => albums.albumArtist,
                      SongsColumn.playlistPosition => playlistSongs.position,
                    },
                    mode: sort.dir.toMode(),
                  ),
                )
                .toList(),
          );

    _limitQuery(query: query, limit: q.limit, offset: q.offset);

    return query
        .map(
          (row) => (
            song: row.readTable(songs),
            albumCoverArt: row.read(albums.coverArt),
          ),
        )
        .get();
  }

  Future<List<models.Playlist>> listPlaylists(PlaylistsQuery q) {
    final query = playlists.select()
      ..where((playlists) {
        var filter = playlists.sourceId.equals(q.sourceId);
        for (final queryFilter in q.filter) {
          filter &= switch (queryFilter) {
            PlaylistsFilterPublic(:final public) => playlists.public.equals(
              public,
            ),
            PlaylistsFilterNameSearch() => CustomExpression(''),
            _ => CustomExpression(''),
          };
        }

        return filter;
      })
      ..orderBy(
        q.sort
            .map(
              (sort) =>
                  (albums) => OrderingTerm(
                    expression: switch (sort.by) {
                      PlaylistsColumn.name => playlists.name,
                      PlaylistsColumn.created => playlists.created,
                    },
                    mode: sort.dir.toMode(),
                  ),
            )
            .toList(),
      );

    _limitQuery(query: query, limit: q.limit, offset: q.offset);

    return query.get();
  }

  Selectable<models.Album> getAlbum(int sourceId, String id) {
    return db.managers.albums.filter(
      (f) => f.sourceId.equals(sourceId) & f.id.equals(id),
    );
  }

  Selectable<models.Artist> getArtist(int sourceId, String id) {
    return db.managers.artists.filter(
      (f) => f.sourceId.equals(sourceId) & f.id.equals(id),
    );
  }

  Selectable<models.Playlist> getPlaylist(int sourceId, String id) {
    return db.managers.playlists.filter(
      (f) => f.sourceId.equals(sourceId) & f.id.equals(id),
    );
  }

  void _limitQuery({
    required LimitContainerMixin query,
    required int? limit,
    required int? offset,
  }) {
    if (limit != null) {
      query.limit(limit, offset: offset);
    }
  }
}
