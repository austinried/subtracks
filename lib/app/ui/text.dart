import 'package:flutter/material.dart';

class TextH1 extends StatelessWidget {
  const TextH1(
    this.data, {
    super.key,
  });

  final String data;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Text(
      data,
      style: theme.textTheme.headlineLarge?.copyWith(
        fontWeight: FontWeight.w800,
      ),
    );
  }
}

class TextH2 extends StatelessWidget {
  const TextH2(
    this.data, {
    super.key,
  });

  final String data;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Text(
      data,
      style: theme.textTheme.headlineMedium?.copyWith(
        fontWeight: FontWeight.w700,
      ),
    );
  }
}
