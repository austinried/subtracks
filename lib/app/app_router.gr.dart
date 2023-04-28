// **************************************************************************
// AutoRouteGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND

// **************************************************************************
// AutoRouteGenerator
// **************************************************************************
//
// ignore_for_file: type=lint

part of 'app_router.dart';

class _$AppRouter extends RootStackRouter {
  _$AppRouter([GlobalKey<NavigatorState>? navigatorKey]) : super(navigatorKey);

  @override
  final Map<String, PageFactory> pagesMap = {
    RootRouter.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const EmptyRouterPage(),
      );
    },
    NowPlayingRoute.name: (routeData) {
      return CustomPage<dynamic>(
        routeData: routeData,
        child: const NowPlayingPage(),
        transitionsBuilder: TransitionsBuilders.slideBottom,
        durationInMilliseconds: 200,
        reverseDurationInMilliseconds: 160,
        opaque: true,
        barrierDismissible: false,
      );
    },
    BottomNavTabsRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const BottomNavTabsPage(),
      );
    },
    LibraryRouter.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const EmptyRouterPage(),
      );
    },
    BrowseRouter.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const EmptyRouterPage(),
      );
    },
    SearchRouter.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const EmptyRouterPage(),
      );
    },
    SettingsRouter.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const EmptyRouterPage(),
      );
    },
    LibraryTabsRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const LibraryTabsPage(),
      );
    },
    AlbumSongsRoute.name: (routeData) {
      final pathParams = routeData.inheritedPathParams;
      final args = routeData.argsAs<AlbumSongsRouteArgs>(
          orElse: () => AlbumSongsRouteArgs(id: pathParams.getString('id')));
      return CustomPage<dynamic>(
        routeData: routeData,
        child: AlbumSongsPage(
          key: args.key,
          id: args.id,
        ),
        transitionsBuilder: TransitionsBuilders.slideRightWithFade,
        durationInMilliseconds: 160,
        reverseDurationInMilliseconds: 160,
        opaque: true,
        barrierDismissible: false,
      );
    },
    ArtistRoute.name: (routeData) {
      final pathParams = routeData.inheritedPathParams;
      final args = routeData.argsAs<ArtistRouteArgs>(
          orElse: () => ArtistRouteArgs(id: pathParams.getString('id')));
      return CustomPage<dynamic>(
        routeData: routeData,
        child: ArtistPage(
          key: args.key,
          id: args.id,
        ),
        transitionsBuilder: TransitionsBuilders.slideRightWithFade,
        durationInMilliseconds: 160,
        reverseDurationInMilliseconds: 160,
        opaque: true,
        barrierDismissible: false,
      );
    },
    PlaylistSongsRoute.name: (routeData) {
      final pathParams = routeData.inheritedPathParams;
      final args = routeData.argsAs<PlaylistSongsRouteArgs>(
          orElse: () => PlaylistSongsRouteArgs(id: pathParams.getString('id')));
      return CustomPage<dynamic>(
        routeData: routeData,
        child: PlaylistSongsPage(
          key: args.key,
          id: args.id,
        ),
        transitionsBuilder: TransitionsBuilders.slideRightWithFade,
        durationInMilliseconds: 160,
        reverseDurationInMilliseconds: 160,
        opaque: true,
        barrierDismissible: false,
      );
    },
    GenreSongsRoute.name: (routeData) {
      final pathParams = routeData.inheritedPathParams;
      final args = routeData.argsAs<GenreSongsRouteArgs>(
          orElse: () =>
              GenreSongsRouteArgs(genre: pathParams.getString('genre')));
      return CustomPage<dynamic>(
        routeData: routeData,
        child: GenreSongsPage(
          key: args.key,
          genre: args.genre,
        ),
        transitionsBuilder: TransitionsBuilders.slideRightWithFade,
        durationInMilliseconds: 160,
        reverseDurationInMilliseconds: 160,
        opaque: true,
        barrierDismissible: false,
      );
    },
    LibraryAlbumsRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const LibraryAlbumsPage(),
      );
    },
    LibraryArtistsRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const LibraryArtistsPage(),
      );
    },
    LibraryPlaylistsRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const LibraryPlaylistsPage(),
      );
    },
    LibrarySongsRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const LibrarySongsPage(),
      );
    },
    BrowseRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const BrowsePage(),
      );
    },
    SearchRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const SearchPage(),
      );
    },
    SettingsRoute.name: (routeData) {
      return MaterialPageX<dynamic>(
        routeData: routeData,
        child: const SettingsPage(),
      );
    },
    SourceRoute.name: (routeData) {
      final pathParams = routeData.inheritedPathParams;
      final args = routeData.argsAs<SourceRouteArgs>(
          orElse: () => SourceRouteArgs(id: pathParams.optInt('id')));
      return CustomPage<dynamic>(
        routeData: routeData,
        child: SourcePage(
          key: args.key,
          id: args.id,
        ),
        transitionsBuilder: TransitionsBuilders.slideRightWithFade,
        durationInMilliseconds: 160,
        reverseDurationInMilliseconds: 160,
        opaque: true,
        barrierDismissible: false,
      );
    },
  };

  @override
  List<RouteConfig> get routes => [
        RouteConfig(
          RootRouter.name,
          path: '/',
          children: [
            RouteConfig(
              BottomNavTabsRoute.name,
              path: '',
              parent: RootRouter.name,
              children: [
                RouteConfig(
                  LibraryRouter.name,
                  path: 'library',
                  parent: BottomNavTabsRoute.name,
                  children: [
                    RouteConfig(
                      LibraryTabsRoute.name,
                      path: '',
                      parent: LibraryRouter.name,
                      children: [
                        RouteConfig(
                          LibraryAlbumsRoute.name,
                          path: 'albums',
                          parent: LibraryTabsRoute.name,
                        ),
                        RouteConfig(
                          LibraryArtistsRoute.name,
                          path: 'artists',
                          parent: LibraryTabsRoute.name,
                        ),
                        RouteConfig(
                          LibraryPlaylistsRoute.name,
                          path: 'playlists',
                          parent: LibraryTabsRoute.name,
                        ),
                        RouteConfig(
                          LibrarySongsRoute.name,
                          path: 'songs',
                          parent: LibraryTabsRoute.name,
                        ),
                      ],
                    ),
                    RouteConfig(
                      AlbumSongsRoute.name,
                      path: 'album/:id',
                      parent: LibraryRouter.name,
                    ),
                    RouteConfig(
                      ArtistRoute.name,
                      path: 'artist/:id',
                      parent: LibraryRouter.name,
                    ),
                    RouteConfig(
                      PlaylistSongsRoute.name,
                      path: 'playlist/:id',
                      parent: LibraryRouter.name,
                    ),
                    RouteConfig(
                      GenreSongsRoute.name,
                      path: 'genre/:genre',
                      parent: LibraryRouter.name,
                    ),
                  ],
                ),
                RouteConfig(
                  BrowseRouter.name,
                  path: 'browse',
                  parent: BottomNavTabsRoute.name,
                  children: [
                    RouteConfig(
                      BrowseRoute.name,
                      path: '',
                      parent: BrowseRouter.name,
                    ),
                    RouteConfig(
                      AlbumSongsRoute.name,
                      path: 'album/:id',
                      parent: BrowseRouter.name,
                    ),
                    RouteConfig(
                      ArtistRoute.name,
                      path: 'artist/:id',
                      parent: BrowseRouter.name,
                    ),
                    RouteConfig(
                      PlaylistSongsRoute.name,
                      path: 'playlist/:id',
                      parent: BrowseRouter.name,
                    ),
                    RouteConfig(
                      GenreSongsRoute.name,
                      path: 'genre/:genre',
                      parent: BrowseRouter.name,
                    ),
                  ],
                ),
                RouteConfig(
                  SearchRouter.name,
                  path: 'search',
                  parent: BottomNavTabsRoute.name,
                  children: [
                    RouteConfig(
                      SearchRoute.name,
                      path: '',
                      parent: SearchRouter.name,
                    ),
                    RouteConfig(
                      AlbumSongsRoute.name,
                      path: 'album/:id',
                      parent: SearchRouter.name,
                    ),
                    RouteConfig(
                      ArtistRoute.name,
                      path: 'artist/:id',
                      parent: SearchRouter.name,
                    ),
                    RouteConfig(
                      PlaylistSongsRoute.name,
                      path: 'playlist/:id',
                      parent: SearchRouter.name,
                    ),
                    RouteConfig(
                      GenreSongsRoute.name,
                      path: 'genre/:genre',
                      parent: SearchRouter.name,
                    ),
                  ],
                ),
                RouteConfig(
                  SettingsRouter.name,
                  path: 'settings',
                  parent: BottomNavTabsRoute.name,
                  children: [
                    RouteConfig(
                      SettingsRoute.name,
                      path: '',
                      parent: SettingsRouter.name,
                    ),
                    RouteConfig(
                      SourceRoute.name,
                      path: 'source/:id',
                      parent: SettingsRouter.name,
                    ),
                  ],
                ),
              ],
            )
          ],
        ),
        RouteConfig(
          NowPlayingRoute.name,
          path: '/now-playing',
        ),
      ];
}

