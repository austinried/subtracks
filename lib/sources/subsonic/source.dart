import 'package:collection/collection.dart';
import 'package:pool/pool.dart';
import 'package:xml/xml.dart';
import 'package:xml/xml_events.dart';

import '../models.dart';
import '../music_source.dart';
import 'client.dart';
import 'mapping.dart';

class SubsonicSource implements MusicSource {
  SubsonicSource(
    this.client, {
    this.maxConnections = 10,
    this.connectionPoolTimeout = const Duration(seconds: 60),
  });

  final SubsonicClient client;
  final int maxConnections;
  final Duration connectionPoolTimeout;

  late final _pool = Pool(maxConnections, timeout: connectionPoolTimeout);

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
  Stream<SourceArtist> allArtists() async* {
    final getArtistsRes = await _pool.withResource(
      () => client.get('getArtists'),
    );

    yield* _pool.forEach(getArtistsRes.xml.findAllElements('artist'), (
      artist,
    ) async {
      final res = await client.get('getArtistInfo2', {
        'id': artist.getAttribute('id')!,
      });

      return mapArtist(artist, res.xml.getElement('artistInfo2'));
    });
  }

  @override
  Stream<SourceAlbum> allAlbums() async* {
    final extras = await Future.wait([
      _albumList(
        'frequent',
      ).flatten().map((e) => e.getAttribute('id')!).toList(),
      _albumList(
        'recent',
      ).flatten().map((e) => e.getAttribute('id')!).toList(),
    ]);

    final frequentlyPlayed = {
      for (var i = 0; i < extras[0].length; i++) extras[0][i]: i,
    };
    final recentlyPlayed = {
      for (var i = 0; i < extras[1].length; i++) extras[1][i]: i,
    };

    await for (final albums in _albumList('newest')) {
      yield* Stream.fromIterable(
        albums.map(
          (album) => mapAlbum(
            album,
            frequentRank: frequentlyPlayed[album.getAttribute('id')!],
            recentRank: recentlyPlayed[album.getAttribute('id')!],
          ),
        ),
      );
    }
  }

  @override
  Stream<SourcePlaylist> allPlaylists() async* {
    final res = await _pool.withResource(() => client.get('getPlaylists'));

    yield* Stream.fromIterable(
      res.xml.findAllElements('playlist').map(mapPlaylist),
    );
  }

  @override
  Stream<SourcePlaylistSong> allPlaylistSongs() async* {
    final allPlaylists = await _pool.withResource(
      () => client.get('getPlaylists'),
    );

    yield* _pool
        .forEach(allPlaylists.xml.findAllElements('playlist'), (
          playlist,
        ) async {
          final id = playlist.getAttribute('id')!;
          final res = await client.get('getPlaylist', {'id': id});

          return res.xml.findAllElements('entry').mapIndexed(mapPlaylistSong);
        })
        .expand((a) => a);
  }

  @override
  Stream<SourceSong> allSongs() async* {
    if (await supportsFastSongSync) {
      await for (var songs in _songSearch()) {
        yield* Stream.fromIterable(songs.map(mapSong));
      }
    } else {
      await for (var albumsList in _albumList('alphabeticalByName')) {
        yield* _pool
            .forEach(albumsList, (album) async {
              final albums = await client.get('getAlbum', {
                'id': album.getAttribute('id')!,
              });
              return albums.xml.findAllElements('song').map(mapSong);
            })
            .expand((a) => a);
      }
    }
  }

  @override
  Uri streamUri(String songId, {int? maxBitrate, String? format}) {
    return client.uri('stream', {
      'id': songId,
      'estimateContentLength': true.toString(),
      'maxBitRate': maxBitrate.toString(),
      'format': format.toString(),
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

  Stream<Iterable<XmlElement>> _albumList(String type) async* {
    const size = 500;
    var offset = 0;

    while (true) {
      final res = await _pool.withResource(
        () => client.get('getAlbumList2', {
          'type': type,
          'size': size.toString(),
          'offset': offset.toString(),
        }),
      );

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
      final res = await _pool.withResource(
        () => client.get('search3', {
          'query': '""',
          'songCount': size.toString(),
          'songOffset': offset.toString(),
          'artistCount': '0',
          'albumCount': '0',
        }),
      );

      final songs = res.xml.findAllElements('song');
      offset += songs.length;

      yield songs;

      if (songs.length < size) {
        break;
      }
    }
  }
}
