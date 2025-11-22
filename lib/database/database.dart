import 'package:drift/drift.dart';
import 'package:drift_flutter/drift_flutter.dart';
import 'package:path/path.dart' as path;
import 'package:path_provider/path_provider.dart';

import '../sources/models.dart' as models;
import 'converters.dart';
import 'dao/library_dao.dart';
import 'dao/sources_dao.dart';

part 'database.g.dart';

// don't exceed SQLITE_MAX_VARIABLE_NUMBER (32766 for version >= 3.32.0)
// https://www.sqlite.org/limits.html
const kSqliteMaxVariableNumber = 32766;

@DriftDatabase(
  include: {'tables.drift'},
  daos: [
    SourcesDao,
    LibraryDao,
  ],
)
class SubtracksDatabase extends _$SubtracksDatabase {
  SubtracksDatabase([QueryExecutor? executor])
    : super(executor ?? _openConnection());

  static QueryExecutor _openConnection() {
    return driftDatabase(
      name: 'my_database',
      native: DriftNativeOptions(
        databasePath: () async {
          final directory = await getApplicationSupportDirectory();
          return path.join(directory.absolute.path, 'subtracks.sqlite');
        },
      ),
    );
  }

  @override
  int get schemaVersion => 1;

  @override
  MigrationStrategy get migration {
    return MigrationStrategy(
      beforeOpen: (details) async {
        await customStatement('PRAGMA foreign_keys = ON');
      },
    );
  }
}

extension ArtistToDb on models.Artist {
  ArtistsCompanion toDb(int sourceId) => ArtistsCompanion.insert(
    sourceId: sourceId,
    id: id,
    name: name,
    starred: Value(starred),
    coverArt: Value(coverArt),
  );
}

extension AlbumToDb on models.Album {
  AlbumsCompanion toDb(int sourceId) => AlbumsCompanion.insert(
    sourceId: sourceId,
    id: id,
    artistId: Value(artistId),
    name: name,
    albumArtist: Value(albumArtist),
    created: created,
    coverArt: Value(coverArt),
    genre: Value(genre),
    year: Value(year),
    starred: Value(starred),
    frequentRank: Value(frequentRank),
    recentRank: Value(recentRank),
  );
}

extension SongToDb on models.Song {
  SongsCompanion toDb(int sourceId) => SongsCompanion.insert(
    sourceId: sourceId,
    id: id,
    albumId: Value(albumId),
    artistId: Value(artistId),
    title: title,
    album: Value(album),
    artist: Value(artist),
    duration: Value(duration),
    track: Value(track),
    disc: Value(disc),
    starred: Value(starred),
    genre: Value(genre),
  );
}

extension PlaylistToDb on models.Playlist {
  PlaylistsCompanion toDb(int sourceId) => PlaylistsCompanion.insert(
    sourceId: sourceId,
    id: id,
    name: name,
    comment: Value(comment),
    coverArt: Value(coverArt),
    created: created,
    changed: changed,
  );
}

extension PlaylistSongToDb on models.PlaylistSong {
  PlaylistSongsCompanion toDb(int sourceId) => PlaylistSongsCompanion.insert(
    sourceId: sourceId,
    playlistId: playlistId,
    songId: songId,
    position: position,
  );
}