/// generated route for
/// [EmptyRouterPage]
class RootRouter extends PageRouteInfo<void> {
  const RootRouter({List<PageRouteInfo>? children})
      : super(
          RootRouter.name,
          path: '/',
          initialChildren: children,
        );

  static const String name = 'RootRouter';
}

/// generated route for
/// [NowPlayingPage]
class NowPlayingRoute extends PageRouteInfo<void> {
  const NowPlayingRoute()
      : super(
          NowPlayingRoute.name,
          path: '/now-playing',
        );

  static const String name = 'NowPlayingRoute';
}

/// generated route for
/// [BottomNavTabsPage]
class BottomNavTabsRoute extends PageRouteInfo<void> {
  const BottomNavTabsRoute({List<PageRouteInfo>? children})
      : super(
          BottomNavTabsRoute.name,
          path: '',
          initialChildren: children,
        );

  static const String name = 'BottomNavTabsRoute';
}

/// generated route for
/// [EmptyRouterPage]
class LibraryRouter extends PageRouteInfo<void> {
  const LibraryRouter({List<PageRouteInfo>? children})
      : super(
          LibraryRouter.name,
          path: 'library',
          initialChildren: children,
        );

  static const String name = 'LibraryRouter';
}

/// generated route for
/// [EmptyRouterPage]
class BrowseRouter extends PageRouteInfo<void> {
  const BrowseRouter({List<PageRouteInfo>? children})
      : super(
          BrowseRouter.name,
          path: 'browse',
          initialChildren: children,
        );

