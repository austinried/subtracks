import 'dart:async';

import 'package:auto_route/auto_route.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:sliver_tools/sliver_tools.dart';

import '../../models/music.dart';
import '../../models/query.dart';
import '../../models/support.dart';
import '../../services/audio_service.dart';
import '../../services/cache_service.dart';
import '../../state/music.dart';
import '../../state/settings.dart';
import '../../state/theme.dart';
import '../buttons.dart';
import '../context_menus.dart';
import '../gradient.dart';
import '../hooks/use_download_actions.dart';
import '../hooks/use_list_query_paging_controller.dart';
import '../images.dart';
import '../items.dart';
import '../lists.dart';

class AlbumSongsPage extends HookConsumerWidget {
  final String id;

  const AlbumSongsPage({
    super.key,
    @pathParam required this.id,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final album = ref.watch(albumProvider(id)).valueOrNull;
    final audio = ref.watch(audioControlProvider);
    final colors = ref.watch(albumArtThemeProvider(id)).valueOrNull;
    final key = useState(GlobalKey());

    if (album == null) {
      return Container();
    }

    final query = useMemoized(() => const ListQuery(
          page: Pagination(limit: 30),
          sort: SortBy(column: 'disc, track'),
        ));

    final getSongs = useCallback(
      (ListQuery query) => ref.read(albumSongsListProvider(id, query).future),
      [id],
    );

    final play = useCallback(
      ({int? index, bool? shuffle}) => audio.playSongs(
        query: query,
        getSongs: getSongs,
        startIndex: index,
        context: QueueContextType.album,
        contextId: id,
        shuffle: shuffle,
      ),
      [id, query, getSongs],
    );

    return QueueContext(
      id: id,
      type: QueueContextType.album,
      child: _SongsPage(
        query: query,
        getSongs: getSongs,
        fab: ShuffleFab(onPressed: () => play(shuffle: true)),
        onSongTap: (song, index) => play(index: index),
        background: AlbumArtGradient(key: key.value, id: id),
        colors: colors,
        header: _AlbumHeader(
          album: album,
          play: () => play(shuffle: false),
        ),
      ),
    );
  }
}

class _AlbumHeader extends HookConsumerWidget {
  final Album album;
  final void Function() play;

  const _AlbumHeader({
    required this.album,
    required this.play,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cache = ref.watch(cacheServiceProvider);

    final downloadActions = useAlbumDownloadActions(
      context: context,
      ref: ref,
      album: album,
    );

    final l = AppLocalizations.of(context);

    return _Header(
      title: album.name,
      subtitle: album.albumArtist,
      imageCache: cache.albumArt(album, thumbnail: false),
      playText: l.resourcesAlbumActionsPlay,
      onPlay: play,
      onMore: () => showContextMenu(
        context: context,
        ref: ref,
        builder: (context) => BottomSheetMenu(
          child: AlbumContextMenu(album: album),
        ),
      ),
      downloadActions: downloadActions,
    );
  }
}

class PlaylistSongsPage extends HookConsumerWidget {
  final String id;

  const PlaylistSongsPage({
    super.key,
    @pathParam required this.id,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final playlist = ref.watch(playlistProvider(id)).valueOrNull;
    final audio = ref.watch(audioControlProvider);
    final colors = ref.watch(playlistArtThemeProvider(id)).valueOrNull;

    if (playlist == null) {
      return Container();
    }

    final query = useMemoized(() => const ListQuery(
          page: Pagination(limit: 30),
          sort: SortBy(column: 'playlist_songs.position'),
        ));

    final getSongs = useCallback(
      (ListQuery query) =>
          ref.read(playlistSongsListProvider(id, query).future),
      [id],
    );

    final play = useCallback(
      ({int? index, bool? shuffle}) => audio.playSongs(
        query: query,
        getSongs: getSongs,
        startIndex: index,
        context: QueueContextType.playlist,
        contextId: id,
        shuffle: shuffle,
      ),
      [id, query, getSongs],
    );

    return QueueContext(
      id: id,
      type: QueueContextType.playlist,
      child: _SongsPage(
        query: query,
        getSongs: getSongs,
        fab: ShuffleFab(onPressed: () => play(shuffle: true)),
        onSongTap: (song, index) => play(index: index),
        songImage: true,
        background: PlaylistArtGradient(id: id),
        colors: colors,
        header: _PlaylistHeader(
          playlist: playlist,
          play: () => play(shuffle: false),
        ),
      ),
    );
  }
}

class _PlaylistHeader extends HookConsumerWidget {
  final Playlist playlist;
  final void Function() play;

  const _PlaylistHeader({
    required this.playlist,
    required this.play,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cache = ref.watch(cacheServiceProvider);

    final downloadActions = usePlaylistDownloadActions(
      context: context,
      ref: ref,
      playlist: playlist,
    );

    final l = AppLocalizations.of(context);

    return _Header(
      title: playlist.name,
      subtitle: playlist.comment,
      imageCache: cache.playlistArt(playlist, thumbnail: false),
      playText: l.resourcesPlaylistActionsPlay,
      onPlay: play,
      onMore: () {
        showContextMenu(
          context: context,
          ref: ref,
          builder: (context) => BottomSheetMenu(
            size: MenuSize.small,
            child: PlaylistContextMenu(playlist: playlist),
          ),
        );
      },
      downloadActions: downloadActions,
    );
  }
}

class GenreSongsPage extends HookConsumerWidget {
  final String genre;

