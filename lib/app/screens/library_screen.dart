import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:material_symbols_icons/symbols.dart';

import '../../database/query.dart';
import '../../l10n/generated/app_localizations.dart';
import '../state/services.dart';
import '../state/source.dart';
import '../ui/lists/albums_grid.dart';
import '../ui/lists/artists_list.dart';
import '../ui/lists/items.dart';
import '../ui/lists/playlists_list.dart';
import '../ui/lists/songs_list.dart';
import '../util/custom_scroll_fix.dart';

const kIconSize = 26.0;
const kTabHeight = 36.0;

enum LibraryTab {
  home(Icon(Symbols.home_rounded)),
  albums(Icon(Symbols.album_rounded)),
  artists(Icon(Symbols.person_rounded)),
  songs(Icon(Symbols.music_note_rounded)),
  playlists(Icon(Symbols.playlist_play_rounded));

  const LibraryTab(this.icon);

  final Widget icon;

  @override
  toString() => name;
}

class LibraryScreen extends HookConsumerWidget {
  const LibraryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tabController = useTabController(
      initialLength: LibraryTab.values.length,
      initialIndex: 1,
    );

    return Scaffold(
      body: NestedScrollView(
        floatHeaderSlivers: true,
        headerSliverBuilder: (context, innerBoxIsScrolled) => <Widget>[
          SliverOverlapAbsorber(
            handle: NestedScrollView.sliverOverlapAbsorberHandleFor(context),
            sliver: LibraryTabsHeader(tabController: tabController),
          ),
        ],
        body: Builder(
          builder: (context) => CustomScrollProvider(
            tabController: tabController,
            parent: PrimaryScrollController.of(context),
            child: LibraryTabBarView(tabController: tabController),
          ),
        ),
      ),
    );
  }
}

class LibraryTabBarView extends HookConsumerWidget {
  const LibraryTabBarView({
    super.key,
    required this.tabController,
  });

  final TabController tabController;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sourceId = ref.watch(sourceIdProvider);

    final songsQuery = SongsQuery(
      sourceId: sourceId,
      sort: IList([
        SortingTerm.songsAsc(SongsColumn.albumArtist),
        SortingTerm.songsAsc(SongsColumn.album),
        SortingTerm.songsAsc(SongsColumn.disc),
        SortingTerm.songsAsc(SongsColumn.track),
        SortingTerm.songsAsc(SongsColumn.title),
      ]),
    );

    return TabBarView(
      controller: tabController,
      children: LibraryTab.values
          .map(
            (tab) => TabScrollView(
              index: LibraryTab.values.indexOf(tab),
              sliver: switch (tab) {
                LibraryTab.albums => AlbumsGrid(),
                LibraryTab.artists => ArtistsList(),
                LibraryTab.playlists => PlaylistsList(),
                LibraryTab.songs => SongsList(
                  query: songsQuery,
                  itemBuilder: (context, item, index) => SongListTile(
                    song: item.song,
                    coverArt: item.albumCoverArt,
                    showLeading: true,
                    onTap: () {},
                  ),
                ),
                _ => ArtistsList(),
              },
            ),
          )
          .toList(),
    );
  }
}

class LibraryTabsHeader extends HookConsumerWidget {
  const LibraryTabsHeader({
    super.key,
    required this.tabController,
  });

  final TabController tabController;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    return SliverAppBar(
      pinned: true,
      floating: true,
      flexibleSpace: FlexibleSpaceBar(
        collapseMode: CollapseMode.pin,
        background: SafeArea(
          child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 18, vertical: 16),
            child: TabTitleText(tabController: tabController),
          ),
        ),
      ),
      bottom: PreferredSize(
        preferredSize: Size.fromHeight(kTabHeight + 18),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 10),
          child: IconTheme(
            data: IconThemeData(
              fill: 1,
              color: theme.textTheme.headlineLarge?.color,
              weight: 600,
              opticalSize: kIconSize,
              size: kIconSize,
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                TabBar(
                  controller: tabController,
                  dividerColor: Colors.transparent,
                  isScrollable: true,
                  tabAlignment: TabAlignment.start,
                  indicatorSize: TabBarIndicatorSize.label,
                  labelPadding: EdgeInsets.symmetric(horizontal: 2),
                  labelColor: theme.primaryColorDark,
                  unselectedLabelColor: theme.textTheme.headlineLarge?.color,
                  padding: EdgeInsets.symmetric(vertical: 8),
                  splashBorderRadius: BorderRadius.circular(8),
                  indicator: BoxDecoration(
                    color: theme.primaryTextTheme.headlineLarge?.color,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  tabs: LibraryTab.values
                      .map(
                        (tab) => Tab(
                          height: kTabHeight,
                          child: Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 6),
                            child: tab.icon,
                          ),
                        ),
                      )
                      .toList(),
                ),
                Spacer(),
                SyncButton(),
                SettingsButton(),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class TabTitleText extends HookConsumerWidget {
  const TabTitleText({
    super.key,
    required this.tabController,
  });

  final TabController tabController;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    final text = TextTheme.of(context);

    String tabLocalization(LibraryTab tab) => switch (tab) {
      LibraryTab.albums => l.navigationTabsAlbums,
      LibraryTab.home => l.navigationTabsHome,
      LibraryTab.artists => l.navigationTabsArtists,
      LibraryTab.songs => l.navigationTabsSongs,
      LibraryTab.playlists => l.navigationTabsPlaylists,
    };

    final tabName = tabLocalization(LibraryTab.values[tabController.index]);
    final tabText = useState(tabName);

    useListenable(tabController);
    useEffect(() {
      tabText.value = tabName;
      return;
    }, [tabName]);

    return Text(tabText.value, style: text.headlineLarge);
  }
}

class TabScrollView extends HookConsumerWidget {
  const TabScrollView({
    super.key,
    required this.index,
    required this.sliver,
  });

  final int index;
  final Widget sliver;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    useAutomaticKeepAlive();

    final scrollProvider = CustomScrollProviderData.of(context);

    return CustomScrollView(
      controller: scrollProvider.scrollControllers[index],
      slivers: <Widget>[
        SliverOverlapInjector(
          handle: NestedScrollView.sliverOverlapAbsorberHandleFor(context),
        ),
        sliver,
      ],
    );
  }
}

class SyncButton extends HookConsumerWidget {
  const SyncButton({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final syncService = ref.watch(syncServiceProvider);

    return IconButton(
      icon: Icon(Symbols.sync_rounded),
      onPressed: () {
        syncService.sync();
      },
    );
  }
}

class SettingsButton extends HookConsumerWidget {
  const SettingsButton({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return IconButton(
      icon: Icon(Symbols.settings_rounded),
      onPressed: () {
        context.push('/settings');
      },
    );
  }
}
