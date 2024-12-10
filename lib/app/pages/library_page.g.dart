// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'library_page.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$libraryTabObserverHash() =>
    r'a976ea55e2168e4684114c47592f25a2b187f15f';

/// See also [libraryTabObserver].
@ProviderFor(libraryTabObserver)
final libraryTabObserverProvider = Provider<TabObserver>.internal(
  libraryTabObserver,
  name: r'libraryTabObserverProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$libraryTabObserverHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef LibraryTabObserverRef = ProviderRef<TabObserver>;
String _$libraryTabPathHash() => r'fe60984ea9d629683d344f809749b1b9362735fa';

/// See also [libraryTabPath].
@ProviderFor(libraryTabPath)
final libraryTabPathProvider = StreamProvider<String>.internal(
  libraryTabPath,
  name: r'libraryTabPathProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$libraryTabPathHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef LibraryTabPathRef = StreamProviderRef<String>;
String _$libraryListQueryHash() => r'6079338e19e0249aaa09868dd405fd3aefc42c2b';

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

/// See also [libraryListQuery].
@ProviderFor(libraryListQuery)
const libraryListQueryProvider = LibraryListQueryFamily();

/// See also [libraryListQuery].
class LibraryListQueryFamily extends Family<LibraryListQuery> {
  /// See also [libraryListQuery].
  const LibraryListQueryFamily();

  /// See also [libraryListQuery].
  LibraryListQueryProvider call(
    int index,
  ) {
    return LibraryListQueryProvider(
      index,
    );
  }

  @override
  LibraryListQueryProvider getProviderOverride(
    covariant LibraryListQueryProvider provider,
  ) {
    return call(
      provider.index,
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
  String? get name => r'libraryListQueryProvider';
}

/// See also [libraryListQuery].
class LibraryListQueryProvider extends Provider<LibraryListQuery> {
  /// See also [libraryListQuery].
  LibraryListQueryProvider(
    int index,
  ) : this._internal(
          (ref) => libraryListQuery(
            ref as LibraryListQueryRef,
            index,
          ),
          from: libraryListQueryProvider,
          name: r'libraryListQueryProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$libraryListQueryHash,
          dependencies: LibraryListQueryFamily._dependencies,
          allTransitiveDependencies:
              LibraryListQueryFamily._allTransitiveDependencies,
          index: index,
        );

  LibraryListQueryProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.index,
  }) : super.internal();

  final int index;

  @override
  Override overrideWith(
    LibraryListQuery Function(LibraryListQueryRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: LibraryListQueryProvider._internal(
        (ref) => create(ref as LibraryListQueryRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        index: index,
      ),
    );
  }

  @override
  ProviderElement<LibraryListQuery> createElement() {
    return _LibraryListQueryProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is LibraryListQueryProvider && other.index == index;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, index.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin LibraryListQueryRef on ProviderRef<LibraryListQuery> {
  /// The parameter `index` of this provider.
  int get index;
}

class _LibraryListQueryProviderElement extends ProviderElement<LibraryListQuery>
    with LibraryListQueryRef {
  _LibraryListQueryProviderElement(super.provider);

  @override
  int get index => (origin as LibraryListQueryProvider).index;
}

String _$lastLibraryStateServiceHash() =>
    r'a49e26b5dc0fcb0f697ec2def08e7336f64c4cb3';

/// See also [LastLibraryStateService].
@ProviderFor(LastLibraryStateService)
final lastLibraryStateServiceProvider =
    AsyncNotifierProvider<LastLibraryStateService, void>.internal(
  LastLibraryStateService.new,
  name: r'lastLibraryStateServiceProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$lastLibraryStateServiceHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$LastLibraryStateService = AsyncNotifier<void>;
String _$libraryListsHash() => r'7c9fd1ca3b0d70253e0f5d8197abf18b3a18c995';

/// See also [LibraryLists].
@ProviderFor(LibraryLists)
final libraryListsProvider =
    NotifierProvider<LibraryLists, IList<LibraryListQuery>>.internal(
  LibraryLists.new,
  name: r'libraryListsProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$libraryListsHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$LibraryLists = Notifier<IList<LibraryListQuery>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member
