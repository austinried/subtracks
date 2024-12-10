import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class ShuffleFab extends StatelessWidget {
  final void Function()? onPressed;

  const ShuffleFab({
    super.key,
    this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    final l = AppLocalizations.of(context);

    return FloatingActionButton(
      heroTag: null,
      onPressed: onPressed,
      tooltip: l.actionsCancel,
      child: const Icon(Icons.shuffle_rounded),
    );
  }
}

class RadioPlayFab extends StatelessWidget {
  final void Function()? onPressed;

  const RadioPlayFab({
    super.key,
    this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return FloatingActionButton(
      heroTag: null,
      onPressed: onPressed,
      child: Stack(
        clipBehavior: Clip.none,
        children: [
          const Icon(Icons.radio_rounded),
          Positioned(
            bottom: -11,
            right: -10,
            child: Icon(
              Icons.play_arrow_rounded,
              color: theme.colorScheme.primaryContainer,
              size: 26,
            ),
          ),
          const Positioned(
            bottom: -6,
            right: -5,
            child: Icon(
              Icons.play_arrow_rounded,
              size: 16,
            ),
          ),
        ],
      ),
    );
  }
}