  const GenreSongsPage({
    super.key,
    @pathParam required this.genre,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = useMemoized(
      () => ListQuery(
        page: const Pagination(limit: 30),
        sort: const SortBy(
          column: 'albums.created DESC, albums.name, songs.disc, songs.track',
        ),
        filters: IList(
          [FilterWith.equals(column: 'songs.genre', value: genre)],
        ),
      ),
      [genre],
    );

    final getSongs = useCallback(
      (ListQuery query) => ref.read(songsByAlbumListProvider(query).future),
      [],
    );

    final play = useCallback(
      ({int? index, bool? shuffle}) => ref.read(audioControlProvider).playRadio(
            context: QueueContextType.genre,
            contextId: genre,
            query: query,
            getSongs: getSongs,
          ),
      [query, getSongs],
    );

    return QueueContext(
      id: genre,
      type: QueueContextType.album,
      child: _SongsPage(
        query: query,
        getSongs: getSongs,
        // onSongTap: (song, index) => play(index: index),
        songImage: true,
        background: const BackgroundGradient(),
        fab: RadioPlayFab(
          onPressed: () => play(),
        ),
        header: _GenreHeader(genre: genre),
      ),
    );
  }
}

class _GenreHeader extends HookConsumerWidget {
  final String genre;

  const _GenreHeader({
    required this.genre,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final count = ref.watch(songsByGenreCountProvider(genre)).valueOrNull ?? 0;

    final l = AppLocalizations.of(context);

    return _Header(
      title: genre,
      subtitle: l.resourcesSongCount(count),
      downloadActions: const [],
    );
  }
}

class QueueContext extends InheritedWidget {
  final QueueContextType type;
  final String? id;

  const QueueContext({
    super.key,
    required this.type,
    this.id,
    required super.child,
  });

  static QueueContext? maybeOf(BuildContext context) {
    return context.dependOnInheritedWidgetOfExactType<QueueContext>();
  }

  static QueueContext of(BuildContext context) {
    final QueueContext? result = maybeOf(context);
    assert(result != null, 'No QueueContext found in context');
    return result!;
  }

  @override
  bool updateShouldNotify(covariant QueueContext oldWidget) =>
      oldWidget.id != id || oldWidget.type != type;
}

class _SongsPage extends HookConsumerWidget {
  final ListQuery query;
  final FutureOr<List<Song>> Function(ListQuery query) getSongs;
  final void Function(Song song, int index)? onSongTap;
  final bool songImage;
  final Widget background;
  final Widget fab;
  final ColorTheme? colors;
  final Widget header;

  const _SongsPage({
    required this.query,
    required this.getSongs,
    this.onSongTap,
    this.songImage = false,
    required this.background,
    required this.fab,
    this.colors,
    required this.header,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final base = ref.watch(baseThemeProvider);
    ref.listen(musicSourceProvider, (previous, next) {
      if (next.id != previous?.id) {
        context.router.popUntilRoot();
      }
    });

    final pagingController = useListQueryPagingController(
      ref,
      query: query,
      getItems: getSongs,
    );

    final widget = Scaffold(
      floatingActionButton: fab,
      body: CustomScrollView(
        slivers: [
          SliverStack(
            children: [
              SliverPositioned.fill(
                child: Container(
                  color: base.gradientLow,
                ),
              ),
              SliverPositioned.directional(
                textDirection: TextDirection.ltr,
                start: 0,
                end: 0,
                top: 0,
                child: background,
              ),
              MultiSliver(
                children: [
                  SliverSafeArea(
                    sliver: SliverToBoxAdapter(
                      child: Material(
                        type: MaterialType.transparency,
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: header,
                        ),
                      ),
                    ),
                  ),
                  PagedListQueryView(
                    pagingController: pagingController,
                    useSliver: true,
                    itemBuilder: (context, item, index) => SongListTile(
                      song: item,
                      image: songImage,
                      onTap: () =>
                          onSongTap != null ? onSongTap!(item, index) : null,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );

    if (colors != null) {
      return Theme(data: colors!.theme, child: widget);
    } else {
      return widget;
    }
  }
}

class _Header extends HookConsumerWidget {
  final UriCacheInfo? imageCache;
  final String title;
  final String? subtitle;
  final String? playText;
  final void Function()? onPlay;
  final FutureOr<void> Function()? onMore;
  final List<DownloadAction> downloadActions;

  const _Header({
    this.imageCache,
    required this.title,
    this.subtitle,
    this.playText,
    this.onPlay,
    this.onMore,
    required this.downloadActions,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final inheritedStyle = DefaultTextStyle.of(context).style;
    final theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        const SizedBox(height: 10),
        if (imageCache != null)
          CardClip(
            square: false,
            child: UriCacheInfoImage(
              height: 300,
              fit: BoxFit.contain,
              placeholderStyle: PlaceholderStyle.spinner,
              cache: imageCache!,
            ),
          ),
        const SizedBox(height: 16),
        Column(
          children: [
            Text(
              title,
              style: theme.textTheme.titleLarge!.copyWith(
                color: inheritedStyle.color,
                fontWeight: FontWeight.w500,
              ),
              textAlign: TextAlign.center,
            ),
            Text(
              subtitle ?? '',
              style: theme.textTheme.titleMedium!.copyWith(
                color: inheritedStyle.color,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
        const SizedBox(height: 16),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            if (downloadActions.isNotEmpty)
              IconButton(
                onPressed: downloadActions.first.action,
                icon: downloadActions.first.type == DownloadActionType.delete
                    ? const Icon(Icons.download_done_rounded)
                    : downloadActions.first.iconBuilder(context),
              ),
            if (onPlay != null)
              FilledButton.icon(
                onPressed: onPlay,
                icon: const Icon(Icons.play_arrow_rounded),
                label: Text(playText ?? ''),
              ),
            if (onMore != null)
              IconButton(
                onPressed: onMore,
                icon: const Icon(Icons.more_horiz),
              )
          ],
        ),
      ],
    );
  }
}
