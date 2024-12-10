import 'package:collection/collection.dart';
import 'package:drift/drift.dart' show Value;
import 'package:http/http.dart';
import 'package:pool/pool.dart';
import 'package:xml/xml.dart';
import 'package:xml/xml_events.dart';

import '../../database/database.dart';
import '../../database/util.dart';
import '../../models/settings.dart';
import '../music_source.dart';
import 'client.dart';

class SubsonicSource implements MusicSource {
  final SubsonicSettings opt;
  final BaseClient http;
  final int maxBitrate;
  final String? streamFormat;

  late final SubsonicClient client;

  final _pool = Pool(10, timeout: const Duration(seconds: 60));

  SubsonicSource({
    required this.opt,
    required this.http,
    required this.maxBitrate,
    this.streamFormat,
  }) {
    client = SubsonicClient(opt, http);
  }

  @override
  int get id => opt.id;

  @override
  Future<void> ping() async {
    await client.get('ping');
  }

  @override
  Stream<Iterable<ArtistsCompanion>> allArtists() async* {
    final res = await client.get('getArtists');
    for (var artists in res.xml.findAllElements('artist').slices(200)) {
      yield artists.map(_mapArtist);
    }
  }

  @override
  Stream<Iterable<AlbumsCompanion>> allAlbums() async* {
    final extras = await Future.wait([
      _albumList('frequent')
          .flatten()
          .map((element) => element.getAttribute('id')!)
          .toList(),
      _albumList('recent')
          .flatten()
          .map((element) => element.getAttribute('id')!)
          .toList(),
    ]);

    final frequentlyPlayed = {
      for (var i = 0; i < extras[0].length; i++) extras[0][i]: i
    };
    final recentlyPlayed = {
      for (var i = 0; i < extras[1].length; i++) extras[1][i]: i
    };

    await for (var albums in _albumList('newest')) {
      yield albums.map(
        (e) => _mapAlbum(
          e,
          frequentRank: Value(frequentlyPlayed[e.getAttribute('id')!]),
          recentRank: Value(recentlyPlayed[e.getAttribute('id')!]),
        ),
      );
    }
  }

  @override
  Stream<Iterable<PlaylistWithSongsCompanion>> allPlaylists() async* {
    final allPlaylists = await client.get('getPlaylists');

    yield* _pool.forEach(allPlaylists.xml.findAllElements('playlist'),
        (playlist) async {
      final res = await client.get(
        'getPlaylist',
        {'id': playlist.getAttribute('id')},
      );
      return [
        PlaylistWithSongsCompanion(
          _mapPlaylist(res.xml.getElement('playlist')!),
          res.xml.findAllElements('entry').mapIndexed(_mapPlaylistSong),
        )
      ];
    });
  }

  @override
  Stream<Iterable<SongsCompanion>> allSongs() async* {
    if (opt.features.contains(SubsonicFeature.emptyQuerySearch)) {
      await for (var songs in _songSearch()) {
        yield songs.map(_mapSong);
      }
    } else {
      await for (var albumsList in _albumList('alphabeticalByName')) {
        yield* _pool.forEach(albumsList, (album) async {
          final albums = await client.get('getAlbum', {
            'id': album.getAttribute('id')!,
          });
          return albums.xml.findAllElements('song').map(_mapSong);
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
    return Uri.tryParse(res.xml
            .getElement('artistInfo2')
            ?.getElement(thumbnail ? 'smallImageUrl' : 'largeImageUrl')
            ?.value ??
        '');
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

  ArtistsCompanion _mapArtist(XmlElement e) {
    return ArtistsCompanion.insert(
      sourceId: id,
      id: 'artist.${e.getAttribute('id')!}',
      name: e.getAttribute('name') ?? 'Artist ${e.getAttribute('id')}',
      albumCount: int.parse(e.getAttribute('albumCount')!),
      starred: Value(DateTimeExt.tryParseUtc(e.getAttribute('starred'))),
    );
  }

  AlbumsCompanion _mapAlbum(
    XmlElement e, {
    Value<int?> frequentRank = const Value.absent(),
    Value<int?> recentRank = const Value.absent(),
  }) {
    return AlbumsCompanion.insert(
      sourceId: id,
      id: 'album.${e.getAttribute('id')!}',
      artistId: Value(e.getAttribute('artistId') != null
          ? 'artist.${e.getAttribute('artistId')}'
          : null),
      name: e.getAttribute('name') ?? 'Album ${e.getAttribute('id')}',
      albumArtist: Value(e.getAttribute('artist')),
      created: DateTimeExt.parseUtc(e.getAttribute('created')!),
      coverArt: Value(e.getAttribute('coverArt') != null
          ? 'coverArt.${e.getAttribute('coverArt')}'
          : null),
      year: e.getAttribute('year') != null
          ? Value(int.parse(e.getAttribute('year')!))
          : const Value(null),
      starred: Value(DateTimeExt.tryParseUtc(e.getAttribute('starred'))),
      genre: Value(e.getAttribute('genre')),
      songCount: int.parse(e.getAttribute('songCount')!),
      frequentRank: frequentRank,
      recentRank: recentRank,
    );
  }

  PlaylistsCompanion _mapPlaylist(XmlElement e) {
    return PlaylistsCompanion.insert(
      sourceId: id,
      id: 'playlist.${e.getAttribute('id')!}',
      name: e.getAttribute('name') ?? 'Playlist ${e.getAttribute('id')}',
      comment: Value(e.getAttribute('comment')),
      coverArt: Value(e.getAttribute('coverArt') != null
          ? 'coverArt.${e.getAttribute('coverArt')}'
          : null),
      songCount: int.parse(e.getAttribute('songCount')!),
      created: DateTimeExt.parseUtc(e.getAttribute('created')!),
    );
  }

  SongsCompanion _mapSong(XmlElement e) {
    return SongsCompanion.insert(
      sourceId: id,
      id: 'song.${e.getAttribute('id')!}',
      albumId: Value(e.getAttribute('albumId') != null
          ? 'album.${e.getAttribute('albumId')}'
          : null),
      artistId: Value(e.getAttribute('artistId') != null
          ? 'artist.${e.getAttribute('artistId')}'
          : null),
      title: e.getAttribute('title') ?? 'Song ${e.getAttribute('id')}',
      album: Value(e.getAttribute('album')),
      artist: Value(e.getAttribute('artist')),
      duration: e.getAttribute('duration') != null
          ? Value(Duration(
              seconds: int.parse(e.getAttribute('duration').toString())))
          : const Value(null),
      track: e.getAttribute('track') != null
          ? Value(int.parse(e.getAttribute('track')!))
          : const Value(null),
      disc: e.getAttribute('discNumber') != null
          ? Value(int.parse(e.getAttribute('discNumber')!))
          : const Value(null),
      starred: Value(DateTimeExt.tryParseUtc(e.getAttribute('starred'))),
      genre: Value(e.getAttribute('genre')),
    );
  }

  PlaylistSongsCompanion _mapPlaylistSong(int index, XmlElement e) {
    return PlaylistSongsCompanion.insert(
      sourceId: id,
      playlistId: 'playlist.${e.parentElement!.getAttribute('id')!}',
      songId: 'song.${e.getAttribute('id')!}',
      position: index,
    );
  }
}
