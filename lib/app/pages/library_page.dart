import 'package:auto_route/auto_route.dart';
import 'package:collection/collection.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../database/database.dart';
import '../../models/query.dart';
import '../app_router.dart';
import '../context_menus.dart';

part 'library_page.g.dart';

@Riverpod(keepAlive: true)
TabObserver libraryTabObserver(LibraryTabObserverRef ref) {
  return TabObserver();
}

@Riverpod(keepAlive: true)
Stream<String> libraryTabPath(LibraryTabPathRef ref) async* {
  final observer = ref.watch(libraryTabObserverProvider);
  await for (var tab in observer.path) {
    yield tab;
  }
}

@Riverpod(keepAlive: true)
class LastLibraryStateService extends _$LastLibraryStateService {
  @override
  Future<void> build() async {
    final db = ref.watch(databaseProvider);
    final tab = await ref.watch(libraryTabPathProvider.future);

    await db.saveLastLibraryState(LastLibraryStateData(
      id: 1,
      tab: tab,
      albumsList: ref.watch(libraryListQueryProvider(0)).query,
      artistsList: ref.watch(libraryListQueryProvider(1)).query,
      playlistsList: ref.watch(libraryListQueryProvider(2)).query,
      songsList: ref.watch(libraryListQueryProvider(3)).query,
    ));
  }
}

@Riverpod(keepAlive: true)
class LibraryLists extends _$LibraryLists {
  @override
  IList<LibraryListQuery> build() {
    return const IListConst([
      /// Albums
      LibraryListQuery(
        options: ListQueryOptions(
          sortColumns: IListConst([
            'albums.name',
            'albums.created',
            'albums.album_artist',
            'albums.year',
          ]),
          filterColumns: IListConst([
            'albums.starred',
            'albums.album_artist',
            'albums.year',
            'albums.genre',
          ]),
        ),
        query: ListQuery(
          page: Pagination(limit: 60),
          sort: SortBy(column: 'albums.name'),
        ),
      ),

      /// Artists
      LibraryListQuery(
        options: ListQueryOptions(
          sortColumns: IListConst([
            'artists.name',
            'artists.album_count',
          ]),
          filterColumns: IListConst([
            'artists.starred',
          ]),
        ),
        query: ListQuery(
          page: Pagination(limit: 30),
          sort: SortBy(column: 'artists.name'),
        ),
      ),

      /// Playlists
      LibraryListQuery(
        options: ListQueryOptions(
          sortColumns: IListConst([
            'playlists.name',
            'playlists.created',
            'playlists.changed',
          ]),
          filterColumns: IListConst([
            'playlists.owner',
          ]),
        ),
        query: ListQuery(
          page: Pagination(limit: 30),
          sort: SortBy(column: 'playlists.name'),
        ),
      ),

      /// Songs
      LibraryListQuery(
        options: ListQueryOptions(
          sortColumns: IListConst([
            'songs.album',
            'songs.artist',
            'songs.created',
            'songs.title',
            'songs.year',
          ]),
          filterColumns: IListConst([
            'songs.starred',
            'songs.artist',
            'songs.album',
            'songs.year',
            'songs.genre',
          ]),
        ),
        query: ListQuery(
          page: Pagination(limit: 30),
          sort: SortBy(column: 'songs.album'),
        ),
      ),
    ]);
  }

  Future<void> init() async {
    final db = ref.read(databaseProvider);
    final last = await db.getLastLibraryState().getSingleOrNull();
    if (last == null) {
      return;
    }

    state = state
        .replace(0, state[0].copyWith(query: last.albumsList))
        .replace(1, state[1].copyWith(query: last.artistsList))
        .replace(2, state[2].copyWith(query: last.playlistsList))
        .replace(3, state[3].copyWith(query: last.songsList));
  }

  void setSortColumn(int index, String column) {
    state = state.replace(
      index,
      state[index].copyWith.query.sort!(column: column),
    );
  }

  void toggleDirection(int index) {
    final toggled = state[index].query.sort?.dir == SortDirection.asc
        ? SortDirection.desc
        : SortDirection.asc;
    state = state.replace(
      index,
      state[index].copyWith.query.sort!(dir: toggled),
    );
  }

  void setFilter(int index, FilterWith filter) {
    state = state.replace(
      index,
      state[index].copyWith.query(
            filters: state[index].query.filters.updateById(
              [filter],
              (e) => e.column,
            ),
          ),
    );
  }

  void removeFilter(int index, String column) {
    state = state.replace(
      index,
      state[index].copyWith.query(
          filters: state[index]
              .query
              .filters
              .removeWhere((f) => f.column == column)),
    );
  }

  void clearFilters(int index) {
    state = state.replace(index, state[index].copyWith.query(filters: IList()));
  }
}

@Riverpod(keepAlive: true)
LibraryListQuery libraryListQuery(LibraryListQueryRef ref, int index) {
  return ref.watch(libraryListsProvider.select((value) => value[index]));
}

