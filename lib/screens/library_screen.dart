import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:material_symbols_icons/symbols.dart';

import '../util/custom_scroll_fix.dart';

class LibraryScreen extends StatefulWidget {
  const LibraryScreen({super.key});

  @override
  State<LibraryScreen> createState() => _LibraryScreenState();
}

class _LibraryScreenState extends State<LibraryScreen>
    with SingleTickerProviderStateMixin {
  late final TabController tabController;

  final iconSize = 26.0;
  final tabHeight = 32.0;

  late final List<(String, Widget)> tabs = [
    ('Home', Icon(Symbols.home_rounded, size: iconSize)),
    ('Albums', Icon(Symbols.album_rounded, size: iconSize)),
    ('Artists', Icon(Symbols.person_rounded, size: iconSize)),
    ('Songs', Icon(Symbols.music_note_rounded, size: iconSize)),
    ('Playlists', Icon(Symbols.playlist_play_rounded, size: iconSize)),
  ];

  @override
  void initState() {
    super.initState();
    tabController = TabController(
      length: tabs.length,
      initialIndex: 1,
      vsync: this,
    );
  }

  @override
  void dispose() {
    tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return IconTheme(
      data: IconThemeData(
        fill: 1,
        color: TextTheme.of(context).headlineLarge?.color,
        weight: 600,
        opticalSize: iconSize,
      ),
      child: Scaffold(
        body: NestedScrollView(
          floatHeaderSlivers: true,
          headerSliverBuilder: (context, innerBoxIsScrolled) {
            return <Widget>[
              SliverOverlapAbsorber(
                handle: NestedScrollView.sliverOverlapAbsorberHandleFor(
                  context,
                ),
                sliver: SliverAppBar(
                  flexibleSpace: FlexibleSpaceBar(
                    collapseMode: CollapseMode.pin,
                    background: SafeArea(
                      child: Padding(
                        padding: EdgeInsets.symmetric(
                          horizontal: 18,
                          vertical: 16,
                        ),
                        child: Text(
                          'Albums',
                          style: TextTheme.of(context).headlineLarge?.copyWith(
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ),
                    ),
                  ),
                  pinned: true,
                  floating: true,
                  bottom: PreferredSize(
                    preferredSize: Size.fromHeight(tabHeight + 18),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 10),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          TabBar(
                            controller: tabController,
                            dividerColor: Colors.transparent,
                            isScrollable: true,
                            tabAlignment: TabAlignment.start,
                            indicatorSize: TabBarIndicatorSize.label,
                            labelPadding: EdgeInsets.symmetric(
                              horizontal: 2,
                            ),
                            labelColor: Theme.of(context).primaryColorDark,
                            unselectedLabelColor: Theme.of(
                              context,
                            ).textTheme.headlineLarge?.color,
                            padding: EdgeInsets.symmetric(
                              // horizontal: 12,
                              vertical: 8,
                            ),
                            splashBorderRadius: BorderRadius.circular(8),
                            indicator: BoxDecoration(
                              color: Theme.of(
                                context,
                              ).primaryTextTheme.headlineLarge?.color,
                              borderRadius: BorderRadius.circular(8),
                            ),
                            tabs: tabs
                                .map(
                                  (tab) => Tab(
                                    height: tabHeight,
                                    child: Padding(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 6,
                                      ),
                                      child: tab.$2,
                                    ),
                                  ),
                                )
                                .toList(),
                          ),
                          IconButton(
                            onPressed: () {
                              context.push('/settings');
                            },
                            icon: Icon(
                              Symbols.settings_rounded,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ];
          },
          body: Builder(
            builder: (context) {
              return CustomScrollProvider(
                tabController: tabController,
                parent: PrimaryScrollController.of(context),
                child: TabBarView(
                  // These are the contents of the tab views, below the tabs.
                  controller: tabController,
                  children: tabs.map((tab) {
                    final index = tabs.indexOf(tab);
                    return SafeArea(
                      top: false,
                      bottom: false,
                      child: NewWidget(index: index, tab: tab),
                    );
                  }).toList(),
                ),
              );
            },
          ),
        ),
      ),
    );
  }
}

class NewWidget extends StatefulWidget {
  const NewWidget({
    super.key,
    required this.index,
    required this.tab,
  });

  final int index;
  final (String, Widget) tab;

  @override
  State<NewWidget> createState() => _NewWidgetState();
}

class _NewWidgetState extends State<NewWidget>
    with AutomaticKeepAliveClientMixin {
  @override
  bool get wantKeepAlive => true;

  @override
  Widget build(BuildContext context) {
    super.build(context);

    final scrollProvider = CustomScrollProviderData.of(context);

    return CustomScrollView(
      controller: scrollProvider.scrollControllers[widget.index],
      slivers: <Widget>[
        SliverOverlapInjector(
          handle: NestedScrollView.sliverOverlapAbsorberHandleFor(
            context,
          ),
        ),
        SliverPadding(
          padding: const EdgeInsets.all(8.0),
          sliver: SliverFixedExtentList(
            itemExtent: 48.0,
            delegate: SliverChildBuilderDelegate(
              (BuildContext context, int index) {
                return ListTile(
                  title: Text('Item $index'),
                  onTap: () {
                    context.push('/album');
                  },
                );
              },
              childCount: 30,
            ),
          ),
        ),
      ],
    );
  }
}
