import 'models.dart';

abstract class MusicSource {
  Future<void> ping();

  Stream<Album> allAlbums();
  Stream<Artist> allArtists();
  Stream<Playlist> allPlaylists();
  Stream<Song> allSongs();
  Stream<PlaylistSong> allPlaylistSongs();

  Uri streamUri(String songId);
  Uri downloadUri(String songId);

  Uri coverArtUri(String coverArt, {bool thumbnail = false});
}
