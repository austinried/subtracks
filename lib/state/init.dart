import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../app/app.dart';
import '../app/app_router.dart';
import '../app/pages/bottom_nav_page.dart';
import '../app/pages/library_page.dart';
import '../services/audio_service.dart';
import '../services/download_service.dart';
import '../services/settings_service.dart';
import 'audio.dart';
import 'settings.dart';

part 'init.g.dart';

@Riverpod(keepAlive: true)
FutureOr<Map<String, String>> env(EnvRef ref) async {
  await dotenv.load();
  return dotenv.env;
}

@Riverpod(keepAlive: true)
AppRouter router(RouterRef ref) {
  return AppRouter();
}

@Riverpod(keepAlive: true)
FutureOr<Uri> placeholderImageUri(PlaceholderImageUriRef ref) async {
  final byteData = await rootBundle.load('assets/placeholder.png');
  final docsDir = await getApplicationDocumentsDirectory();

  return (await File('${docsDir.path}/placeholder.png').writeAsBytes(byteData
          .buffer
          .asUint8List(byteData.offsetInBytes, byteData.lengthInBytes)))
      .uri;
}

@Riverpod(keepAlive: true)
FutureOr<Uri> placeholderThumbImageUri(PlaceholderThumbImageUriRef ref) async {
  final byteData = await rootBundle.load('assets/placeholder_thumb.png');
  final docsDir = await getApplicationDocumentsDirectory();

  return (await File('${docsDir.path}/placeholder_thumb.png').writeAsBytes(
          byteData.buffer
              .asUint8List(byteData.offsetInBytes, byteData.lengthInBytes)))
      .uri;
}

@Riverpod(keepAlive: true)
FutureOr<PackageInfo> packageInfo(PackageInfoRef ref) async {
  return await PackageInfo.fromPlatform();
}

@Riverpod(keepAlive: true)
FutureOr<void> init(InitRef ref) async {
  ref.watch(routerProvider);
  await ref.watch(envProvider.future);
  await ref.read(packageInfoProvider.future);
  await ref.watch(placeholderImageUriProvider.future);
  await ref.watch(placeholderThumbImageUriProvider.future);

  await ref.read(networkModeProvider.future);
  await ref.read(maxBitrateProvider.future);
  await ref.watch(settingsServiceProvider.notifier).init();

  final audio = await ref.watch(audioControlInitProvider.future);
  await audio.init();
  ref.watch(lastAudioStateServiceProvider.notifier);

  await ref.watch(downloadServiceProvider.notifier).init();

  await ref.watch(lastPathProvider.notifier).init();
  ref.watch(lastBottomNavStateServiceProvider.notifier);
  ref.watch(lastLibraryStateServiceProvider.notifier);

  await ref.watch(libraryListsProvider.notifier).init();
}
