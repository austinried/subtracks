// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'library_songs_page.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$songsListHash() => r'a3149eb61f8f1ff326e9b1de0ac1c02d7baa831f';

/// Copied from Dart SDK
class _SystemHash {
  _SystemHash._();

  static int combine(int hash, int value) {
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + value);
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + ((0x0007ffff & hash) << 10));
    return hash ^ (hash >> 6);
  }

  static int finish(int hash) {
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + ((0x03ffffff & hash) << 3));
    // ignore: parameter_assignments
    hash = hash ^ (hash >> 11);
    return 0x1fffffff & (hash + ((0x00003fff & hash) << 15));
  }
}

typedef SongsListRef = AutoDisposeFutureProviderRef<List<Song>>;

/// See also [songsList].
@ProviderFor(songsList)
const songsListProvider = SongsListFamily();

/// See also [songsList].
class SongsListFamily extends Family<AsyncValue<List<Song>>> {
  /// See also [songsList].
  const SongsListFamily();

  /// See also [songsList].
  SongsListProvider call(
    ListQuery opt,
  ) {
    return SongsListProvider(
      opt,
    );
  }

  @override
  SongsListProvider getProviderOverride(
    covariant SongsListProvider provider,
  ) {
    return call(
      provider.opt,
    );
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'songsListProvider';
}

/// See also [songsList].
class SongsListProvider extends AutoDisposeFutureProvider<List<Song>> {
  /// See also [songsList].
  SongsListProvider(
    this.opt,
  ) : super.internal(
          (ref) => songsList(
            ref,
            opt,
          ),
          from: songsListProvider,
          name: r'songsListProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$songsListHash,
          dependencies: SongsListFamily._dependencies,
          allTransitiveDependencies: SongsListFamily._allTransitiveDependencies,
        );

  final ListQuery opt;

  @override
  bool operator ==(Object other) {
    return other is SongsListProvider && other.opt == opt;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, opt.hashCode);

    return _SystemHash.finish(hash);
  }
}
// ignore_for_file: unnecessary_raw_strings, subtype_of_sealed_class, invalid_use_of_internal_member, do_not_use_environment, prefer_const_constructors, public_member_api_docs, avoid_private_typedef_functions
