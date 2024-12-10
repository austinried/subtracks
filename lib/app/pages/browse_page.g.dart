// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'browse_page.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$albumsCategoryListHash() =>
    r'e0516a585bf39e8140c72c08fd41f33a817c747d';

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

/// See also [albumsCategoryList].
@ProviderFor(albumsCategoryList)
const albumsCategoryListProvider = AlbumsCategoryListFamily();

/// See also [albumsCategoryList].
class AlbumsCategoryListFamily extends Family<AsyncValue<List<Album>>> {
  /// See also [albumsCategoryList].
  const AlbumsCategoryListFamily();

  /// See also [albumsCategoryList].
  AlbumsCategoryListProvider call(
    ListQuery opt,
  ) {
    return AlbumsCategoryListProvider(
      opt,
    );
  }

  @override
  AlbumsCategoryListProvider getProviderOverride(
    covariant AlbumsCategoryListProvider provider,
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
  String? get name => r'albumsCategoryListProvider';
}

/// See also [albumsCategoryList].
class AlbumsCategoryListProvider
    extends AutoDisposeStreamProvider<List<Album>> {
  /// See also [albumsCategoryList].
  AlbumsCategoryListProvider(
    ListQuery opt,
  ) : this._internal(
          (ref) => albumsCategoryList(
            ref as AlbumsCategoryListRef,
            opt,
          ),
          from: albumsCategoryListProvider,
          name: r'albumsCategoryListProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumsCategoryListHash,
          dependencies: AlbumsCategoryListFamily._dependencies,
          allTransitiveDependencies:
              AlbumsCategoryListFamily._allTransitiveDependencies,
          opt: opt,
        );

  AlbumsCategoryListProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.opt,
  }) : super.internal();

  final ListQuery opt;

  @override
  Override overrideWith(
    Stream<List<Album>> Function(AlbumsCategoryListRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumsCategoryListProvider._internal(
        (ref) => create(ref as AlbumsCategoryListRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        opt: opt,
      ),
    );
  }

  @override
  AutoDisposeStreamProviderElement<List<Album>> createElement() {
    return _AlbumsCategoryListProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumsCategoryListProvider && other.opt == opt;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, opt.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumsCategoryListRef on AutoDisposeStreamProviderRef<List<Album>> {
  /// The parameter `opt` of this provider.
  ListQuery get opt;
}

class _AlbumsCategoryListProviderElement
    extends AutoDisposeStreamProviderElement<List<Album>>
    with AlbumsCategoryListRef {
  _AlbumsCategoryListProviderElement(super.provider);

  @override
  ListQuery get opt => (origin as AlbumsCategoryListProvider).opt;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member
