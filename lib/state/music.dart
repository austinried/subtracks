import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../database/database.dart';
import '../models/music.dart';
import '../models/query.dart';
import '../models/support.dart';
import 'settings.dart';

part 'music.g.dart';

@riverpod
Stream<Artist> artist(ArtistRef ref, String id) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.artistById(sourceId, id).watchSingle();
}

@riverpod
Stream<Album> album(AlbumRef ref, String id) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.albumById(sourceId, id).watchSingle();
}

@riverpod
Stream<ListDownloadStatus> albumDownloadStatus(
  AlbumDownloadStatusRef ref,
  String id,
) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.albumDownloadStatus(sourceId, id).watchSingle();
}

@riverpod
Stream<ListDownloadStatus> playlistDownloadStatus(
  PlaylistDownloadStatusRef ref,
  String id,
) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.playlistDownloadStatus(sourceId, id).watchSingle();
}

@riverpod
Stream<Song> song(SongRef ref, String id) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.songById(sourceId, id).watchSingle();
}

@riverpod
Future<List<Song>> albumSongsList(
  AlbumSongsListRef ref,
  String id,
  ListQuery opt,
) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.albumSongsList(SourceId(sourceId: sourceId, id: id), opt).get();
}

@riverpod
Future<List<Song>> songsByAlbumList(
  SongsByAlbumListRef ref,
  ListQuery opt,
) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.songsByAlbumList(sourceId, opt).get();
}

@riverpod
Stream<Playlist> playlist(PlaylistRef ref, String id) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.playlistById(sourceId, id).watchSingle();
}

@riverpod
Future<List<Song>> playlistSongsList(
  PlaylistSongsListRef ref,
  String id,
  ListQuery opt,
) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.playlistSongsList(SourceId(sourceId: sourceId, id: id), opt).get();
}

@riverpod
Future<List<Album>> albumsInIds(AlbumsInIdsRef ref, IList<String> ids) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.albumsInIds(sourceId, ids.toList()).get();
}

@riverpod
Stream<IList<Album>> albumsByArtistId(AlbumsByArtistIdRef ref, String id) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db
      .albumsByArtistId(sourceId, id)
      .watch()
      .map((event) => event.toIList());
}

@riverpod
Stream<IList<String>> albumGenres(AlbumGenresRef ref, Pagination page) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db
      .albumGenres(sourceId, page.limit, page.offset)
      .watch()
      .map((event) => event.withNullsRemoved().toIList());
}

@riverpod
Stream<IList<Album>> albumsByGenre(
  AlbumsByGenreRef ref,
  String genre,
  Pagination page,
) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db
      .albumsByGenre(sourceId, genre, page.limit, page.offset)
      .watch()
      .map((event) => event.toIList());
}

@riverpod
Stream<int> songsByGenreCount(SongsByGenreCountRef ref, String genre) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db.songsByGenreCount(sourceId, genre).watchSingle();
}

@riverpod
Stream<IList<Song>> songsList(SongsListRef ref, ListQuery opt) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);

  return db
      .songsList(sourceId, opt)
      .watch()
      .map((event) => event.withNullsRemoved().toIList());
}