class LibraryTabsPage extends HookConsumerWidget {
  const LibraryTabsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final observer = ref.watch(libraryTabObserverProvider);

    return AutoTabsRouter.tabBar(
      inheritNavigatorObservers: false,
      navigatorObservers: () => [observer],
      routes: const [
        LibraryAlbumsRoute(),
        LibraryArtistsRoute(),
        LibraryPlaylistsRoute(),
        LibrarySongsRoute(),
      ],
      builder: (context, child, tabController) {
        return Scaffold(
          body: child,
          floatingActionButton: const _LibraryFilterFab(),
        );
      },
    );
  }
}

class _LibraryFilterFab extends HookConsumerWidget {
  const _LibraryFilterFab();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    final tabsRouter = AutoTabsRouter.of(context);
    final activeIndex =
        useListenableSelector(tabsRouter, () => tabsRouter.activeIndex);
    final tabHasFilters = ref.watch(libraryListQueryProvider(activeIndex)
        .select((value) => value.query.filters.isNotEmpty));

    List<Widget> dot = [];
    if (tabHasFilters) {
      dot.addAll([
        PositionedDirectional(
          top: 3,
          end: 0,
          child: Icon(
            Icons.circle,
            color: theme.colorScheme.primaryContainer,
            size: 11,
          ),
        ),
        const PositionedDirectional(
          top: 5,
          end: 1,
          child: Icon(
            Icons.circle,
            size: 7,
          ),
        ),
      ]);
    }

    return FloatingActionButton(
      heroTag: null,
      onPressed: () async {
        showContextMenu(
          context: context,
          ref: ref,
          builder: (context) => BottomSheetMenu(
            child: LibraryMenu(
              tabsRouter: tabsRouter,
            ),
          ),
        );
      },
      tooltip: 'List',
      child: Stack(
        children: [
          const Icon(
            Icons.sort_rounded,
            size: 28,
          ),
          ...dot,
        ],
      ),
    );
  }
}

class LibraryMenu extends HookConsumerWidget {
  final TabsRouter tabsRouter;

  const LibraryMenu({
    super.key,
    required this.tabsRouter,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return CustomScrollView(
      slivers: [
        SliverPadding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          sliver: SliverToBoxAdapter(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Row(
                  children: [
                    FilterChip(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(100),
                      ),
                      onSelected: (value) {},
                      selected: true,
                      label: const Icon(
                        Icons.grid_on,
                        size: 20,
                      ),
                    ),
                    const SizedBox(width: 6),
                    FilterChip(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(100),
                      ),
                      onSelected: (value) {},
                      label: const Icon(
                        Icons.grid_view_outlined,
                        size: 20,
                      ),
                    ),
                    const SizedBox(width: 6),
                    FilterChip(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(100),
                      ),
                      onSelected: (value) {},
                      label: const Icon(
                        Icons.format_list_bulleted,
                        size: 20,
                      ),
                    ),
                  ],
                ),
                _FilterToggleButton(tabsRouter: tabsRouter),
              ],
            ),
          ),
        ),
        const SliverPadding(padding: EdgeInsets.only(top: 8)),
        ListSortFilterOptions(index: tabsRouter.activeIndex),
        const SliverPadding(padding: EdgeInsets.only(top: 16)),
      ],
    );
  }
}

class _FilterToggleButton extends HookConsumerWidget {
  final TabsRouter tabsRouter;

  const _FilterToggleButton({
    required this.tabsRouter,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tabHasFilters = ref.watch(
        libraryListQueryProvider(tabsRouter.activeIndex)
            .select((value) => value.query.filters.isNotEmpty));

    return FilledButton(
      onPressed: tabHasFilters
          ? () {
              ref
                  .read(libraryListsProvider.notifier)
                  .clearFilters(tabsRouter.activeIndex);
            }
          : null,
      child: const Icon(Icons.filter_list_off_rounded),
    );
  }
}

class ListSortFilterOptions extends HookConsumerWidget {
  final int index;

  const ListSortFilterOptions({
    super.key,
    required this.index,
  });

  void Function()? _filterOnEdit(
    String column,
    BuildContext context,
    WidgetRef ref,
  ) {
    final type = column.split('.').last;
    switch (type) {
      case 'year':
        return () {
          // TODO: year filter dialog
          // showDialog(
          //   context: context,
          //   builder: (context) {
          //     return Dialog(
          //       child: Text('adsf'),
          //     );
          //   },
          // );
        };
      case 'genre':
      case 'album_artist':
      case 'owner':
      case 'album':
      case 'artist':
        // TODO: other filter dialogs
        return () {};
      default:
        return null;
    }
  }

