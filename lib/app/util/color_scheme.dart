/// This file is a fork of the built-in [ColorScheme.fromImageProvider] function
/// with the following changes:
///
/// 1. [_extractColorsFromImageProvider] now runs the Quantizer to extract colors
/// in an isolate to prevent jank on the UI thread (especially noticable during
/// transitions).
///
/// 2. [_imageProviderToScaled] has its hard-coded image resize max dimensions
/// to 32x32 pixels.
library;

import 'dart:async';
import 'dart:isolate';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:material_color_utilities/material_color_utilities.dart';

/// Generate a [ColorScheme] derived from the given `imageProvider`.
///
/// Material Color Utilities extracts the dominant color from the
/// supplied [ImageProvider]. Using this color, a [ColorScheme] is generated
/// with harmonious colors that meet contrast requirements for accessibility.
///
/// If any of the optional color parameters are non-null, they will be
/// used in place of the generated colors for that field in the resulting
/// [ColorScheme]. This allows apps to override specific colors for their
/// needs.
///
/// Given the nature of the algorithm, the most dominant color of the
/// `imageProvider` may not wind up as one of the [ColorScheme] colors.
///
/// The provided image will be scaled down to a maximum size of 112x112 pixels
/// during color extraction.
///
/// {@tool dartpad}
/// This sample shows how to use [ColorScheme.fromImageProvider] to create
/// content-based dynamic color schemes.
///
/// ** See code in examples/api/lib/material/color_scheme/dynamic_content_color.0.dart **
/// {@end-tool}
///
/// See also:
///
///  * [M3 Guidelines: Dynamic color from content](https://m3.material.io/styles/color/dynamic-color/user-generated-color#8af550b9-a19e-4e9f-bb0a-7f611fed5d0f)
///  * <https://pub.dev/packages/dynamic_color>, a package to create
///    [ColorScheme]s based on a platform's implementation of dynamic color.
///  * <https://m3.material.io/styles/color/the-color-system/color-roles>, the
///    Material 3 Color system specification.
///  * <https://pub.dev/packages/material_color_utilities>, the package
///    used to algorithmically determine the dominant color and to generate
///    the [ColorScheme].
Future<ColorScheme> colorSchemefromImageProvider({
  required ImageProvider provider,
  Brightness brightness = Brightness.light,
  DynamicSchemeVariant dynamicSchemeVariant = DynamicSchemeVariant.tonalSpot,
  double contrastLevel = 0.0,
  Color? primary,
  Color? onPrimary,
  Color? primaryContainer,
  Color? onPrimaryContainer,
  Color? primaryFixed,
  Color? primaryFixedDim,
  Color? onPrimaryFixed,
  Color? onPrimaryFixedVariant,
  Color? secondary,
  Color? onSecondary,
  Color? secondaryContainer,
  Color? onSecondaryContainer,
  Color? secondaryFixed,
  Color? secondaryFixedDim,
  Color? onSecondaryFixed,
  Color? onSecondaryFixedVariant,
  Color? tertiary,
  Color? onTertiary,
  Color? tertiaryContainer,
  Color? onTertiaryContainer,
  Color? tertiaryFixed,
  Color? tertiaryFixedDim,
  Color? onTertiaryFixed,
  Color? onTertiaryFixedVariant,
  Color? error,
  Color? onError,
  Color? errorContainer,
  Color? onErrorContainer,
  Color? outline,
  Color? outlineVariant,
  Color? surface,
  Color? onSurface,
  Color? surfaceDim,
  Color? surfaceBright,
  Color? surfaceContainerLowest,
  Color? surfaceContainerLow,
  Color? surfaceContainer,
  Color? surfaceContainerHigh,
  Color? surfaceContainerHighest,
  Color? onSurfaceVariant,
  Color? inverseSurface,
  Color? onInverseSurface,
  Color? inversePrimary,
  Color? shadow,
  Color? scrim,
  Color? surfaceTint,
  @Deprecated(
    'Use surface instead. '
    'This feature was deprecated after v3.18.0-0.1.pre.',
  )
  Color? background,
  @Deprecated(
    'Use onSurface instead. '
    'This feature was deprecated after v3.18.0-0.1.pre.',
  )
  Color? onBackground,
  @Deprecated(
    'Use surfaceContainerHighest instead. '
    'This feature was deprecated after v3.18.0-0.1.pre.',
  )
  Color? surfaceVariant,
}) async {
  // Extract dominant colors from image.
  final QuantizerResult quantizerResult = await _extractColorsFromImageProvider(
    provider,
  );
  final Map<int, int> colorToCount = quantizerResult.colorToCount.map(
    (int key, int value) => MapEntry<int, int>(_getArgbFromAbgr(key), value),
  );

  // Score colors for color scheme suitability.
  final List<int> scoredResults = Score.score(colorToCount, desired: 1);
  final ui.Color baseColor = Color(scoredResults.first);

  final DynamicScheme scheme = _buildDynamicScheme(
    brightness,
    baseColor,
    dynamicSchemeVariant,
    contrastLevel,
  );

  return ColorScheme(
    primary: primary ?? Color(MaterialDynamicColors.primary.getArgb(scheme)),
    onPrimary:
        onPrimary ?? Color(MaterialDynamicColors.onPrimary.getArgb(scheme)),
    primaryContainer:
        primaryContainer ??
        Color(MaterialDynamicColors.primaryContainer.getArgb(scheme)),
    onPrimaryContainer:
        onPrimaryContainer ??
        Color(MaterialDynamicColors.onPrimaryContainer.getArgb(scheme)),
    primaryFixed:
        primaryFixed ??
        Color(MaterialDynamicColors.primaryFixed.getArgb(scheme)),
    primaryFixedDim:
        primaryFixedDim ??
        Color(MaterialDynamicColors.primaryFixedDim.getArgb(scheme)),
    onPrimaryFixed:
        onPrimaryFixed ??
        Color(MaterialDynamicColors.onPrimaryFixed.getArgb(scheme)),
    onPrimaryFixedVariant:
        onPrimaryFixedVariant ??
        Color(MaterialDynamicColors.onPrimaryFixedVariant.getArgb(scheme)),
    secondary:
        secondary ?? Color(MaterialDynamicColors.secondary.getArgb(scheme)),
    onSecondary:
        onSecondary ?? Color(MaterialDynamicColors.onSecondary.getArgb(scheme)),
    secondaryContainer:
        secondaryContainer ??
        Color(MaterialDynamicColors.secondaryContainer.getArgb(scheme)),
    onSecondaryContainer:
        onSecondaryContainer ??
        Color(MaterialDynamicColors.onSecondaryContainer.getArgb(scheme)),
    secondaryFixed:
        secondaryFixed ??
        Color(MaterialDynamicColors.secondaryFixed.getArgb(scheme)),
    secondaryFixedDim:
        secondaryFixedDim ??
        Color(MaterialDynamicColors.secondaryFixedDim.getArgb(scheme)),
    onSecondaryFixed:
        onSecondaryFixed ??
        Color(MaterialDynamicColors.onSecondaryFixed.getArgb(scheme)),
    onSecondaryFixedVariant:
        onSecondaryFixedVariant ??
        Color(MaterialDynamicColors.onSecondaryFixedVariant.getArgb(scheme)),
    tertiary: tertiary ?? Color(MaterialDynamicColors.tertiary.getArgb(scheme)),
    onTertiary:
        onTertiary ?? Color(MaterialDynamicColors.onTertiary.getArgb(scheme)),
    tertiaryContainer:
        tertiaryContainer ??
        Color(MaterialDynamicColors.tertiaryContainer.getArgb(scheme)),
    onTertiaryContainer:
        onTertiaryContainer ??
        Color(MaterialDynamicColors.onTertiaryContainer.getArgb(scheme)),
    tertiaryFixed:
        tertiaryFixed ??
        Color(MaterialDynamicColors.tertiaryFixed.getArgb(scheme)),
    tertiaryFixedDim:
        tertiaryFixedDim ??
        Color(MaterialDynamicColors.tertiaryFixedDim.getArgb(scheme)),
    onTertiaryFixed:
        onTertiaryFixed ??
        Color(MaterialDynamicColors.onTertiaryFixed.getArgb(scheme)),
    onTertiaryFixedVariant:
        onTertiaryFixedVariant ??
        Color(MaterialDynamicColors.onTertiaryFixedVariant.getArgb(scheme)),
    error: error ?? Color(MaterialDynamicColors.error.getArgb(scheme)),
    onError: onError ?? Color(MaterialDynamicColors.onError.getArgb(scheme)),
    errorContainer:
        errorContainer ??
        Color(MaterialDynamicColors.errorContainer.getArgb(scheme)),
    onErrorContainer:
        onErrorContainer ??
        Color(MaterialDynamicColors.onErrorContainer.getArgb(scheme)),
    outline: outline ?? Color(MaterialDynamicColors.outline.getArgb(scheme)),
    outlineVariant:
        outlineVariant ??
        Color(MaterialDynamicColors.outlineVariant.getArgb(scheme)),
    surface: surface ?? Color(MaterialDynamicColors.surface.getArgb(scheme)),
    surfaceDim:
        surfaceDim ?? Color(MaterialDynamicColors.surfaceDim.getArgb(scheme)),
    surfaceBright:
        surfaceBright ??
        Color(MaterialDynamicColors.surfaceBright.getArgb(scheme)),
    surfaceContainerLowest:
        surfaceContainerLowest ??
        Color(MaterialDynamicColors.surfaceContainerLowest.getArgb(scheme)),
    surfaceContainerLow:
        surfaceContainerLow ??
        Color(MaterialDynamicColors.surfaceContainerLow.getArgb(scheme)),
    surfaceContainer:
        surfaceContainer ??
        Color(MaterialDynamicColors.surfaceContainer.getArgb(scheme)),
    surfaceContainerHigh:
        surfaceContainerHigh ??
        Color(MaterialDynamicColors.surfaceContainerHigh.getArgb(scheme)),
    surfaceContainerHighest:
        surfaceContainerHighest ??
        Color(MaterialDynamicColors.surfaceContainerHighest.getArgb(scheme)),
    onSurface:
        onSurface ?? Color(MaterialDynamicColors.onSurface.getArgb(scheme)),
    onSurfaceVariant:
        onSurfaceVariant ??
        Color(MaterialDynamicColors.onSurfaceVariant.getArgb(scheme)),
    inverseSurface:
        inverseSurface ??
        Color(MaterialDynamicColors.inverseSurface.getArgb(scheme)),
    onInverseSurface:
        onInverseSurface ??
        Color(MaterialDynamicColors.inverseOnSurface.getArgb(scheme)),
    inversePrimary:
        inversePrimary ??
        Color(MaterialDynamicColors.inversePrimary.getArgb(scheme)),
    shadow: shadow ?? Color(MaterialDynamicColors.shadow.getArgb(scheme)),
    scrim: scrim ?? Color(MaterialDynamicColors.scrim.getArgb(scheme)),
    surfaceTint:
        surfaceTint ?? Color(MaterialDynamicColors.primary.getArgb(scheme)),
    brightness: brightness,
    // DEPRECATED (newest deprecations at the bottom)
    // ignore: deprecated_member_use
    background:
        background ?? Color(MaterialDynamicColors.background.getArgb(scheme)),
    // ignore: deprecated_member_use
    onBackground:
        onBackground ??
        Color(MaterialDynamicColors.onBackground.getArgb(scheme)),
    // ignore: deprecated_member_use
    surfaceVariant:
        surfaceVariant ??
        Color(MaterialDynamicColors.surfaceVariant.getArgb(scheme)),
  );
}