  static const String name = 'BrowseRouter';
}

/// generated route for
/// [EmptyRouterPage]
class SearchRouter extends PageRouteInfo<void> {
  const SearchRouter({List<PageRouteInfo>? children})
      : super(
          SearchRouter.name,
          path: 'search',
          initialChildren: children,
        );

  static const String name = 'SearchRouter';
}

/// generated route for
/// [EmptyRouterPage]
class SettingsRouter extends PageRouteInfo<void> {
  const SettingsRouter({List<PageRouteInfo>? children})
      : super(
          SettingsRouter.name,
          path: 'settings',
          initialChildren: children,
        );

  static const String name = 'SettingsRouter';
}

/// generated route for
/// [LibraryTabsPage]
class LibraryTabsRoute extends PageRouteInfo<void> {
  const LibraryTabsRoute({List<PageRouteInfo>? children})
      : super(
          LibraryTabsRoute.name,
          path: '',
          initialChildren: children,
        );

  static const String name = 'LibraryTabsRoute';
}

/// generated route for
/// [AlbumSongsPage]
class AlbumSongsRoute extends PageRouteInfo<AlbumSongsRouteArgs> {
  AlbumSongsRoute({
    Key? key,
    required String id,
  }) : super(
          AlbumSongsRoute.name,
          path: 'album/:id',
          args: AlbumSongsRouteArgs(
            key: key,
            id: id,
          ),
          rawPathParams: {'id': id},
        );

  static const String name = 'AlbumSongsRoute';
}

class AlbumSongsRouteArgs {
  const AlbumSongsRouteArgs({
    this.key,
    required this.id,
  });

  final Key? key;

  final String id;

  @override
  String toString() {
    return 'AlbumSongsRouteArgs{key: $key, id: $id}';
  }
}

/// generated route for
/// [ArtistPage]
class ArtistRoute extends PageRouteInfo<ArtistRouteArgs> {
  ArtistRoute({
    Key? key,
    required String id,
  }) : super(
          ArtistRoute.name,
          path: 'artist/:id',
          args: ArtistRouteArgs(
            key: key,
            id: id,
          ),
          rawPathParams: {'id': id},
        );

  static const String name = 'ArtistRoute';
}

