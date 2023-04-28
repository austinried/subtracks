// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'images.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$artistArtCacheInfoHash() =>
    r'f82d3e91aa1596939e376c6a7ea7d3e974c6f0fc';

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

typedef _ArtistArtCacheInfoRef = AutoDisposeProviderRef<CacheInfo>;

/// See also [_artistArtCacheInfo].
@ProviderFor(_artistArtCacheInfo)
const _artistArtCacheInfoProvider = _ArtistArtCacheInfoFamily();

/// See also [_artistArtCacheInfo].
class _ArtistArtCacheInfoFamily extends Family<CacheInfo> {
  /// See also [_artistArtCacheInfo].
  const _ArtistArtCacheInfoFamily();

  /// See also [_artistArtCacheInfo].
  _ArtistArtCacheInfoProvider call({
    required String artistId,
    bool thumbnail = true,
  }) {
    return _ArtistArtCacheInfoProvider(
      artistId: artistId,
      thumbnail: thumbnail,
    );
  }

  @override
  _ArtistArtCacheInfoProvider getProviderOverride(
    covariant _ArtistArtCacheInfoProvider provider,
  ) {
    return call(
      artistId: provider.artistId,
      thumbnail: provider.thumbnail,
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
  String? get name => r'_artistArtCacheInfoProvider';
}

/// See also [_artistArtCacheInfo].
class _ArtistArtCacheInfoProvider extends AutoDisposeProvider<CacheInfo> {
  /// See also [_artistArtCacheInfo].
  _ArtistArtCacheInfoProvider({
    required this.artistId,
    this.thumbnail = true,
  }) : super.internal(
          (ref) => _artistArtCacheInfo(
            ref,
            artistId: artistId,
            thumbnail: thumbnail,
          ),
          from: _artistArtCacheInfoProvider,
          name: r'_artistArtCacheInfoProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$artistArtCacheInfoHash,
          dependencies: _ArtistArtCacheInfoFamily._dependencies,
          allTransitiveDependencies:
              _ArtistArtCacheInfoFamily._allTransitiveDependencies,
        );

  final String artistId;
  final bool thumbnail;

  @override
  bool operator ==(Object other) {
    return other is _ArtistArtCacheInfoProvider &&
        other.artistId == artistId &&
        other.thumbnail == thumbnail;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, artistId.hashCode);
    hash = _SystemHash.combine(hash, thumbnail.hashCode);

    return _SystemHash.finish(hash);
  }
}

String _$artistArtCachedUrlHash() =>
    r'2a5e0fea614ff12a1d562faccec6cfe98394af42';
typedef _ArtistArtCachedUrlRef = AutoDisposeFutureProviderRef<String?>;

/// See also [_artistArtCachedUrl].
@ProviderFor(_artistArtCachedUrl)
const _artistArtCachedUrlProvider = _ArtistArtCachedUrlFamily();

/// See also [_artistArtCachedUrl].
class _ArtistArtCachedUrlFamily extends Family<AsyncValue<String?>> {
  /// See also [_artistArtCachedUrl].
  const _ArtistArtCachedUrlFamily();

  /// See also [_artistArtCachedUrl].
  _ArtistArtCachedUrlProvider call({
    required String artistId,
    bool thumbnail = true,
  }) {
    return _ArtistArtCachedUrlProvider(
      artistId: artistId,
      thumbnail: thumbnail,
    );
  }

  @override
  _ArtistArtCachedUrlProvider getProviderOverride(
    covariant _ArtistArtCachedUrlProvider provider,
  ) {
    return call(
      artistId: provider.artistId,
      thumbnail: provider.thumbnail,
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
  String? get name => r'_artistArtCachedUrlProvider';
}

/// See also [_artistArtCachedUrl].
class _ArtistArtCachedUrlProvider extends AutoDisposeFutureProvider<String?> {
  /// See also [_artistArtCachedUrl].
  _ArtistArtCachedUrlProvider({
    required this.artistId,
    this.thumbnail = true,
  }) : super.internal(
          (ref) => _artistArtCachedUrl(
            ref,
            artistId: artistId,
            thumbnail: thumbnail,
          ),
          from: _artistArtCachedUrlProvider,
          name: r'_artistArtCachedUrlProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$artistArtCachedUrlHash,
          dependencies: _ArtistArtCachedUrlFamily._dependencies,
          allTransitiveDependencies:
              _ArtistArtCachedUrlFamily._allTransitiveDependencies,
        );

  final String artistId;
  final bool thumbnail;

  @override
  bool operator ==(Object other) {
    return other is _ArtistArtCachedUrlProvider &&
        other.artistId == artistId &&
        other.thumbnail == thumbnail;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, artistId.hashCode);
    hash = _SystemHash.combine(hash, thumbnail.hashCode);

    return _SystemHash.finish(hash);
  }
}

String _$artistArtUriCacheInfoHash() =>
    r'9bdc0f5654882265236ef746ea697a6d107a4b6f';
typedef _ArtistArtUriCacheInfoRef = AutoDisposeFutureProviderRef<UriCacheInfo>;

/// See also [_artistArtUriCacheInfo].
@ProviderFor(_artistArtUriCacheInfo)
const _artistArtUriCacheInfoProvider = _ArtistArtUriCacheInfoFamily();

/// See also [_artistArtUriCacheInfo].
class _ArtistArtUriCacheInfoFamily extends Family<AsyncValue<UriCacheInfo>> {
  /// See also [_artistArtUriCacheInfo].
  const _ArtistArtUriCacheInfoFamily();

  /// See also [_artistArtUriCacheInfo].
  _ArtistArtUriCacheInfoProvider call({
    required String artistId,
    bool thumbnail = true,
  }) {
    return _ArtistArtUriCacheInfoProvider(
      artistId: artistId,
      thumbnail: thumbnail,
    );
  }

  @override
  _ArtistArtUriCacheInfoProvider getProviderOverride(
    covariant _ArtistArtUriCacheInfoProvider provider,
  ) {
    return call(
      artistId: provider.artistId,
      thumbnail: provider.thumbnail,
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
  String? get name => r'_artistArtUriCacheInfoProvider';
}

/// See also [_artistArtUriCacheInfo].
class _ArtistArtUriCacheInfoProvider
    extends AutoDisposeFutureProvider<UriCacheInfo> {
  /// See also [_artistArtUriCacheInfo].
  _ArtistArtUriCacheInfoProvider({
    required this.artistId,
    this.thumbnail = true,
  }) : super.internal(
          (ref) => _artistArtUriCacheInfo(
            ref,
            artistId: artistId,
            thumbnail: thumbnail,
          ),
          from: _artistArtUriCacheInfoProvider,
          name: r'_artistArtUriCacheInfoProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$artistArtUriCacheInfoHash,
          dependencies: _ArtistArtUriCacheInfoFamily._dependencies,
          allTransitiveDependencies:
              _ArtistArtUriCacheInfoFamily._allTransitiveDependencies,
        );

  final String artistId;
  final bool thumbnail;

  @override
  bool operator ==(Object other) {
    return other is _ArtistArtUriCacheInfoProvider &&
        other.artistId == artistId &&
        other.thumbnail == thumbnail;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, artistId.hashCode);
    hash = _SystemHash.combine(hash, thumbnail.hashCode);

    return _SystemHash.finish(hash);
  }
}
// ignore_for_file: unnecessary_raw_strings, subtype_of_sealed_class, invalid_use_of_internal_member, do_not_use_environment, prefer_const_constructors, public_member_api_docs, avoid_private_typedef_functions
