import 'dart:math';

import 'package:auto_route/auto_route.dart';
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:path/path.dart' as p;
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../log.dart';
import '../../models/support.dart';
import '../../services/settings_service.dart';
import '../../state/init.dart';
import '../../state/settings.dart';
import '../app_router.dart';
import '../dialogs.dart';

const kHorizontalPadding = 16.0;

class SettingsPage extends HookConsumerWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    // final downloads = ref.watch(downloadServiceProvider.select(
    //   (value) => value.downloads,
    // ));

    return Scaffold(
      body: ListView(
        children: [
          const SizedBox(height: 96),
          _SectionHeader(l.settingsServersName),
          const _Sources(),
          _SectionHeader(l.settingsNetworkName),
          const _Network(),
          _SectionHeader(l.settingsAboutName),
          _About(),
          // const _SectionHeader('Downloads'),
          // _Section(
          //   children: downloads
          //       .map(
          //         (e) => ListTile(
          //           isThreeLine: true,
          //           title: Text(e.filename ?? e.url),
          //           subtitle: Column(
          //             mainAxisAlignment: MainAxisAlignment.start,
          //             children: [
          //               Row(children: [Text('Progress: ${e.progress}%')]),
          //               Row(children: [Text('Status: ${e.status})')]),
          //               Text('Status: ${e.savedDir}'),
          //             ],
          //           ),
          //           trailing:
          //               CircularProgressIndicator(value: e.progress / 100),
          //         ),
          //       )
          //       .toList(),
          // ),
        ],
      ),
    );
  }
}

class _Section extends StatelessWidget {
  final List<Widget> children;

  const _Section({required this.children});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        ...children,
        const SizedBox(height: 32),
      ],
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;

  const _SectionHeader(this.title);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      children: [
        SizedBox(
          width: double.infinity,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: kHorizontalPadding),
            child: Text(
              title,
              style: theme.textTheme.displaySmall,
            ),
          ),
        ),
        const SizedBox(height: 12),
      ],
    );
  }
}

class _Network extends StatelessWidget {
  const _Network();

  @override
  Widget build(BuildContext context) {
    return const _Section(
      children: [
        _OfflineMode(),
        _MaxBitrateWifi(),
        _MaxBitrateMobile(),
        _StreamFormat(),
      ],
    );
  }
}

class _About extends HookConsumerWidget {
  _About();

  final _homepage = Uri.parse('https://github.com/austinried/subtracks');
  final _donate = Uri.parse('https://ko-fi.com/austinried');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);
    final pkg = ref.watch(packageInfoProvider).requireValue;

    return _Section(
      children: [
        ListTile(
          title: const Text('subtracks'),
          subtitle: Text(l.settingsAboutVersion(pkg.version)),
        ),
        ListTile(
          title: Text(l.settingsAboutActionsLicenses),
          // trailing: const Icon(Icons.open_in_new_rounded),
          onTap: () {},
        ),
        ListTile(
          title: Text(l.settingsAboutActionsProjectHomepage),
          subtitle: Text(_homepage.toString()),
          trailing: const Icon(Icons.open_in_new_rounded),
          onTap: () => launchUrl(
            _homepage,
            mode: LaunchMode.externalApplication,
          ),
        ),
        ListTile(
          title: Text(l.settingsAboutActionsSupport),
          subtitle: Text(_donate.toString()),
          trailing: const Icon(Icons.open_in_new_rounded),
          onTap: () => launchUrl(
            _donate,
            mode: LaunchMode.externalApplication,
          ),
        ),
        const SizedBox(height: 12),
        const _ShareLogsButton(),
      ],
    );
  }
}

class _ShareLogsButton extends StatelessWidget {
  const _ShareLogsButton();

  @override
  Widget build(BuildContext context) {
    final l = AppLocalizations.of(context);

    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        OutlinedButton.icon(
          icon: const Icon(Icons.share),
          label: Text(l.settingsAboutShareLogs),
          onPressed: () async {
            final files = await logFiles();
            if (files.isEmpty) return;

            if (!context.mounted) return;
            final value = await showDialog<String>(
              context: context,
              builder: (context) => MultipleChoiceDialog<String>(
                title: l.settingsAboutChooseLog,
                current: files.first.path,
                options: files
                    .map((e) => MultiChoiceOption.string(
                          title: p.basename(e.path),
                          option: e.path,
                        ))
                    .toIList(),
              ),
            );

            if (value == null) return;
            Share.shareXFiles(
              [XFile(value, mimeType: 'text/plain')],
              subject: 'Logs from subtracks: ${String.fromCharCodes(
                List.generate(8, (_) => Random().nextInt(26) + 65),
              )}',
            );
          },
        ),
      ],
    );
  }
}

class _MaxBitrateWifi extends HookConsumerWidget {
  const _MaxBitrateWifi();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bitrate = ref.watch(settingsServiceProvider.select(
      (value) => value.app.maxBitrateWifi,
    ));
    final l = AppLocalizations.of(context);

    return _MaxBitrateOption(
      title: l.settingsNetworkOptionsMaxBitrateWifiTitle,
      bitrate: bitrate,
      onChange: (value) {
        ref.read(settingsServiceProvider.notifier).setMaxBitrateWifi(value);
      },
    );
  }
}

