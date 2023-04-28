// ignore_for_file: implementation_imports

import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:flutter_cache_manager/src/storage/file_system/file_system_io.dart';
import 'package:http/http.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../http/client.dart';

part 'image_cache.g.dart';

CacheManager _openImageCache(BaseClient httpClient) {
  const key = 'images';
  return CacheManager(
    Config(
      key,
      stalePeriod: const Duration(days: 2147483647),
      maxNrOfCacheObjects: 2147483647,
      repo: JsonCacheInfoRepository(databaseName: key),
      fileSystem: IOFileSystem(key),
      fileService: HttpFileService(httpClient: httpClient),
    ),
  );
}

@Riverpod(keepAlive: true)
CacheManager imageCache(ImageCacheRef ref) {
  final http = ref.watch(httpClientProvider);
  return _openImageCache(http);
}
