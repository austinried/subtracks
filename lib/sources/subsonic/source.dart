import 'package:collection/collection.dart';
import 'package:pool/pool.dart';
import 'package:xml/xml.dart';
import 'package:xml/xml_events.dart';

import '../models.dart';
import '../music_source.dart';
import 'client.dart';
import 'mapping.dart';

class SubsonicSource implements MusicSource {
  SubsonicSource({
    required this.client,
    required this.maxBitrate,
    this.streamFormat,
  });

  final SubsonicClient client;
  final int maxBitrate;
  final String? streamFormat;

  final _pool = Pool(10, timeout: const Duration(seconds: 60));

  bool? _featureEmptyQuerySearch;
  Future<bool> get supportsFastSongSync async {
    if (_featureEmptyQuerySearch == null) {
      final res = await client.get(
        'search3',
        {'query': '""', 'songCount': '1'},
      );

      _featureEmptyQuerySearch = res.xml.findAllElements('song').isNotEmpty;
    }

    return _featureEmptyQuerySearch!;
  }

  @override
  Future<void> ping() async {
    await client.get('ping');
  }

  @override
  Stream<Iterable<SourceArtist>> allArtists() async* {
    final res = await client.get('getArtists');

    for (var artists in res.xml.findAllElements('artist').slices(200)) {
      yield artists.map(mapArtist);
    }
  }

  @override
  Stream<Iterable<SourceAlbum>> allAlbums() async* {
    final extras = await Future.wait([
      _albumList(
        'frequent',
      ).flatten().map((element) => element.getAttribute('id')!).toList(),
      _albumList(
        'recent',
      ).flatten().map((element) => element.getAttribute('id')!).toList(),
    ]);

    final frequentlyPlayed = {
      for (var i = 0; i < extras[0].length; i++) extras[0][i]: i,
    };
    final recentlyPlayed = {
      for (var i = 0; i < extras[1].length; i++) extras[1][i]: i,
    };

    await for (var albums in _albumList('newest')) {
      yield albums.map(
        (e) => mapAlbum(
          e,
          frequentRank: frequentlyPlayed[e.getAttribute('id')!],
          recentRank: recentlyPlayed[e.getAttribute('id')!],
        ),
      );
    }
  }

  @override
  Stream<Iterable<SourcePlaylist>> allPlaylists() async* {
    final res = await client.get('getPlaylists');

    for (var playlists in res.xml.findAllElements('playlist').slices(200)) {
      yield playlists.map(mapPlaylist);
    }
  }

  @override
  Stream<Iterable<SourcePlaylistSong>> allPlaylistSongs() async* {
    final allPlaylists = await client.get('getPlaylists');

    yield* _pool.forEach(allPlaylists.xml.findAllElements('playlist'), (
      playlist,
    ) async {
      final id = playlist.getAttribute('id')!;
      final res = await client.get('getPlaylist', {'id': id});

      return res.xml.findAllElements('entry').mapIndexed(mapPlaylistSong);
    });
  }

  @override
  Stream<Iterable<SourceSong>> allSongs() async* {
    if (await supportsFastSongSync) {
      await for (var songs in _songSearch()) {
        yield songs.map(mapSong);
      }
    } else {
      await for (var albumsList in _albumList('alphabeticalByName')) {
        yield* _pool.forEach(albumsList, (album) async {
          final albums = await client.get('getAlbum', {
            'id': album.getAttribute('id')!,
          });
          return albums.xml.findAllElements('song').map(mapSong);
        });
      }
    }
  }

  @override
  Uri streamUri(String songId) {
    return client.uri('stream', {
      'id': songId,
      'estimateContentLength': true.toString(),
      'maxBitRate': maxBitrate.toString(),
      'format': streamFormat?.toString(),
    });
  }

  @override
  Uri downloadUri(String songId) {
    return client.uri('download', {'id': songId});
  }

  @override
  Uri coverArtUri(String id, {bool thumbnail = true}) {
    final opts = {'id': id};
    if (thumbnail) {
      opts['size'] = 256.toString();
    }
    return client.uri('getCoverArt', opts);
  }

  @override
  Future<Uri?> artistArtUri(String artistId, {bool thumbnail = true}) async {
    final res = await client.get('getArtistInfo2', {'id': artistId});
    return Uri.tryParse(
      res.xml
              .getElement('artistInfo2')
              ?.getElement(thumbnail ? 'smallImageUrl' : 'largeImageUrl')
              ?.text ??
          '',
    );
  }

  Stream<Iterable<XmlElement>> _albumList(String type) async* {
    const size = 500;
    var offset = 0;

    while (true) {
      final res = await client.get('getAlbumList2', {
        'type': type,
        'size': size.toString(),
        'offset': offset.toString(),
      });

      final albums = res.xml.findAllElements('album');
      offset += albums.length;

      yield albums;

      if (albums.length < size) {
        break;
      }
    }
  }

  Stream<Iterable<XmlElement>> _songSearch() async* {
    const size = 500;
    var offset = 0;

    while (true) {
      final res = await client.get('search3', {
        'query': '""',
        'songCount': size.toString(),
        'songOffset': offset.toString(),
        'artistCount': '0',
        'albumCount': '0',
      });

      final songs = res.xml.findAllElements('song');
      offset += songs.length;

      yield songs;

      if (songs.length < size) {
        break;
      }
    }
  }
}