  void Function(bool? value)? _filterOnChanged(String column, WidgetRef ref) {
    final type = column.split('.').last;
    switch (type) {
      case 'starred':
        return (value) {
          if (value!) {
            ref.read(libraryListsProvider.notifier).setFilter(
                  index,
                  FilterWith.isNull(column: column, invert: true),
                );
          } else {
            ref.read(libraryListsProvider.notifier).removeFilter(index, column);
          }
        };
      case 'year':
        // TODO: add/remove filter
        return null;
      case 'genre':
      case 'album_artist':
      case 'owner':
      case 'album':
      case 'artist':
        // TODO: add/remove filter
        return null;
      default:
        return null;
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    final list = ref.watch(libraryListQueryProvider(index));

    return SliverList(
      delegate: SliverChildListDelegate.fixed([
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Text(
            'Sort by',
            style: theme.textTheme.titleLarge,
          ),
        ),
        const SizedBox(height: 8),
        for (var column in list.options.sortColumns)
          SortOptionTile(
            column: column,
            value: list.query.sort!.copyWith(column: column),
            groupValue: list.query.sort!,
            onColumnChanged: (column) {
              if (column != null) {
                ref
                    .read(libraryListsProvider.notifier)
                    .setSortColumn(index, column);
              }
            },
            onDirectionToggle: () =>
                ref.read(libraryListsProvider.notifier).toggleDirection(index),
          ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Text(
            'Filter',
            style: theme.textTheme.titleLarge,
          ),
        ),
        for (var column in list.options.filterColumns)
          FilterOptionTile(
            column: column,
            state: list.query.filters.singleWhereOrNull(
              (e) => e.column == column,
            ),
            onEdit: _filterOnEdit(column, context, ref),
            onChanged: _filterOnChanged(column, ref),
          )
      ]),
    );
  }
}

class SortOptionTile extends HookConsumerWidget {
  final String column;
  final SortBy value;
  final SortBy groupValue;
  final void Function(String? value) onColumnChanged;
  final void Function() onDirectionToggle;

  const SortOptionTile({
    super.key,
    required this.column,
    required this.value,
    required this.groupValue,
    required this.onColumnChanged,
    required this.onDirectionToggle,
  });

  String _sortTitle(AppLocalizations l, String type) {
    type = type.split('.').last;
    switch (type) {
      case 'name':
        return l.resourcesSortByName;
      case 'album_artist':
        return l.resourcesSortByArtist;
      case 'created':
        return l.resourcesSortByAdded;
      case 'year':
        return l.resourcesSortByYear;
      case 'album_count':
        return l.resourcesSortByAlbumCount;
      case 'changed':
        return l.resourcesSortByUpdated;
      case 'album':
        return l.resourcesSortByAlbum;
      case 'artist':
        return l.resourcesSortByArtist;
      case 'title':
        return l.resourcesSortByTitle;
      default:
        return '';
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);

    return RadioListTile<String?>(
      value: value.column,
      groupValue: groupValue.column,
      onChanged: onColumnChanged,
      selected: value.column == groupValue.column,
      title: Text(_sortTitle(l, column)),
      secondary: value.column == groupValue.column
          ? IconButton(
              icon: Icon(
                value.dir == SortDirection.desc
                    ? Icons.arrow_upward_rounded
                    : Icons.arrow_downward_rounded,
              ),
              onPressed: onDirectionToggle,
            )
          : null,
    );
  }
}

class FilterOptionTile extends HookConsumerWidget {
  final String column;
  final FilterWith? state;
  final void Function(bool? value)? onChanged;
  final void Function()? onEdit;

  const FilterOptionTile({
    super.key,
    required this.column,
    required this.state,
    required this.onChanged,
    this.onEdit,
  });

  String _filterTitle(AppLocalizations l, String type) {
    type = type.split('.').last;
    switch (type) {
      case 'starred':
        return l.resourcesFilterStarred;
      case 'year':
        return l.resourcesFilterYear;
      case 'genre':
        return l.resourcesFilterGenre;
      case 'album_artist':
        return l.resourcesFilterArtist;
      case 'owner':
        return l.resourcesFilterOwner;
      case 'album':
        return l.resourcesFilterAlbum;
      case 'artist':
        return l.resourcesFilterArtist;
      default:
        return '';
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);

    return CheckboxListTile(
      value: state == null
          ? false
          : state!.map(
              equals: (value) => value.invert ? null : true,
              greaterThan: (value) => true,
              isNull: (_) => true,
              betweenInt: (_) => true,
              isIn: (value) => value.invert ? null : true,
            ),
      tristate: state?.map(
            equals: (value) => true,
            greaterThan: (value) => false,
            isNull: (_) => false,
            betweenInt: (_) => false,
            isIn: (_) => true,
          ) ??
          false,
      title: Text(_filterTitle(l, column)),
      secondary: onEdit == null
          ? null
          : IconButton(
              icon: const Icon(Icons.edit_rounded),
              onPressed: onEdit,
            ),
      controlAffinity: ListTileControlAffinity.leading,
      onChanged: onChanged,
    );
  }
}
