import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/query.dart';
import '../../l10n/generated/app_localizations.dart';
import '../state/database.dart';
import '../state/source.dart';
import '../ui/cover_art_theme.dart';
import '../ui/gradient.dart';
import '../ui/lists/header.dart';
import '../ui/lists/items.dart';
import '../ui/lists/songs_list.dart';

class PlaylistScreen extends HookConsumerWidget {
  const PlaylistScreen({
    super.key,
    required this.id,
  });

  final String id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);

    final db = ref.watch(databaseProvider);
    final sourceId = ref.watch(sourceIdProvider);

    final getPlaylist = useMemoized(
      () => db.libraryDao.getPlaylist(sourceId, id).getSingle(),
    );
    final playlist = useFuture(getPlaylist).data;

    if (playlist == null) {
      return Container();
    }

    final query = SongsQuery(
      sourceId: sourceId,
      filter: IList([SongsFilter.playlistId(playlist.id)]),
      sort: IList([
        SortingTerm.songsAsc(SongsColumn.playlistPosition),
      ]),
    );

    return CoverArtTheme(
      coverArt: playlist.coverArt,
      child: Scaffold(
        body: GradientScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: SongsListHeader(
                title: playlist.name,
                // subtitle: playlist.albumArtist,
                coverArt: playlist.coverArt,
                playText: l.resourcesPlaylistActionsPlay,
                onPlay: () {},
                onMore: () {},
              ),
            ),
            SongsList(
              query: query,
              itemBuilder: (context, item, index) => SongListTile(
                song: item.song,
                coverArt: item.albumCoverArt,
                showLeading: true,
                onTap: () {},
              ),
            ),
          ],
        ),
      ),
    );
  }
}