// ColorScheme.fromImageProvider() utilities.

/// Extracts bytes from an [ImageProvider] and returns a [QuantizerResult]
/// containing the most dominant colors.
Future<QuantizerResult> _extractColorsFromImageProvider(
  ImageProvider imageProvider,
) async {
  final ui.Image scaledImage = await _imageProviderToScaled(imageProvider);
  final ByteData? imageBytes = await scaledImage.toByteData();

  return Isolate.run(
    () => QuantizerCelebi().quantize(
      imageBytes!.buffer.asUint32List(),
      128,
      returnInputPixelToClusterPixel: true,
    ),
  );
}

/// Scale image size down to reduce computation time of color extraction.
Future<ui.Image> _imageProviderToScaled(ImageProvider imageProvider) async {
  const double maxDimension = 32.0;
  final ImageStream stream = imageProvider.resolve(
    const ImageConfiguration(size: Size(maxDimension, maxDimension)),
  );
  final Completer<ui.Image> imageCompleter = Completer<ui.Image>();
  late ImageStreamListener listener;
  late ui.Image scaledImage;
  Timer? loadFailureTimeout;

  listener = ImageStreamListener(
    (ImageInfo info, bool sync) async {
      loadFailureTimeout?.cancel();
      stream.removeListener(listener);
      final ui.Image image = info.image;
      final int width = image.width;
      final int height = image.height;
      double paintWidth = width.toDouble();
      double paintHeight = height.toDouble();
      assert(width > 0 && height > 0);

      final bool rescale = width > maxDimension || height > maxDimension;
      if (rescale) {
        paintWidth = (width > height)
            ? maxDimension
            : (maxDimension / height) * width;
        paintHeight = (height > width)
            ? maxDimension
            : (maxDimension / width) * height;
      }
      final ui.PictureRecorder pictureRecorder = ui.PictureRecorder();
      final Canvas canvas = Canvas(pictureRecorder);
      paintImage(
        canvas: canvas,
        rect: Rect.fromLTRB(0, 0, paintWidth, paintHeight),
        image: image,
        filterQuality: FilterQuality.none,
      );

      final ui.Picture picture = pictureRecorder.endRecording();
      scaledImage = await picture.toImage(
        paintWidth.toInt(),
        paintHeight.toInt(),
      );
      imageCompleter.complete(info.image);
    },
    onError: (Object exception, StackTrace? stackTrace) {
      loadFailureTimeout?.cancel();
      stream.removeListener(listener);
      imageCompleter.completeError(
        Exception('Failed to render image: $exception'),
        stackTrace,
      );
    },
  );

  loadFailureTimeout = Timer(const Duration(seconds: 5), () {
    stream.removeListener(listener);
    imageCompleter.completeError(
      TimeoutException('Timeout occurred trying to load image'),
    );
  });

  stream.addListener(listener);
  await imageCompleter.future;
  return scaledImage;
}

