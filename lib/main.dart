import 'package:drift/drift.dart' show Value;
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import 'app/router.dart';
import 'database/database.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final db = SubtracksDatabase();
  await db
      .into(db.sources)
      .insertOnConflictUpdate(
        SourcesCompanion.insert(
          id: Value(1),
          name: 'test navidrome',
        ),
      );
  await db
      .into(db.subsonicSettings)
      .insertOnConflictUpdate(
        SubsonicSettingsCompanion.insert(
          sourceId: Value(1),
          address: Uri.parse('http://demo.subsonic.org'),
          username: 'guest1',
          password: 'guest',
          // address: Uri.parse('http://10.0.2.2:4533'),
          // username: 'admin',
          // password: 'password',
          useTokenAuth: Value(true),
        ),
      );

  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ProviderScope(
      child: MaterialApp.router(
        themeMode: ThemeMode.dark,
        darkTheme: ThemeData.dark(useMaterial3: true),
        debugShowCheckedModeBanner: false,
        routerConfig: router,
      ),
    );
  }
}
