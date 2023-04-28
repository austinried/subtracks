import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../http/client.dart';
import '../models/settings.dart';
import '../services/settings_service.dart';
import '../sources/music_source.dart';
import '../sources/subsonic/source.dart';

part 'settings.g.dart';

@Riverpod(keepAlive: true)
MusicSource musicSource(MusicSourceRef ref) {
  final settings = ref.watch(settingsServiceProvider.select(
    (value) => value.activeSource,
  )) as SubsonicSettings;
  final streamFormat = ref.watch(settingsServiceProvider.select(
    (value) => value.app.streamFormat,
  ));
  final maxBitrate = ref.watch(maxBitrateProvider).requireValue;
  final http = ref.watch(httpClientProvider);

  return MusicSource(
    SubsonicSource(
      opt: settings,
      http: http,
      maxBitrate: maxBitrate,
      streamFormat: streamFormat,
    ),
    ref,
  );
}

@Riverpod(keepAlive: true)
Stream<NetworkMode> networkMode(NetworkModeRef ref) async* {
  await for (var state in Connectivity().onConnectivityChanged) {
    switch (state) {
      case ConnectivityResult.wifi:
      case ConnectivityResult.ethernet:
        yield NetworkMode.wifi;
        break;
      default:
        yield NetworkMode.mobile;
        break;
    }
  }
}

@Riverpod(keepAlive: true)
Future<int> maxBitrate(MaxBitrateRef ref) async {
  final settings = ref.watch(settingsServiceProvider.select(
    (value) => value.app,
  ));
  final networkMode = ref.watch(networkModeProvider).requireValue;

  return networkMode == NetworkMode.wifi
      ? settings.maxBitrateWifi
      : settings.maxBitrateMobile;
}

@Riverpod(keepAlive: true)
int sourceId(SourceIdRef ref) {
  return ref.watch(musicSourceProvider.select((value) => value.id));
}

@Riverpod(keepAlive: true)
class OfflineMode extends _$OfflineMode {
  @override
  bool build() {
    return false;
  }

  Future<void> setMode(bool value) async {
    final hasSource = ref.read(settingsServiceProvider.select(
      (value) => value.activeSource != null,
    ));
    if (!hasSource) return;

    if (value == false && state == true) {
      try {
        await ref.read(musicSourceProvider).ping();
      } catch (err) {
        return;
      }
    }
    state = value;
  }
}
