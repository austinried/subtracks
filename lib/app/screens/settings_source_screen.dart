import 'package:drift/drift.dart' show Value;
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:go_router/go_router.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../database/dao/sources_dao.dart';
import '../../database/database.dart';
import '../../l10n/generated/app_localizations.dart';
import '../../util/logger.dart';
import '../state/database.dart';
import '../util/padding.dart';

class SettingsSourceScreen extends HookConsumerWidget {
  const SettingsSourceScreen({
    super.key,
    this.id,
  });

  final int? id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final db = ref.watch(databaseProvider);

    final getSource = useMemoized(
      () async => id != null
          ? (result: await db.sourcesDao.getSource(id!).getSingle())
          : await Future.value((result: null)),
      [id],
    );
    final sourceResult = useFuture(getSource).data;

    if (sourceResult == null) {
      return Scaffold();
    }

    return _SettingsSourceScreen(source: sourceResult.result);
  }
}

class _SettingsSourceScreen extends HookConsumerWidget {
  const _SettingsSourceScreen({
    required this.source,
  });

  final SourceSetting? source;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    final textTheme = TextTheme.of(context);
    final colorScheme = ColorScheme.of(context);

    final form = useState(GlobalKey<FormState>()).value;
    final isSaving = useState(false);
    final isDeleting = useState(false);

    final nameController = useTextEditingController(text: source?.source.name);
    final addressController = useTextEditingController(
      text: source?.subsonicSetting.address.toString(),
    );
    final usernameController = useTextEditingController(
      text: source?.subsonicSetting.username,
    );
    final passwordController = useTextEditingController(
      text: source?.subsonicSetting.password,
    );
    final forcePlaintextPassword = useState(
      !(source?.subsonicSetting.useTokenAuth ?? true),
    );

    final name = LabeledTextField(
      label: l.settingsServersFieldsName,
      controller: nameController,
      required: true,
    );

    final address = LabeledTextField(
      label: l.settingsServersFieldsAddress,
      controller: addressController,
      keyboardType: TextInputType.url,
      autofillHints: const [AutofillHints.url],
      required: true,
      validator: (value, label) {
        final uri = Uri.tryParse(value ?? '');
        if (uri?.isAbsolute != true || uri?.host.isNotEmpty != true) {
          return '$label must be a valid URL';
        }

        return null;
      },
    );

    final username = LabeledTextField(
      label: l.settingsServersFieldsUsername,
      controller: usernameController,
      autofillHints: const [AutofillHints.username],
      required: true,
    );

    final password = LabeledTextField(
      label: l.settingsServersFieldsPassword,
      controller: passwordController,
      autofillHints: const [AutofillHints.password],
      obscureText: true,
      required: true,
    );

    final forcePlaintextSwitch = SwitchListTile(
      value: forcePlaintextPassword.value,
      title: Text(l.settingsServersOptionsForcePlaintextPasswordTitle),
      subtitle: forcePlaintextPassword.value
          ? Text(l.settingsServersOptionsForcePlaintextPasswordDescriptionOn)
          : Text(l.settingsServersOptionsForcePlaintextPasswordDescriptionOff),
      onChanged: (value) => forcePlaintextPassword.value = value,
    );

    return PopScope(
      canPop: !isSaving.value && !isDeleting.value,
      child: Scaffold(
        appBar: AppBar(
          title: Text(
            source == null
                ? l.settingsServersActionsAdd
                : l.settingsServersActionsEdit,
            style: textTheme.headlineLarge,
          ),
        ),
        floatingActionButton: Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: [
            if (source != null && source?.source.isActive != true)
              FloatingActionButton(
                backgroundColor: colorScheme.tertiaryContainer,
                foregroundColor: colorScheme.onTertiaryContainer,
                onPressed: !isSaving.value && !isDeleting.value
                    ? () async {
                        try {
                          isDeleting.value = true;
                          await ref
                              .read(databaseProvider)
                              .sourcesDao
                              .deleteSource(source!.source.id);
                        } finally {
                          isDeleting.value = false;
                        }

                        if (context.mounted) {
                          context.pop();
                        }
                      }
                    : null,
                child: isDeleting.value
                    ? SizedBox(
                        height: 24,
                        width: 24,
                        child: CircularProgressIndicator(
                          color: colorScheme.onTertiaryContainer,
                        ),
                      )
                    : const Icon(Icons.delete_forever_rounded),
              ),
            const SizedBox(width: 12),
            FloatingActionButton.extended(
              heroTag: null,
              icon: isSaving.value
                  ? const SizedBox(
                      height: 24,
                      width: 24,
                      child: CircularProgressIndicator(),
                    )
                  : const Icon(Icons.save_rounded),
              label: Text(l.settingsServersActionsSave),
              onPressed: !isSaving.value && !isDeleting.value
                  ? () async {
                      if (!form.currentState!.validate()) {
                        return;
                      }

                      var error = false;
                      try {
                        isSaving.value = true;
                        if (source != null) {
                          await ref
                              .read(databaseProvider)
                              .sourcesDao
                              .updateSource(
                                source!.source.copyWith(
                                  name: nameController.text,
                                ),
                                source!.subsonicSetting.copyWith(
                                  address: Uri.parse(addressController.text),
                                  username: usernameController.text,
                                  password: passwordController.text,
                                  useTokenAuth: !forcePlaintextPassword.value,
                                ),
                              );
                        } else {
                          await ref
                              .read(databaseProvider)
                              .sourcesDao
                              .createSource(
                                SourcesCompanion.insert(
                                  name: nameController.text,
                                ),
                                SubsonicSettingsCompanion.insert(
                                  address: Uri.parse(addressController.text),
                                  username: usernameController.text,
                                  password: passwordController.text,
                                  useTokenAuth: Value(
                                    !forcePlaintextPassword.value,
                                  ),
                                ),
                              );
                        }
                      } catch (e, _) {
                        // showErrorSnackbar(context, e.toString());
                        // log.severe('Saving source', e, st);
                        logger.w('fuck');
                        error = true;
                      } finally {
                        isSaving.value = false;
                      }

                      if (!error && context.mounted) {
                        context.pop();
                      }
                    }
                  : null,
            ),
          ],
        ),
        body: Form(
          key: form,
          child: AutofillGroup(
            child: ListView(
              children: [
                name,
                address,
                username,
                password,
                const SizedBox(height: 24),
                forcePlaintextSwitch,
                const FabPadding(),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class LabeledTextField extends HookConsumerWidget {
  const LabeledTextField({
    super.key,
    required this.label,
    required this.controller,
    this.obscureText = false,
    this.keyboardType,
    this.validator,
    this.autofillHints,
    this.required = false,
  });

  final String label;
  final TextEditingController controller;
  final bool obscureText;
  final bool required;
  final TextInputType? keyboardType;
  final Iterable<String>? autofillHints;
  final String? Function(String? value, String label)? validator;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final textTheme = TextTheme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const SizedBox(height: 32),
          Text(label, style: textTheme.titleMedium),
          TextFormField(
            controller: controller,
            obscureText: obscureText,
            keyboardType: keyboardType,
            autofillHints: autofillHints,
            validator: (value) {
              if (required) {
                if (value == null || value.isEmpty) {
                  return '$label is required';
                }
              }

              if (validator != null) {
                return validator!(value, label);
              }

              return null;
            },
          ),
        ],
      ),
    );
  }
}
