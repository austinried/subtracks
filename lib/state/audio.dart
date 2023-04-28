import 'package:audio_service/audio_service.dart';
import 'package:drift/drift.dart' show Value;
import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../database/database.dart';
import '../models/music.dart';
import '../models/support.dart';
import '../services/audio_service.dart';
import 'settings.dart';

part 'audio.g.dart';

@Riverpod(keepAlive: true)
Stream<MediaItem?> mediaItem(MediaItemRef ref) async* {
  final audio = ref.watch(audioControlProvider);
  await for (var item in audio.mediaItem) {
    yield item;
  }
}

@Riverpod(keepAlive: true)
MediaItemData? mediaItemData(MediaItemDataRef ref) {
  return ref.watch(mediaItemProvider.select(
    (value) => value.valueOrNull?.data,
  ));
}

@Riverpod(keepAlive: true)
Stream<Song?> mediaItemSong(MediaItemSongRef ref) {
  final db = ref.watch(databaseProvider);
  final sourceId = ref.watch(sourceIdProvider);
  final id = ref.watch(mediaItemProvider.select(
    (value) => value.valueOrNull?.id,
  ));

  return db.songById(sourceId, id ?? '').watchSingleOrNull();
}

@Riverpod(keepAlive: true)
Stream<PlaybackState> playbackState(PlaybackStateRef ref) async* {
  final audio = ref.watch(audioControlProvider);
  await for (var state in audio.playbackState) {
    yield state;
  }
}

@Riverpod(keepAlive: true)
Stream<QueueMode> queueMode(QueueModeRef ref) async* {
  final audio = ref.watch(audioControlProvider);
  await for (var state in audio.queueMode) {
    yield state;
  }
}

@Riverpod(keepAlive: true)
Stream<List<MediaItem>> queue(QueueRef ref) async* {
  final audio = ref.watch(audioControlProvider);
  await for (var queue in audio.queue) {
    yield queue;
  }
}

@Riverpod(keepAlive: true)
Stream<List<int>?> shuffleIndicies(ShuffleIndiciesRef ref) async* {
  final audio = ref.watch(audioControlProvider);
  await for (var indicies in audio.shuffleIndicies) {
    yield indicies;
  }
}

@riverpod
Stream<Duration> positionStream(PositionStreamRef ref) async* {
  final audio = ref.watch(audioControlProvider);
  await for (var state in audio.position) {
    yield state;
  }
}

@riverpod
bool playing(PlayingRef ref) {
  return ref.watch(playbackStateProvider.select(
    (value) => value.valueOrNull?.playing ?? false,
  ));
}

@riverpod
AudioProcessingState? processingState(ProcessingStateRef ref) {
  return ref.watch(playbackStateProvider.select(
    (value) => value.valueOrNull?.processingState,
  ));
}

@riverpod
int position(PositionRef ref) {
  return ref.watch(positionStreamProvider.select(
    (value) => value.valueOrNull?.inSeconds ?? 0,
  ));
}

@riverpod
int duration(DurationRef ref) {
  return ref.watch(mediaItemProvider.select(
    (value) => value.valueOrNull?.duration?.inSeconds ?? 0,
  ));
}

@Riverpod(keepAlive: true)
AudioServiceShuffleMode? shuffleMode(ShuffleModeRef ref) {
  return ref.watch(playbackStateProvider.select(
    (value) => value.valueOrNull?.shuffleMode,
  ));
}

@Riverpod(keepAlive: true)
AudioServiceRepeatMode repeatMode(RepeatModeRef ref) {
  return ref.watch(playbackStateProvider.select(
    (value) => value.valueOrNull?.repeatMode ?? AudioServiceRepeatMode.none,
  ));
}

@Riverpod(keepAlive: true)
class LastAudioStateService extends _$LastAudioStateService {
  @override
  Future<void> build() async {
    final db = ref.watch(databaseProvider);
    final queueMode = ref.watch(queueModeProvider).valueOrNull;
    final shuffleIndicies = ref.watch(shuffleIndiciesProvider).valueOrNull;
    final repeat = ref.watch(repeatModeProvider);

    await db.saveLastAudioState(LastAudioStateCompanion.insert(
      id: const Value(1),
      queueMode: queueMode ?? QueueMode.user,
      shuffleIndicies: Value(shuffleIndicies?.lock),
      repeat: {
        AudioServiceRepeatMode.none: RepeatMode.none,
        AudioServiceRepeatMode.all: RepeatMode.all,
        AudioServiceRepeatMode.group: RepeatMode.all,
        AudioServiceRepeatMode.one: RepeatMode.one,
      }[repeat]!,
    ));
  }
}