class _MaxBitrateMobile extends HookConsumerWidget {
  const _MaxBitrateMobile();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bitrate = ref.watch(settingsServiceProvider.select(
      (value) => value.app.maxBitrateMobile,
    ));
    final l = AppLocalizations.of(context);

    return _MaxBitrateOption(
      title: l.settingsNetworkOptionsMaxBitrateMobileTitle,
      bitrate: bitrate,
      onChange: (value) {
        ref.read(settingsServiceProvider.notifier).setMaxBitrateMobile(value);
      },
    );
  }
}

class _MaxBitrateOption extends HookConsumerWidget {
  final String title;
  final int bitrate;
  final void Function(int value) onChange;

  const _MaxBitrateOption({
    required this.title,
    required this.bitrate,
    required this.onChange,
  });

  static const options = [0, 24, 32, 64, 96, 128, 192, 256, 320];

  String _bitrateText(AppLocalizations l, int bitrate) {
    return bitrate == 0
        ? l.settingsNetworkValuesUnlimitedKbps
        : l.settingsNetworkValuesKbps(bitrate.toString());
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l = AppLocalizations.of(context);

    return ListTile(
      title: Text(title),
      subtitle: Text(_bitrateText(l, bitrate)),
      onTap: () async {
        final value = await showDialog<int>(
          context: context,
          builder: (context) => MultipleChoiceDialog<int>(
            title: title,
            current: bitrate,
            options: options
                .map((opt) => MultiChoiceOption.int(
                      title: _bitrateText(l, opt),
                      option: opt,
                    ))
                .toIList(),
          ),
        );

        if (value != null) {
          onChange(value);
        }
      },
    );
  }
}

class _StreamFormat extends HookConsumerWidget {
  const _StreamFormat();

  static const options = ['', 'mp3', 'opus', 'ogg'];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final streamFormat = ref.watch(
      settingsServiceProvider.select((value) => value.app.streamFormat),
    );
    final l = AppLocalizations.of(context);

    return ListTile(
      title: Text(l.settingsNetworkOptionsStreamFormat),
      subtitle: Text(
        streamFormat ?? l.settingsNetworkOptionsStreamFormatServerDefault,
      ),
      onTap: () async {
        final value = await showDialog<String>(
          context: context,
          builder: (context) => MultipleChoiceDialog<String>(
            title: l.settingsNetworkOptionsStreamFormat,
            current: streamFormat ?? '',
            options: options
                .map((opt) => MultiChoiceOption.string(
                      title: opt == ''
                          ? l.settingsNetworkOptionsStreamFormatServerDefault
                          : opt,
                      option: opt,
                    ))
                .toIList(),
          ),
        );

        if (value != null) {
          ref
              .read(settingsServiceProvider.notifier)
              .setStreamFormat(value == '' ? null : value);
        }
      },
    );
  }
}

class _OfflineMode extends HookConsumerWidget {
  const _OfflineMode();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final offline = ref.watch(offlineModeProvider);
    final l = AppLocalizations.of(context);

    return SwitchListTile(
      value: offline,
      title: Text(l.settingsNetworkOptionsOfflineMode),
      subtitle: offline
          ? Text(l.settingsNetworkOptionsOfflineModeOn)
          : Text(l.settingsNetworkOptionsOfflineModeOff),
      onChanged: (value) {
        ref.read(offlineModeProvider.notifier).setMode(value);
      },
    );
  }
}

class _Sources extends HookConsumerWidget {
  const _Sources();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sources = ref.watch(settingsServiceProvider.select(
      (value) => value.sources,
    ));
    final activeSource = ref.watch(settingsServiceProvider.select(
      (value) => value.activeSource,
    ));

    final l = AppLocalizations.of(context);

    return _Section(
      children: [
        for (var source in sources)
          RadioListTile<int>(
            value: source.id,
            groupValue: activeSource?.id,
            onChanged: (value) {
              ref
                  .read(settingsServiceProvider.notifier)
                  .setActiveSource(source.id);
            },
            title: Text(source.name),
            subtitle: Text(
              source.address.toString(),
              maxLines: 1,
              softWrap: false,
              overflow: TextOverflow.fade,
            ),
            secondary: IconButton(
              icon: const Icon(Icons.edit_rounded),
              onPressed: () {
                context.pushRoute(SourceRoute(id: source.id));
              },
            ),
          ),
        const SizedBox(height: 8),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            OutlinedButton.icon(
              icon: const Icon(Icons.add_rounded),
              label: Text(l.settingsServersActionsAdd),
              onPressed: () {
                context.pushRoute(SourceRoute());
              },
            ),
          ],
        ),
        // TODO: remove
        if (kDebugMode)
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              OutlinedButton.icon(
                icon: const Icon(Icons.add_rounded),
                label: const Text('Add TEST'),
                onPressed: () {
                  ref
                      .read(settingsServiceProvider.notifier)
                      .addTestSource('TEST');
                },
              ),
            ],
          ),
      ],
    );
  }
}
