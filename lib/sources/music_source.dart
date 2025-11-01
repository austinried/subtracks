import 'models.dart';

abstract class MusicSource {
  Future<void> ping();

  Stream<Iterable<SourceAlbum>> allAlbums();
  Stream<Iterable<SourceArtist>> allArtists();
  Stream<Iterable<SourcePlaylist>> allPlaylists();
  Stream<Iterable<SourceSong>> allSongs();
  Stream<Iterable<SourcePlaylistSong>> allPlaylistSongs();

  Uri streamUri(String songId);
  Uri downloadUri(String songId);

  Uri coverArtUri(String coverArtId, {bool thumbnail = true});
  Future<Uri?> artistArtUri(String artistId, {bool thumbnail = true});
}
