import 'package:flutter/material.dart';

ThemeData subtracksTheme([ColorScheme? colorScheme]) {
  final theme = ThemeData.from(
    colorScheme:
        colorScheme ??
        ColorScheme.fromSeed(
          seedColor: Colors.purple.shade800,
          brightness: Brightness.dark,
        ),
    useMaterial3: true,
  );

  final text = theme.textTheme;
  return theme.copyWith(
    textTheme: text.copyWith(
      headlineLarge: text.headlineLarge?.copyWith(
        fontWeight: FontWeight.w800,
      ),
      headlineMedium: text.headlineMedium?.copyWith(
        fontWeight: FontWeight.w700,
      ),
      headlineSmall: text.headlineSmall?.copyWith(
        fontWeight: FontWeight.w600,
      ),
    ),
  );
}
