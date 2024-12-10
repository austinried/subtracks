import 'package:auto_route/auto_route.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../database/database.dart';
import '../../services/settings_service.dart';
import '../../state/settings.dart';
import '../app_router.dart';
import '../now_playing_bar.dart';

part 'bottom_nav_page.g.dart';

@Riverpod(keepAlive: true)
TabObserver bottomTabObserver(BottomTabObserverRef ref) {
  return TabObserver();
}

@Riverpod(keepAlive: true)
Stream<String> bottomTabPath(BottomTabPathRef ref) async* {
  final observer = ref.watch(bottomTabObserverProvider);
  await for (var tab in observer.path) {
    yield tab;
  }
}

@Riverpod(keepAlive: true)
class LastBottomNavStateService extends _$LastBottomNavStateService {
  @override
  Future<void> build() async {
    final db = ref.watch(databaseProvider);
    final tab = ref.watch(bottomTabPathProvider).valueOrNull;
    if (tab == null || tab == 'settings' || tab == 'search') {
      return;
    }

    await db.saveLastBottomNavState(LastBottomNavStateData(id: 1, tab: tab));
  }
}

class BottomNavTabsPage extends HookConsumerWidget {
  const BottomNavTabsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);

    final observer = ref.watch(bottomTabObserverProvider);
    const navElevation = 3.0;

    return AutoTabsRouter(
      lazyLoad: false,
      inheritNavigatorObservers: false,
      navigatorObservers: () => [observer],
      routes: const [
        LibraryRouter(),
        BrowseRouter(),
        SearchRouter(),
        SettingsRouter(),
      ],
      builder: (context, child, animation) {
        return AnnotatedRegion<SystemUiOverlayStyle>(
          value: SystemUiOverlayStyle.light.copyWith(
            systemNavigationBarColor: ElevationOverlay.applySurfaceTint(
              theme.colorScheme.surface,
              theme.colorScheme.surfaceTint,
              navElevation,
            ),
            statusBarColor: Colors.transparent,
          ),
          child: Scaffold(
            body: Stack(
              alignment: AlignmentDirectional.bottomStart,
              children: [
                FadeTransition(
                  opacity: animation,
                  child: child,
                ),
                const OfflineIndicator(),
              ],
            ),
            bottomNavigationBar: const _BottomNavBar(
              navElevation: navElevation,
            ),
          ),
        );
      },
    );
  }
}

class OfflineIndicator extends HookConsumerWidget {
  const OfflineIndicator({
    super.key,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final offline = ref.watch(offlineModeProvider);
    final testing = useState(false);

    if (!offline) {
      return Container();
    }

    return Padding(
      padding: const EdgeInsetsDirectional.only(
        start: 20,
        bottom: 20,
      ),
      child: FilledButton.tonal(
        style: const ButtonStyle(
          padding: WidgetStatePropertyAll<EdgeInsetsGeometry>(
            EdgeInsets.zero,
          ),
          fixedSize: WidgetStatePropertyAll<Size>(
            Size(42, 42),
          ),
          minimumSize: WidgetStatePropertyAll<Size>(
            Size(42, 42),
          ),
        ),
        onPressed: () async {
          testing.value = true;
          await ref.read(offlineModeProvider.notifier).setMode(false);
          testing.value = false;
        },
        child: testing.value
            ? const SizedBox(
                height: 16,
                width: 16,
                child: CircularProgressIndicator(strokeWidth: 2.5),
              )
            : const Padding(
                padding: EdgeInsets.only(left: 2, bottom: 2),
                child: Icon(
                  Icons.cloud_off_rounded,
                  size: 20,
                ),
              ),
      ),
    );
  }
}

class _BottomNavBar extends HookConsumerWidget {
  final double navElevation;

  const _BottomNavBar({
    required this.navElevation,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final iconTheme = IconTheme.of(context);
    final tabsRouter = AutoTabsRouter.of(context);

    useListenableSelector(tabsRouter, () => tabsRouter.activeIndex);

    return Column(
      mainAxisSize: MainAxisSize.min,
      mainAxisAlignment: MainAxisAlignment.end,
      children: [
        const NowPlayingBar(),
        NavigationBar(
          elevation: navElevation,
          height: 50,
          labelBehavior: NavigationDestinationLabelBehavior.alwaysHide,
          selectedIndex: tabsRouter.activeIndex,
          onDestinationSelected: (index) {
            // TODO: replace this with a proper first-time setup flow
            final hasActiveSource = ref.read(settingsServiceProvider.select(
              (value) => value.activeSource != null,
            ));

            if (!hasActiveSource) {
              tabsRouter.setActiveIndex(3);
            } else {
              tabsRouter.setActiveIndex(index);
            }
          },
          destinations: [
            const NavigationDestination(
              icon: Icon(Icons.music_note),
              label: 'Library',
            ),
            NavigationDestination(
              icon: Builder(builder: (context) {
                return SvgPicture.asset(
                  'assets/tag_FILL0_wght400_GRAD0_opsz24.svg',
                  colorFilter: ColorFilter.mode(
                    iconTheme.color!.withOpacity(iconTheme.opacity ?? 1),
                    BlendMode.srcIn,
                  ),
                  height: 28,
                );
              }),
              label: 'Browse',
            ),
            const NavigationDestination(
              icon: Icon(Icons.search_rounded),
              label: 'Search',
            ),
            const NavigationDestination(
              icon: Icon(Icons.settings_rounded),
              label: 'Settings',
            ),
          ],
        ),
      ],
    );
  }
}
