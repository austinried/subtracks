import 'package:go_router/go_router.dart';

import 'screens/album_screen.dart';
import 'screens/artist_screen.dart';
import 'screens/library_screen.dart';
import 'screens/now_playing_screen.dart';
import 'screens/preload_screen.dart';
import 'screens/root_shell_screen.dart';
import 'screens/settings_screen.dart';

final router = GoRouter(
  initialLocation: '/preload',
  routes: [
    GoRoute(
      path: '/preload',
      builder: (context, state) => PreloadScreen(),
    ),
    ShellRoute(
      builder: (context, state, child) => RootShellScreen(child: child),
      routes: [
        GoRoute(
          path: '/',
          builder: (context, state) => LibraryScreen(),
          routes: [
            GoRoute(
              path: 'album/:id',
              builder: (context, state) =>
                  AlbumScreen(id: state.pathParameters['id']!),
            ),
            GoRoute(
              path: 'artist',
              builder: (context, state) => ArtistScreen(),
            ),
          ],
        ),
      ],
    ),
    GoRoute(
      path: '/now-playing',
      builder: (context, state) => NowPlayingScreen(),
    ),
    GoRoute(
      path: '/settings',
      builder: (context, state) => SettingsScreen(),
    ),
  ],
);
