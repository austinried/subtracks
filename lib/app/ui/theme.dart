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

  final text = theme.textTheme.copyWith(
    headlineLarge: theme.textTheme.headlineLarge?.copyWith(
      fontWeight: FontWeight.w800,
    ),
    headlineMedium: theme.textTheme.headlineMedium?.copyWith(
      fontWeight: FontWeight.w700,
    ),
    headlineSmall: theme.textTheme.headlineSmall?.copyWith(
      fontWeight: FontWeight.w600,
    ),
  );

  return theme.copyWith(
    textTheme: text,
  );
}
