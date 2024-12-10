import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:palette_generator/palette_generator.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:worker_manager/worker_manager.dart';

import '../cache/image_cache.dart';
import '../models/support.dart';
import '../services/cache_service.dart';
import 'audio.dart';
import 'music.dart';

part 'theme.g.dart';

const kDarkBackgroundValue = 0.28;
const kDarkerBackgroundLightness = 0.13;
const kOnDarkerBackgroundLightness = 0.6;

PaletteColor? _rankedByLuminance(
  List<PaletteColor?> colors, [
  double minLuminance = 0.0,
  double maxLuminance = 1.0,
]) {
  for (var color in colors) {
    if (color == null) {
      continue;
    }

    final luminance = color.color.computeLuminance();
    if (luminance >= minLuminance && luminance <= maxLuminance) {
      return color;
    }
  }

  return null;
}

PaletteColor? _rankedWithValue(double value, List<PaletteColor?> colors) {
  for (var color in colors) {
    if (color == null) {
      continue;
    }

    final hsv = HSVColor.fromColor(color.color);
    if (hsv.value > value) {
      return PaletteColor(hsv.withValue(value).toColor(), 0);
    }
  }

  return null;
}

@riverpod
ColorTheme _colorTheme(_ColorThemeRef ref, Palette palette) {
  final base = ref.watch(baseThemeProvider);

  final primary = _rankedByLuminance([
    palette.dominantColor,
    palette.vibrantColor,
    palette.mutedColor,
    palette.darkVibrantColor,
  ], 0.2);
  final vibrant = _rankedByLuminance([
    palette.vibrantColor,
    palette.darkVibrantColor,
    palette.dominantColor,
  ], 0.05);
  final secondary = _rankedByLuminance([
    palette.lightMutedColor,
    palette.mutedColor,
    palette.darkMutedColor,
  ]);

  final background = _rankedWithValue(0.5, [
    palette.vibrantColor,
    palette.darkVibrantColor,
    palette.darkMutedColor,
    palette.dominantColor,
    palette.mutedColor,
    palette.lightVibrantColor,
    palette.lightMutedColor,
  ]);

  final colorScheme = ColorScheme.fromSeed(
    brightness: Brightness.dark,
    seedColor: background?.color ?? Colors.purple[800]!,
    primaryContainer: primary?.color,
    onPrimaryContainer: primary?.bodyTextColor,
    secondaryContainer: secondary?.color,
    onSecondaryContainer: secondary?.bodyTextColor,
    surface: background?.color,
    surfaceTint: vibrant?.color,
  );

  final hsv = HSVColor.fromColor(colorScheme.surface);
  final hsl = HSLColor.fromColor(colorScheme.surface);

  return base.copyWith(
    theme: ThemeData(
      colorScheme: colorScheme,
      useMaterial3: base.theme.useMaterial3,
      brightness: base.theme.brightness,
      cardTheme: base.theme.cardTheme,
    ),
    gradientHigh: colorScheme.surface,
    darkBackground: hsv.withValue(kDarkBackgroundValue).toColor(),
    darkerBackground: hsl.withLightness(kDarkerBackgroundLightness).toColor(),
    onDarkerBackground:
        hsl.withLightness(kOnDarkerBackgroundLightness).toColor(),
  );
}

@riverpod
ColorTheme baseTheme(BaseThemeRef ref) {
  final theme = ThemeData(
    useMaterial3: true,
    colorSchemeSeed: Colors.purple[800],
    brightness: Brightness.dark,
    cardTheme: CardTheme(
      clipBehavior: Clip.antiAlias,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(2),
      ),
    ),
  );

  final hsv = HSVColor.fromColor(theme.colorScheme.surface);
  final hsl = HSLColor.fromColor(theme.colorScheme.surface);

  return ColorTheme(
    theme: theme,
    gradientHigh: theme.colorScheme.surface,
    gradientLow: HSLColor.fromColor(theme.colorScheme.surface)
        .withLightness(0.06)
        .toColor(),
    darkBackground: hsv.withValue(kDarkBackgroundValue).toColor(),
    darkerBackground: hsl.withLightness(kDarkerBackgroundLightness).toColor(),
    onDarkerBackground:
        hsl.withLightness(kOnDarkerBackgroundLightness).toColor(),
  );
}