class ArtistRouteArgs {
  const ArtistRouteArgs({
    this.key,
    required this.id,
  });

  final Key? key;

  final String id;

  @override
  String toString() {
    return 'ArtistRouteArgs{key: $key, id: $id}';
  }
}

/// generated route for
/// [PlaylistSongsPage]
class PlaylistSongsRoute extends PageRouteInfo<PlaylistSongsRouteArgs> {
  PlaylistSongsRoute({
    Key? key,
    required String id,
  }) : super(
          PlaylistSongsRoute.name,
          path: 'playlist/:id',
          args: PlaylistSongsRouteArgs(
            key: key,
            id: id,
          ),
          rawPathParams: {'id': id},
        );

  static const String name = 'PlaylistSongsRoute';
}

class PlaylistSongsRouteArgs {
  const PlaylistSongsRouteArgs({
    this.key,
    required this.id,
  });

  final Key? key;

  final String id;

  @override
  String toString() {
    return 'PlaylistSongsRouteArgs{key: $key, id: $id}';
  }
}

/// generated route for
/// [GenreSongsPage]
class GenreSongsRoute extends PageRouteInfo<GenreSongsRouteArgs> {
  GenreSongsRoute({
    Key? key,
    required String genre,
  }) : super(
          GenreSongsRoute.name,
          path: 'genre/:genre',
          args: GenreSongsRouteArgs(
            key: key,
            genre: genre,
          ),
          rawPathParams: {'genre': genre},
        );

  static const String name = 'GenreSongsRoute';
}

class GenreSongsRouteArgs {
  const GenreSongsRouteArgs({
    this.key,
    required this.genre,
  });

  final Key? key;

  final String genre;

  @override
  String toString() {
    return 'GenreSongsRouteArgs{key: $key, genre: $genre}';
  }
}

/// generated route for
/// [LibraryAlbumsPage]
class LibraryAlbumsRoute extends PageRouteInfo<void> {
  const LibraryAlbumsRoute()
      : super(
          LibraryAlbumsRoute.name,
          path: 'albums',
        );

  static const String name = 'LibraryAlbumsRoute';
}

/// generated route for
/// [LibraryArtistsPage]
class LibraryArtistsRoute extends PageRouteInfo<void> {
  const LibraryArtistsRoute()
      : super(
          LibraryArtistsRoute.name,
          path: 'artists',
        );

  static const String name = 'LibraryArtistsRoute';
}

/// generated route for
/// [LibraryPlaylistsPage]
class LibraryPlaylistsRoute extends PageRouteInfo<void> {
  const LibraryPlaylistsRoute()
      : super(
          LibraryPlaylistsRoute.name,
          path: 'playlists',
        );

  static const String name = 'LibraryPlaylistsRoute';
}

/// generated route for
/// [LibrarySongsPage]
class LibrarySongsRoute extends PageRouteInfo<void> {
  const LibrarySongsRoute()
      : super(
          LibrarySongsRoute.name,
          path: 'songs',
        );

  static const String name = 'LibrarySongsRoute';
}

/// generated route for
/// [BrowsePage]
class BrowseRoute extends PageRouteInfo<void> {
  const BrowseRoute()
      : super(
          BrowseRoute.name,
          path: '',
        );

  static const String name = 'BrowseRoute';
}

/// generated route for
/// [SearchPage]
class SearchRoute extends PageRouteInfo<void> {
  const SearchRoute()
      : super(
          SearchRoute.name,
          path: '',
        );

  static const String name = 'SearchRoute';
}

/// generated route for
/// [SettingsPage]
class SettingsRoute extends PageRouteInfo<void> {
  const SettingsRoute()
      : super(
          SettingsRoute.name,
          path: '',
        );

  static const String name = 'SettingsRoute';
}

/// generated route for
/// [SourcePage]
class SourceRoute extends PageRouteInfo<SourceRouteArgs> {
  SourceRoute({
    Key? key,
    int? id,
  }) : super(
          SourceRoute.name,
          path: 'source/:id',
          args: SourceRouteArgs(
            key: key,
            id: id,
          ),
          rawPathParams: {'id': id},
        );

  static const String name = 'SourceRoute';
}

class SourceRouteArgs {
  const SourceRouteArgs({
    this.key,
    this.id,
  });

  final Key? key;

  final int? id;

  @override
  String toString() {
    return 'SourceRouteArgs{key: $key, id: $id}';
  }
}
