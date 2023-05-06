import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../database/database.dart';
import '../state/settings.dart';

part 'sync_service.g.dart';

@Riverpod(keepAlive: true)
class SyncService extends _$SyncService {
  @override
  DateTime build() {
    return DateTime.now();
  }

  Future<void> syncAll() async {
    final db = ref.read(databaseProvider);

    await db.transaction(() async {
      await Future.wait([
        db.transaction(_syncAllArtists),
        db.transaction(_syncAllAlbums),
        db.transaction(_syncAllPlaylists),
        db.transaction(_syncAllSongs),
      ]);
    });

    state = DateTime.now();
  }

  Future<void> _syncAllArtists() async {
    final source = ref.read(musicSourceProvider);
    final db = ref.read(databaseProvider);

    final ids = <String>{};
    await for (var artists in source.allArtists()) {
      ids.addAll(artists.map((e) => e.id.value));
      await db.saveArtists(artists);
    }

    await db.deleteArtistsNotIn(source.id, ids);
  }

  Future<void> _syncAllAlbums() async {
    final source = ref.read(musicSourceProvider);
    final db = ref.read(databaseProvider);

    final ids = <String>{};
    await for (var albums in source.allAlbums()) {
      ids.addAll(albums.map((e) => e.id.value));
      await db.saveAlbums(albums);
    }

    await db.deleteAlbumsNotIn(source.id, ids);
  }

  Future<void> _syncAllPlaylists() async {
    final source = ref.read(musicSourceProvider);
    final db = ref.read(databaseProvider);

    final ids = <String>{};
    await for (var playlists in source.allPlaylists()) {
      ids.addAll(playlists.map((e) => e.playist.id.value));
      await db.savePlaylists(playlists);
    }

    await db.deletePlaylistsNotIn(source.id, ids);
  }

  Future<void> _syncAllSongs() async {
    final source = ref.read(musicSourceProvider);
    final db = ref.read(databaseProvider);

    final ids = <String>{};
    await for (var songs in source.allSongs()) {
      ids.addAll(songs.map((e) => e.id.value));
      await db.saveSongs(songs);
    }

    await db.deleteSongsNotIn(source.id, ids);
  }

  // Future<void> syncArtist(String id) async {
  //   final source = ref.read(musicSourceProvider);
  //   final db = ref.read(databaseProvider);

  //   final artist = await source.artist(id);
  //   await saveArtist(db, artist);
  // }

  // Future<void> syncAlbum(String id) async {
  //   final source = ref.read(musicSourceProvider);
  //   final db = ref.read(databaseProvider);

  //   final album = await source.album(id);
  //   await saveAlbum(db, album);
  // }

  // Future<void> syncPlaylist(String id) async {
  //   final source = ref.read(musicSourceProvider);
  //   final db = ref.read(databaseProvider);

  //   final playlist = await source.playlist(id);
  //   await savePlaylist(db, playlist);
  // }
}
