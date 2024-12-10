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
    required String artistId,
    bool thumbnail = true,
  }) : this._internal(
          (ref) => _artistArtCacheInfo(
            ref as _ArtistArtCacheInfoRef,
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
          artistId: artistId,
          thumbnail: thumbnail,
        );

  _ArtistArtCacheInfoProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.artistId,
    required this.thumbnail,
  }) : super.internal();

  final String artistId;
  final bool thumbnail;

  @override
  Override overrideWith(
    CacheInfo Function(_ArtistArtCacheInfoRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: _ArtistArtCacheInfoProvider._internal(
        (ref) => create(ref as _ArtistArtCacheInfoRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        artistId: artistId,
        thumbnail: thumbnail,
      ),
    );
  }

  @override
  AutoDisposeProviderElement<CacheInfo> createElement() {
    return _ArtistArtCacheInfoProviderElement(this);
  }

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

mixin _ArtistArtCacheInfoRef on AutoDisposeProviderRef<CacheInfo> {
  /// The parameter `artistId` of this provider.
  String get artistId;

  /// The parameter `thumbnail` of this provider.
  bool get thumbnail;
}

class _ArtistArtCacheInfoProviderElement
    extends AutoDisposeProviderElement<CacheInfo> with _ArtistArtCacheInfoRef {
  _ArtistArtCacheInfoProviderElement(super.provider);

  @override
  String get artistId => (origin as _ArtistArtCacheInfoProvider).artistId;
  @override
  bool get thumbnail => (origin as _ArtistArtCacheInfoProvider).thumbnail;
}

String _$artistArtCachedUrlHash() =>
    r'2a5e0fea614ff12a1d562faccec6cfe98394af42';

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
    required String artistId,
    bool thumbnail = true,
  }) : this._internal(
          (ref) => _artistArtCachedUrl(
            ref as _ArtistArtCachedUrlRef,
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
          artistId: artistId,
          thumbnail: thumbnail,
        );

  _ArtistArtCachedUrlProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.artistId,
    required this.thumbnail,
  }) : super.internal();

  final String artistId;
  final bool thumbnail;

  @override
  Override overrideWith(
    FutureOr<String?> Function(_ArtistArtCachedUrlRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: _ArtistArtCachedUrlProvider._internal(
        (ref) => create(ref as _ArtistArtCachedUrlRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        artistId: artistId,
        thumbnail: thumbnail,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<String?> createElement() {
    return _ArtistArtCachedUrlProviderElement(this);
  }

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

mixin _ArtistArtCachedUrlRef on AutoDisposeFutureProviderRef<String?> {
  /// The parameter `artistId` of this provider.
  String get artistId;

  /// The parameter `thumbnail` of this provider.
  bool get thumbnail;
}

class _ArtistArtCachedUrlProviderElement
    extends AutoDisposeFutureProviderElement<String?>
    with _ArtistArtCachedUrlRef {
  _ArtistArtCachedUrlProviderElement(super.provider);

  @override
  String get artistId => (origin as _ArtistArtCachedUrlProvider).artistId;
  @override
  bool get thumbnail => (origin as _ArtistArtCachedUrlProvider).thumbnail;
}

String _$artistArtUriCacheInfoHash() =>
    r'9bdc0f5654882265236ef746ea697a6d107a4b6f';

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
    required String artistId,
    bool thumbnail = true,
  }) : this._internal(
          (ref) => _artistArtUriCacheInfo(
            ref as _ArtistArtUriCacheInfoRef,
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
          artistId: artistId,
          thumbnail: thumbnail,
        );

  _ArtistArtUriCacheInfoProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.artistId,
    required this.thumbnail,
  }) : super.internal();

  final String artistId;
  final bool thumbnail;

  @override
  Override overrideWith(
    FutureOr<UriCacheInfo> Function(_ArtistArtUriCacheInfoRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: _ArtistArtUriCacheInfoProvider._internal(
        (ref) => create(ref as _ArtistArtUriCacheInfoRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        artistId: artistId,
        thumbnail: thumbnail,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<UriCacheInfo> createElement() {
    return _ArtistArtUriCacheInfoProviderElement(this);
  }

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

mixin _ArtistArtUriCacheInfoRef on AutoDisposeFutureProviderRef<UriCacheInfo> {
  /// The parameter `artistId` of this provider.
  String get artistId;

  /// The parameter `thumbnail` of this provider.
  bool get thumbnail;
}

class _ArtistArtUriCacheInfoProviderElement
    extends AutoDisposeFutureProviderElement<UriCacheInfo>
    with _ArtistArtUriCacheInfoRef {
  _ArtistArtUriCacheInfoProviderElement(super.provider);

  @override
  String get artistId => (origin as _ArtistArtUriCacheInfoProvider).artistId;
  @override
  bool get thumbnail => (origin as _ArtistArtUriCacheInfoProvider).thumbnail;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member
