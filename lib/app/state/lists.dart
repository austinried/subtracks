import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/query.dart';
import 'source.dart';

final albumsQueryProvider = Provider<AlbumsQuery>((ref) {
  final sourceId = ref.watch(sourceIdProvider);

  return AlbumsQuery(
    sourceId: sourceId,
    sort: IList([
      SortingTerm.albumsDesc(AlbumsColumn.created),
    ]),
  );
});

final songsQueryProvider = Provider<SongsQuery>((ref) {
  final sourceId = ref.watch(sourceIdProvider);

  return SongsQuery(
    sourceId: sourceId,
    sort: IList([
      SortingTerm.songsAsc(SongsColumn.albumArtist),
      SortingTerm.songsAsc(SongsColumn.album),
      SortingTerm.songsAsc(SongsColumn.disc),
      SortingTerm.songsAsc(SongsColumn.track),
      SortingTerm.songsAsc(SongsColumn.title),
    ]),
  );
});
