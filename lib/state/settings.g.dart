// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'settings.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$musicSourceHash() => r'466e2654eab8518c9e40c8c2c08a2ecb331b0a7f';

/// See also [musicSource].
@ProviderFor(musicSource)
final musicSourceProvider = Provider<MusicSource>.internal(
  musicSource,
  name: r'musicSourceProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$musicSourceHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef MusicSourceRef = ProviderRef<MusicSource>;
String _$networkModeHash() => r'813a60a454c6acaefbe3b56bf0152497ab18dcce';

/// See also [networkMode].
@ProviderFor(networkMode)
final networkModeProvider = StreamProvider<NetworkMode>.internal(
  networkMode,
  name: r'networkModeProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$networkModeHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef NetworkModeRef = StreamProviderRef<NetworkMode>;
String _$maxBitrateHash() => r'ec02d3ccbc9f3429acfc1b3f191cab791b1191e0';

/// See also [maxBitrate].
@ProviderFor(maxBitrate)
final maxBitrateProvider = FutureProvider<int>.internal(
  maxBitrate,
  name: r'maxBitrateProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$maxBitrateHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef MaxBitrateRef = FutureProviderRef<int>;
String _$sourceIdHash() => r'66ed4717b4a07548f5e25a42aeac2027aeab9b9c';

/// See also [sourceId].
@ProviderFor(sourceId)
final sourceIdProvider = Provider<int>.internal(
  sourceId,
  name: r'sourceIdProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$sourceIdHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef SourceIdRef = ProviderRef<int>;
String _$offlineModeHash() => r'b84cdece48d97c69e995fbaea97febb128cfc20a';

/// See also [OfflineMode].
@ProviderFor(OfflineMode)
final offlineModeProvider = NotifierProvider<OfflineMode, bool>.internal(
  OfflineMode.new,
  name: r'offlineModeProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$offlineModeHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$OfflineMode = Notifier<bool>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member
