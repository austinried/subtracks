import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:package_info_plus/package_info_plus.dart';

final sourceIdProvider = Provider<int>((ref) {
  return 1;
});

final packageInfoInitializer = FutureProvider<PackageInfo>((ref) {
  return PackageInfo.fromPlatform();
});

final packageInfoProvider = Provider<PackageInfo>((ref) {
  return ref.watch(packageInfoInitializer).requireValue;
});
