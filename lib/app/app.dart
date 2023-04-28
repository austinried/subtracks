import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../database/database.dart';
import '../services/settings_service.dart';
import '../state/init.dart';
import '../state/theme.dart';

part 'app.g.dart';

class MyApp extends HookConsumerWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final init = ref.watch(initProvider);
    return init.when(
      data: (_) => const App(),
      error: (e, s) => Directionality(
        textDirection: TextDirection.ltr,
        child: Container(
          color: Colors.red[900],
          child: Column(children: [
            const SizedBox(height: 100),
            Text(e.toString()),
            Text(s.toString()),
          ]),
        ),
      ),
      loading: () => const CircularProgressIndicator(),
    );
  }
}

@Riverpod(keepAlive: true)
class LastPath extends _$LastPath {
  @override
  String build() {
    return '/settings';
  }

  Future<void> init() async {
    final db = ref.read(databaseProvider);
    final lastBottomNav = await db.getLastBottomNavState().getSingleOrNull();
    final lastLibrary = await db.getLastLibraryState().getSingleOrNull();

    if (lastBottomNav == null || lastLibrary == null) return;

    // TODO: replace this with a proper first-time setup flow
    final hasActiveSource = ref.read(settingsServiceProvider.select(
      (value) => value.activeSource != null,
    ));
    if (!hasActiveSource) return;

    state = lastBottomNav.tab == 'library'
        ? '/library/${lastLibrary.tab}'
        : '/${lastBottomNav.tab}';
  }
}

class App extends HookConsumerWidget {
  const App({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final appRouter = ref.watch(routerProvider);
    final base = ref.watch(baseThemeProvider);
    final lastPath = ref.watch(lastPathProvider);

    return MaterialApp.router(
      theme: base.theme,
      debugShowCheckedModeBanner: false,
      routerDelegate: appRouter.delegate(
        initialDeepLink: lastPath,
      ),
      routeInformationParser: appRouter.defaultRouteParser(),
      localizationsDelegates: AppLocalizations.localizationsDelegates,
      supportedLocales: AppLocalizations.supportedLocales,
    );
  }
}
