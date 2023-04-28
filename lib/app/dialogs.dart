import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/support.dart';
import '../state/theme.dart';

class DeleteDialog extends HookConsumerWidget {
  const DeleteDialog({
    super.key,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = ref.watch(baseThemeProvider);

    final l = AppLocalizations.of(context);

    return Theme(
      data: theme.theme,
      child: AlertDialog(
        title: Text(l.resourcesSongListDeleteAllTitle),
        content: Text(l.resourcesSongListDeleteAllContent),
        actions: [
          FilledButton.tonal(
            onPressed: () => Navigator.pop(context, false),
            child: Text(l.actionsCancel),
          ),
          FilledButton.icon(
            onPressed: () => Navigator.pop(context, true),
            label: Text(l.actionsDelete),
            icon: const Icon(Icons.delete_forever_rounded),
          ),
        ],
      ),
    );
  }
}

class MultipleChoiceDialog<T> extends HookConsumerWidget {
  final String title;
  final T current;
  final IList<MultiChoiceOption> options;

  const MultipleChoiceDialog({
    super.key,
    required this.title,
    required this.current,
    required this.options,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    final state = useState<T>(current);

    List<Widget> choices = [];
    for (var opt in options) {
      final value = opt.map(
        (value) => null,
        int: (value) => value.option,
        string: (value) => value.option,
      ) as T;
      choices.add(RadioListTile<T>(
        value: value,
        groupValue: state.value,
        title: Text(opt.title),
        onChanged: (value) => state.value = value as T,
      ));
    }

    return AlertDialog(
      title: Text(title),
      contentPadding: const EdgeInsets.symmetric(vertical: 20),
      content: Material(
        type: MaterialType.transparency,
        child: SingleChildScrollView(
          child: Column(children: choices),
        ),
      ),
      actions: [
        FilledButton.tonal(
          onPressed: () => Navigator.pop(context, null),
          child: Text(l.actionsCancel),
        ),
        FilledButton.icon(
          onPressed: () => Navigator.pop(context, state.value),
          label: Text(l.actionsOk),
          icon: const Icon(Icons.check_rounded),
        ),
      ],
    );
  }
}
