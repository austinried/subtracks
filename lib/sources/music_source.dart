import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:rxdart/rxdart.dart';

import '../database/database.dart';
import '../log.dart';
import '../state/settings.dart';

abstract class BaseMusicSource {
  int get id;

  Future<void> ping();

  Stream<Iterable<AlbumsCompanion>> allAlbums();
  Stream<Iterable<ArtistsCompanion>> allArtists();
  Stream<Iterable<PlaylistWithSongsCompanion>> allPlaylists();
  Stream<Iterable<SongsCompanion>> allSongs();

  Uri streamUri(String songId);
  Uri downloadUri(String songId);

  Uri coverArtUri(String coverArtId, {bool thumbnail = true});
  Future<Uri?> artistArtUri(String artistId, {bool thumbnail = true});
}

class OfflineException implements Exception {
  @override
  String toString() => 'OfflineException';
}

class MusicSource implements BaseMusicSource {
  final BaseMusicSource _source;
  final Ref _ref;

  const MusicSource(this._source, this._ref);

  void _testOnline() {
    if (_ref.read(offlineModeProvider)) {
      throw OfflineException();
    }
  }

  @override
  Stream<Iterable<AlbumsCompanion>> allAlbums() {
    _testOnline();
    return _source
        .allAlbums()
        .doOnError((e, st) => log.severe('allAlbums', e, st));
  }

  @override
  Stream<Iterable<ArtistsCompanion>> allArtists() {
    _testOnline();
    return _source
        .allArtists()
        .doOnError((e, st) => log.severe('allArtists', e, st));
  }

  @override
  Stream<Iterable<PlaylistWithSongsCompanion>> allPlaylists() {
    _testOnline();
    return _source
        .allPlaylists()
        .doOnError((e, st) => log.severe('allPlaylists', e, st));
  }

  @override
  Stream<Iterable<SongsCompanion>> allSongs() {
    _testOnline();
    return _source
        .allSongs()
        .doOnError((e, st) => log.severe('allSongs', e, st));
  }

  @override
  Future<Uri?> artistArtUri(String artistId, {bool thumbnail = true}) {
    _testOnline();
    return _source.artistArtUri(artistId, thumbnail: thumbnail);
  }

  @override
  Uri coverArtUri(String coverArtId, {bool thumbnail = true}) =>
      _source.coverArtUri(coverArtId, thumbnail: thumbnail);

  @override
  Uri downloadUri(String songId) => _source.downloadUri(songId);

  @override
  int get id => _source.id;

  @override
  Future<void> ping() => _source.ping();

  @override
  Uri streamUri(String songId) => _source.streamUri(songId);
}
