import 'package:go_router/go_router.dart';

import 'screens/album_screen.dart';
import 'screens/artist_screen.dart';
import 'screens/library_screen.dart';
import 'screens/now_playing_screen.dart';
import 'screens/root_shell_screen.dart';
import 'screens/settings_screen.dart';

final router = GoRouter(
  routes: [
    ShellRoute(
      builder: (context, state, child) => RootShellScreen(child: child),
      routes: [
        GoRoute(
          path: '/',
          builder: (context, state) => LibraryScreen(),
          routes: [
            GoRoute(
              path: 'album',
              builder: (context, state) => AlbumScreen(),
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