/// Converts AABBGGRR color int to AARRGGBB format.
int _getArgbFromAbgr(int abgr) {
  const int exceptRMask = 0xFF00FFFF;
  const int onlyRMask = ~exceptRMask;
  const int exceptBMask = 0xFFFFFF00;
  const int onlyBMask = ~exceptBMask;
  final int r = (abgr & onlyRMask) >> 16;
  final int b = abgr & onlyBMask;
  return (abgr & exceptRMask & exceptBMask) | (b << 16) | r;
}

DynamicScheme _buildDynamicScheme(
  Brightness brightness,
  Color seedColor,
  DynamicSchemeVariant schemeVariant,
  double contrastLevel,
) {
  assert(
    contrastLevel >= -1.0 && contrastLevel <= 1.0,
    'contrastLevel must be between -1.0 and 1.0 inclusive.',
  );
  final bool isDark = brightness == Brightness.dark;
  // ignore: deprecated_member_use
  final Hct sourceColor = Hct.fromInt(seedColor.value);
  return switch (schemeVariant) {
    DynamicSchemeVariant.tonalSpot => SchemeTonalSpot(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
    DynamicSchemeVariant.fidelity => SchemeFidelity(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
    DynamicSchemeVariant.content => SchemeContent(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
    DynamicSchemeVariant.monochrome => SchemeMonochrome(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
    DynamicSchemeVariant.neutral => SchemeNeutral(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
    DynamicSchemeVariant.vibrant => SchemeVibrant(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
    DynamicSchemeVariant.expressive => SchemeExpressive(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
    DynamicSchemeVariant.rainbow => SchemeRainbow(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
    DynamicSchemeVariant.fruitSalad => SchemeFruitSalad(
      sourceColorHct: sourceColor,
      isDark: isDark,
      contrastLevel: contrastLevel,
    ),
  };
}
