import 'package:xml/xml.dart';

import '../models.dart';

Artist mapArtist(XmlElement e, XmlElement? info) => Artist(
  id: e.getAttribute('id')!,
  name: e.getAttribute('name')!,
  starred: DateTime.tryParse(e.getAttribute('starred').toString()),
  smallImage: Uri.tryParse(info?.getElement('smallImageUrl')?.innerText ?? ''),
  largeImage: Uri.tryParse(info?.getElement('largeImageUrl')?.innerText ?? ''),
);

Album mapAlbum(
  XmlElement e, {
  int? frequentRank,
  int? recentRank,
}) => Album(
  id: e.getAttribute('id')!,
  artistId: e.getAttribute('artistId'),
  name: e.getAttribute('name')!,
  albumArtist: e.getAttribute('artist'),
  created: DateTime.parse(e.getAttribute('created')!),
  coverArt: e.getAttribute('coverArt'),
  year: int.tryParse(e.getAttribute('year').toString()),
  starred: DateTime.tryParse(e.getAttribute('starred').toString()),
  genre: e.getAttribute('genre'),
  frequentRank: frequentRank,
  recentRank: recentRank,
);

Playlist mapPlaylist(XmlElement e) => Playlist(
  id: e.getAttribute('id')!,
  name: e.getAttribute('name')!,
  comment: e.getAttribute('comment'),
  coverArt: e.getAttribute('coverArt'),
  created: DateTime.parse(e.getAttribute('created')!),
  changed: DateTime.parse(e.getAttribute('changed')!),
  owner: e.getAttribute('owner'),
);

Song mapSong(XmlElement e) => Song(
  id: e.getAttribute('id')!,
  albumId: e.getAttribute('albumId'),
  artistId: e.getAttribute('artistId'),
  title: e.getAttribute('title')!,
  album: e.getAttribute('album'),
  artist: e.getAttribute('artist'),
  duration: e.getAttribute('duration') != null
      ? Duration(
          seconds: int.parse(e.getAttribute('duration').toString()),
        )
      : null,
  track: int.tryParse(e.getAttribute('track').toString()),
  disc: int.tryParse(e.getAttribute('discNumber').toString()),
  starred: DateTime.tryParse(e.getAttribute('starred').toString()),
  genre: e.getAttribute('genre'),
);

PlaylistSong mapPlaylistSong(
  int index,
  XmlElement e,
) => PlaylistSong(
  playlistId: e.parentElement!.getAttribute('id')!,
  songId: e.getAttribute('id')!,
  position: index,
);
