import 'dart:async';

import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../models/music.dart';
import '../../models/support.dart';
import '../../services/download_service.dart';
import '../../state/music.dart';
import '../../state/settings.dart';
import '../dialogs.dart';

enum DownloadActionType {
  download,
  cancel,
  delete,
}

class DownloadAction {
  final DownloadActionType type;
  final WidgetBuilder iconBuilder;
  final FutureOr<void> Function()? action;

  const DownloadAction({
    required this.type,
    required this.iconBuilder,
    this.action,
  });
}

List<DownloadAction> useAlbumDownloadActions({
  required BuildContext context,
  required WidgetRef ref,
  required Album album,
}) {
  final status = ref.watch(albumDownloadStatusProvider(album.id)).valueOrNull;

  return useListDownloadActions(
    context: context,
    ref: ref,
    list: album,
    status: status,
    onDownload: () =>
        ref.read(downloadServiceProvider.notifier).downloadAlbum(album),
    onDelete: () =>
        ref.read(downloadServiceProvider.notifier).deleteAlbum(album),
    onCancel: () =>
        ref.read(downloadServiceProvider.notifier).cancelAlbum(album),
  );
}

List<DownloadAction> usePlaylistDownloadActions({
  required BuildContext context,
  required WidgetRef ref,
  required Playlist playlist,
}) {
  final status =
      ref.watch(playlistDownloadStatusProvider(playlist.id)).valueOrNull;

  return useListDownloadActions(
    context: context,
    ref: ref,
    list: playlist,
    status: status,
    onDownload: () =>
        ref.read(downloadServiceProvider.notifier).downloadPlaylist(playlist),
    onDelete: () =>
        ref.read(downloadServiceProvider.notifier).deletePlaylist(playlist),
    onCancel: () =>
        ref.read(downloadServiceProvider.notifier).cancelPlaylist(playlist),
  );
}

List<DownloadAction> useListDownloadActions({
  required BuildContext context,
  required WidgetRef ref,
  required SourceIdentifiable list,
  required ListDownloadStatus? status,
  required FutureOr<void> Function() onDelete,
  required FutureOr<void> Function() onCancel,
  required FutureOr<void> Function() onDownload,
}) {
  status ??= const ListDownloadStatus(total: 0, downloaded: 0, downloading: 0);

  final sourceId = SourceId.from(list);
  final offline = ref.watch(offlineModeProvider);
  final listDownloadInProgress = ref.watch(downloadServiceProvider
      .select((value) => value.listDownloads.contains(sourceId)));
  final listDeleteInProgress = ref.watch(downloadServiceProvider
      .select((value) => value.deletes.contains(sourceId)));
  final listCancelInProgress = ref.watch(downloadServiceProvider
      .select((value) => value.listCancels.contains(sourceId)));

  DownloadAction delete() {
    return DownloadAction(
      type: DownloadActionType.delete,
      iconBuilder: (context) => const Icon(Icons.delete_forever_rounded),
      action: listDeleteInProgress
          ? null
          : () async {
              final ok = await showDialog<bool>(
                context: context,
                builder: (context) => const DeleteDialog(),
              );
              if (ok == true) {
                await onDelete();
              }
            },
    );
  }

  DownloadAction cancel() {
    return DownloadAction(
      type: DownloadActionType.cancel,
      iconBuilder: (context) => const Stack(
        alignment: Alignment.center,
        children: [
          Icon(Icons.cancel_rounded),
          SizedBox(
            height: 32,
            width: 32,
            child: CircularProgressIndicator(
              strokeWidth: 3,
            ),
          ),
        ],
      ),
      action: listCancelInProgress ? null : onCancel,
    );
  }

  DownloadAction download() {
    return DownloadAction(
      type: DownloadActionType.download,
      iconBuilder: (context) => const Icon(Icons.download_rounded),
      action: !offline ? onDownload : null,
    );
  }

  if (status.total == status.downloaded) {
    return [delete()];
  } else if (status.downloading == 0 && status.downloaded > 0) {
    return [download(), delete()];
  } else if (listDownloadInProgress || status.downloading > 0) {
    return [cancel()];
  } else {
    return [download()];
  }
}