@riverpod
FutureOr<Palette> albumArtPalette(
  AlbumArtPaletteRef ref,
  String id,
) async {
  final album = await ref.watch(albumProvider(id).future);
  final cache = ref.watch(cacheServiceProvider);
  final info = cache.albumArt(album, thumbnail: true);

  return Palette.fromPaletteGenerator(await _generateFromCache(info));
}

Future<PaletteGenerator> _generateFromCache(
  UriCacheInfo cacheInfo,
) async {
  var file = await cacheInfo.cacheManager.getSingleFile(
    cacheInfo.uri.toString(),
    key: cacheInfo.cacheKey,
  );

  final memoryImage = ResizeImage(
    MemoryImage(await file.readAsBytes()),
    height: 32,
    width: 32,
  );
  final stream = memoryImage.resolve(const ImageConfiguration(
    devicePixelRatio: 1.0,
  ));

  Completer<ui.Image> imageCompleter = Completer<ui.Image>();
  late ImageStreamListener listener;
  listener = ImageStreamListener((ImageInfo info, bool synchronousCall) {
    stream.removeListener(listener);
    imageCompleter.complete(info.image);
  });

  stream.addListener(listener);
  final image = await imageCompleter.future;
  final byteData = (await image.toByteData())!;
  final height = image.height;
  final width = image.width;

  return await Executor().execute(
    arg1: byteData,
    arg2: height,
    arg3: width,
    fun3: _computePalette,
  );
}

Future<PaletteGenerator> _computePalette(
  ByteData byteData,
  int height,
  int width,
  TypeSendPort port,
) async {
  return await PaletteGenerator.fromByteData(
    EncodedImage(byteData, height: height, width: width),
  );
}

@riverpod
FutureOr<Palette> playlistArtPalette(
  PlaylistArtPaletteRef ref,
  String id,
) async {
  final playlist = await ref.watch(playlistProvider(id).future);
  final cache = ref.watch(cacheServiceProvider);
  final info = cache.playlistArt(playlist, thumbnail: true);

  return Palette.fromPaletteGenerator(await _generateFromCache(info));
}

@riverpod
FutureOr<Palette> mediaItemPalette(MediaItemPaletteRef ref) async {
  final item = ref.watch(mediaItemProvider).valueOrNull;
  final itemData = ref.watch(mediaItemDataProvider);
  final imageCache = ref.watch(imageCacheProvider);

  if (item == null || item.artUri == null || itemData == null) {
    return const Palette();
  }

  return Palette.fromPaletteGenerator(await _generateFromCache(
    UriCacheInfo(
      uri: item.artUri!,
      cacheKey: itemData.artCache!.thumbnailArtCacheKey,
      cacheManager: imageCache,
    ),
  ));
}

@riverpod
FutureOr<ColorTheme> mediaItemTheme(MediaItemThemeRef ref) async {
  final palette = await ref.watch(mediaItemPaletteProvider.future);
  return ref.watch(_colorThemeProvider(palette));
}

@riverpod
FutureOr<ColorTheme> albumArtTheme(AlbumArtThemeRef ref, String id) async {
  final palette = await ref.watch(albumArtPaletteProvider(id).future);
  return ref.watch(_colorThemeProvider(palette));
}

@riverpod
FutureOr<ColorTheme> playlistArtTheme(
  PlaylistArtThemeRef ref,
  String id,
) async {
  final palette = await ref.watch(playlistArtPaletteProvider(id).future);
  return ref.watch(_colorThemeProvider(palette));
}
