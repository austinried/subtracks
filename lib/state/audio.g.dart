// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'audio.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$mediaItemHash() => r'ca8b6768872f17355f756c95cf85278127b59444';

/// See also [mediaItem].
@ProviderFor(mediaItem)
final mediaItemProvider = StreamProvider<MediaItem?>.internal(
  mediaItem,
  name: r'mediaItemProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$mediaItemHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef MediaItemRef = StreamProviderRef<MediaItem?>;
String _$mediaItemDataHash() => r'8539c02682f0d33b584ea0437dd3774d9f321a2e';

/// See also [mediaItemData].
@ProviderFor(mediaItemData)
final mediaItemDataProvider = Provider<MediaItemData?>.internal(
  mediaItemData,
  name: r'mediaItemDataProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$mediaItemDataHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef MediaItemDataRef = ProviderRef<MediaItemData?>;
String _$mediaItemSongHash() => r'274f43470cd993f0a2bed3d3da22d7bd41b562f1';

/// See also [mediaItemSong].
@ProviderFor(mediaItemSong)
final mediaItemSongProvider = StreamProvider<Song?>.internal(
  mediaItemSong,
  name: r'mediaItemSongProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$mediaItemSongHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef MediaItemSongRef = StreamProviderRef<Song?>;
String _$playbackStateHash() => r'b4a9eb7f802fc8c92666c1318f789865140d6025';

/// See also [playbackState].
@ProviderFor(playbackState)
final playbackStateProvider = StreamProvider<PlaybackState>.internal(
  playbackState,
  name: r'playbackStateProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$playbackStateHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef PlaybackStateRef = StreamProviderRef<PlaybackState>;
String _$queueModeHash() => r'be0b1ff436c367e9be54c6d15fd8bac4f904fdec';

/// See also [queueMode].
@ProviderFor(queueMode)
final queueModeProvider = StreamProvider<QueueMode>.internal(
  queueMode,
  name: r'queueModeProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$queueModeHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef QueueModeRef = StreamProviderRef<QueueMode>;
String _$queueHash() => r'94d86c99382f56193a11baf3f13354eab6a39fa8';

/// See also [queue].
@ProviderFor(queue)
final queueProvider = StreamProvider<List<MediaItem>>.internal(
  queue,
  name: r'queueProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$queueHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef QueueRef = StreamProviderRef<List<MediaItem>>;
String _$shuffleIndiciesHash() => r'e5dc6879b2a7b7a501b58aace717ff36eff59995';

/// See also [shuffleIndicies].
@ProviderFor(shuffleIndicies)
final shuffleIndiciesProvider = StreamProvider<List<int>?>.internal(
  shuffleIndicies,
  name: r'shuffleIndiciesProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$shuffleIndiciesHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef ShuffleIndiciesRef = StreamProviderRef<List<int>?>;
String _$positionStreamHash() => r'5f1dc9d11e1bcce649ddb764525cc0dc79bfb6d8';

/// See also [positionStream].
@ProviderFor(positionStream)
final positionStreamProvider = AutoDisposeStreamProvider<Duration>.internal(
  positionStream,
  name: r'positionStreamProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$positionStreamHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef PositionStreamRef = AutoDisposeStreamProviderRef<Duration>;
String _$playingHash() => r'2a40fa275358918b243c8734bbe49bc9d7373f10';

/// See also [playing].
@ProviderFor(playing)
final playingProvider = AutoDisposeProvider<bool>.internal(
  playing,
  name: r'playingProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$playingHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef PlayingRef = AutoDisposeProviderRef<bool>;
String _$processingStateHash() => r'b9e59927b905384a0f1221b2adb9b681091c27d1';

/// See also [processingState].
@ProviderFor(processingState)
final processingStateProvider =
    AutoDisposeProvider<AudioProcessingState?>.internal(
  processingState,
  name: r'processingStateProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$processingStateHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef ProcessingStateRef = AutoDisposeProviderRef<AudioProcessingState?>;
String _$positionHash() => r'bfc853fe9e46bf79522fa41374763a7e1c12e739';

/// See also [position].
@ProviderFor(position)
final positionProvider = AutoDisposeProvider<int>.internal(
  position,
  name: r'positionProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$positionHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef PositionRef = AutoDisposeProviderRef<int>;
String _$durationHash() => r'bf9eb316b8401401e5862384deb1a4c1134e6dd2';

/// See also [duration].
@ProviderFor(duration)
final durationProvider = AutoDisposeProvider<int>.internal(
  duration,
  name: r'durationProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$durationHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef DurationRef = AutoDisposeProviderRef<int>;
String _$shuffleModeHash() => r'fd26d81cb9bd5e0e1a7e9ccf1589c104d9a4eb3a';

/// See also [shuffleMode].
@ProviderFor(shuffleMode)
final shuffleModeProvider = Provider<AudioServiceShuffleMode?>.internal(
  shuffleMode,
  name: r'shuffleModeProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$shuffleModeHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef ShuffleModeRef = ProviderRef<AudioServiceShuffleMode?>;
String _$repeatModeHash() => r'346248bf08df65f1f69e4cb4b6ef192190d2910c';

/// See also [repeatMode].
@ProviderFor(repeatMode)
final repeatModeProvider = Provider<AudioServiceRepeatMode>.internal(
  repeatMode,
  name: r'repeatModeProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$repeatModeHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef RepeatModeRef = ProviderRef<AudioServiceRepeatMode>;
String _$lastAudioStateServiceHash() =>
    r'4291b8d4a399f1b192277a3e8a93fbe7096fea32';

/// See also [LastAudioStateService].
@ProviderFor(LastAudioStateService)
final lastAudioStateServiceProvider =
    AsyncNotifierProvider<LastAudioStateService, void>.internal(
  LastAudioStateService.new,
  name: r'lastAudioStateServiceProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$lastAudioStateServiceHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$LastAudioStateService = AsyncNotifier<void>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member
