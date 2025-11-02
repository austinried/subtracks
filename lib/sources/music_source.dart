import 'models.dart';

abstract class MusicSource {
  Future<void> ping();

  Stream<SourceAlbum> allAlbums();
  Stream<SourceArtist> allArtists();
  Stream<SourcePlaylist> allPlaylists();
  Stream<SourceSong> allSongs();
  Stream<SourcePlaylistSong> allPlaylistSongs();

  Uri streamUri(String songId);
  Uri downloadUri(String songId);

  Uri coverArtUri(String coverArtId, {bool thumbnail = true});
}
