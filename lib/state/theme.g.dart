// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'theme.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$colorThemeHash() => r'37d4eb17395341800b67f4f257c2cc49bf3dc91b';

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

/// See also [_colorTheme].
@ProviderFor(_colorTheme)
const _colorThemeProvider = _ColorThemeFamily();

/// See also [_colorTheme].
class _ColorThemeFamily extends Family<ColorTheme> {
  /// See also [_colorTheme].
  const _ColorThemeFamily();

  /// See also [_colorTheme].
  _ColorThemeProvider call(
    Palette palette,
  ) {
    return _ColorThemeProvider(
      palette,
    );
  }

  @override
  _ColorThemeProvider getProviderOverride(
    covariant _ColorThemeProvider provider,
  ) {
    return call(
      provider.palette,
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
  String? get name => r'_colorThemeProvider';
}

/// See also [_colorTheme].
class _ColorThemeProvider extends AutoDisposeProvider<ColorTheme> {
  /// See also [_colorTheme].
  _ColorThemeProvider(
    Palette palette,
  ) : this._internal(
          (ref) => _colorTheme(
            ref as _ColorThemeRef,
            palette,
          ),
          from: _colorThemeProvider,
          name: r'_colorThemeProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$colorThemeHash,
          dependencies: _ColorThemeFamily._dependencies,
          allTransitiveDependencies:
              _ColorThemeFamily._allTransitiveDependencies,
          palette: palette,
        );

  _ColorThemeProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.palette,
  }) : super.internal();

  final Palette palette;

  @override
  Override overrideWith(
    ColorTheme Function(_ColorThemeRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: _ColorThemeProvider._internal(
        (ref) => create(ref as _ColorThemeRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        palette: palette,
      ),
    );
  }

  @override
  AutoDisposeProviderElement<ColorTheme> createElement() {
    return _ColorThemeProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is _ColorThemeProvider && other.palette == palette;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, palette.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin _ColorThemeRef on AutoDisposeProviderRef<ColorTheme> {
  /// The parameter `palette` of this provider.
  Palette get palette;
}

class _ColorThemeProviderElement extends AutoDisposeProviderElement<ColorTheme>
    with _ColorThemeRef {
  _ColorThemeProviderElement(super.provider);

  @override
  Palette get palette => (origin as _ColorThemeProvider).palette;
}

String _$baseThemeHash() => r'489ea7dcf66a57e6eda300c75d8af5fcaef85e8e';

/// See also [baseTheme].
@ProviderFor(baseTheme)
final baseThemeProvider = AutoDisposeProvider<ColorTheme>.internal(
  baseTheme,
  name: r'baseThemeProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$baseThemeHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef BaseThemeRef = AutoDisposeProviderRef<ColorTheme>;
String _$albumArtPaletteHash() => r'8130b954ee3c67f53207593d4ed3dfbffb00c95d';

/// See also [albumArtPalette].
@ProviderFor(albumArtPalette)
const albumArtPaletteProvider = AlbumArtPaletteFamily();

/// See also [albumArtPalette].
class AlbumArtPaletteFamily extends Family<AsyncValue<Palette>> {
  /// See also [albumArtPalette].
  const AlbumArtPaletteFamily();

  /// See also [albumArtPalette].
  AlbumArtPaletteProvider call(
    String id,
  ) {
    return AlbumArtPaletteProvider(
      id,
    );
  }

  @override
  AlbumArtPaletteProvider getProviderOverride(
    covariant AlbumArtPaletteProvider provider,
  ) {
    return call(
      provider.id,
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
  String? get name => r'albumArtPaletteProvider';
}

/// See also [albumArtPalette].
class AlbumArtPaletteProvider extends AutoDisposeFutureProvider<Palette> {
  /// See also [albumArtPalette].
  AlbumArtPaletteProvider(
    String id,
  ) : this._internal(
          (ref) => albumArtPalette(
            ref as AlbumArtPaletteRef,
            id,
          ),
          from: albumArtPaletteProvider,
          name: r'albumArtPaletteProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumArtPaletteHash,
          dependencies: AlbumArtPaletteFamily._dependencies,
          allTransitiveDependencies:
              AlbumArtPaletteFamily._allTransitiveDependencies,
          id: id,
        );

  AlbumArtPaletteProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
  }) : super.internal();

  final String id;

  @override
  Override overrideWith(
    FutureOr<Palette> Function(AlbumArtPaletteRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumArtPaletteProvider._internal(
        (ref) => create(ref as AlbumArtPaletteRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<Palette> createElement() {
    return _AlbumArtPaletteProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumArtPaletteProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumArtPaletteRef on AutoDisposeFutureProviderRef<Palette> {
  /// The parameter `id` of this provider.
  String get id;
}

class _AlbumArtPaletteProviderElement
    extends AutoDisposeFutureProviderElement<Palette> with AlbumArtPaletteRef {
  _AlbumArtPaletteProviderElement(super.provider);

  @override
  String get id => (origin as AlbumArtPaletteProvider).id;
}

String _$playlistArtPaletteHash() =>
    r'6bc015688f354ea8d91dde86e2a7191ef1ef6496';

/// See also [playlistArtPalette].
@ProviderFor(playlistArtPalette)
const playlistArtPaletteProvider = PlaylistArtPaletteFamily();

/// See also [playlistArtPalette].
class PlaylistArtPaletteFamily extends Family<AsyncValue<Palette>> {
  /// See also [playlistArtPalette].
  const PlaylistArtPaletteFamily();

  /// See also [playlistArtPalette].
  PlaylistArtPaletteProvider call(
    String id,
  ) {
    return PlaylistArtPaletteProvider(
      id,
    );
  }

  @override
  PlaylistArtPaletteProvider getProviderOverride(
    covariant PlaylistArtPaletteProvider provider,
  ) {
    return call(
      provider.id,
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
  String? get name => r'playlistArtPaletteProvider';
}

/// See also [playlistArtPalette].
class PlaylistArtPaletteProvider extends AutoDisposeFutureProvider<Palette> {
  /// See also [playlistArtPalette].
  PlaylistArtPaletteProvider(
    String id,
  ) : this._internal(
          (ref) => playlistArtPalette(
            ref as PlaylistArtPaletteRef,
            id,
          ),
          from: playlistArtPaletteProvider,
          name: r'playlistArtPaletteProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$playlistArtPaletteHash,
          dependencies: PlaylistArtPaletteFamily._dependencies,
          allTransitiveDependencies:
              PlaylistArtPaletteFamily._allTransitiveDependencies,
          id: id,
        );

  PlaylistArtPaletteProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
  }) : super.internal();

  final String id;

  @override
  Override overrideWith(
    FutureOr<Palette> Function(PlaylistArtPaletteRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: PlaylistArtPaletteProvider._internal(
        (ref) => create(ref as PlaylistArtPaletteRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<Palette> createElement() {
    return _PlaylistArtPaletteProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is PlaylistArtPaletteProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin PlaylistArtPaletteRef on AutoDisposeFutureProviderRef<Palette> {
  /// The parameter `id` of this provider.
  String get id;
}

class _PlaylistArtPaletteProviderElement
    extends AutoDisposeFutureProviderElement<Palette>
    with PlaylistArtPaletteRef {
  _PlaylistArtPaletteProviderElement(super.provider);

  @override
  String get id => (origin as PlaylistArtPaletteProvider).id;
}

String _$mediaItemPaletteHash() => r'2f2744aa735c6056919197c283a367714d7e04e4';

/// See also [mediaItemPalette].
@ProviderFor(mediaItemPalette)
final mediaItemPaletteProvider = AutoDisposeFutureProvider<Palette>.internal(
  mediaItemPalette,
  name: r'mediaItemPaletteProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$mediaItemPaletteHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef MediaItemPaletteRef = AutoDisposeFutureProviderRef<Palette>;
String _$mediaItemThemeHash() => r'f43e6f86eeed7aef33fa8bd2695454a760f41afa';

/// See also [mediaItemTheme].
@ProviderFor(mediaItemTheme)
final mediaItemThemeProvider = AutoDisposeFutureProvider<ColorTheme>.internal(
  mediaItemTheme,
  name: r'mediaItemThemeProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$mediaItemThemeHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef MediaItemThemeRef = AutoDisposeFutureProviderRef<ColorTheme>;
String _$albumArtThemeHash() => r'd3ee71b2df856f1763ec925e158ae2e0f613b9e0';

/// See also [albumArtTheme].
@ProviderFor(albumArtTheme)
const albumArtThemeProvider = AlbumArtThemeFamily();

/// See also [albumArtTheme].
class AlbumArtThemeFamily extends Family<AsyncValue<ColorTheme>> {
  /// See also [albumArtTheme].
  const AlbumArtThemeFamily();

  /// See also [albumArtTheme].
  AlbumArtThemeProvider call(
    String id,
  ) {
    return AlbumArtThemeProvider(
      id,
    );
  }

  @override
  AlbumArtThemeProvider getProviderOverride(
    covariant AlbumArtThemeProvider provider,
  ) {
    return call(
      provider.id,
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
  String? get name => r'albumArtThemeProvider';
}

/// See also [albumArtTheme].
class AlbumArtThemeProvider extends AutoDisposeFutureProvider<ColorTheme> {
  /// See also [albumArtTheme].
  AlbumArtThemeProvider(
    String id,
  ) : this._internal(
          (ref) => albumArtTheme(
            ref as AlbumArtThemeRef,
            id,
          ),
          from: albumArtThemeProvider,
          name: r'albumArtThemeProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumArtThemeHash,
          dependencies: AlbumArtThemeFamily._dependencies,
          allTransitiveDependencies:
              AlbumArtThemeFamily._allTransitiveDependencies,
          id: id,
        );

  AlbumArtThemeProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
  }) : super.internal();

  final String id;

  @override
  Override overrideWith(
    FutureOr<ColorTheme> Function(AlbumArtThemeRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumArtThemeProvider._internal(
        (ref) => create(ref as AlbumArtThemeRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<ColorTheme> createElement() {
    return _AlbumArtThemeProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumArtThemeProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumArtThemeRef on AutoDisposeFutureProviderRef<ColorTheme> {
  /// The parameter `id` of this provider.
  String get id;
}

class _AlbumArtThemeProviderElement
    extends AutoDisposeFutureProviderElement<ColorTheme> with AlbumArtThemeRef {
  _AlbumArtThemeProviderElement(super.provider);

  @override
  String get id => (origin as AlbumArtThemeProvider).id;
}

String _$playlistArtThemeHash() => r'1629552e1f3aa2a1e7d223ac1e078893042e5e3b';

/// See also [playlistArtTheme].
@ProviderFor(playlistArtTheme)
const playlistArtThemeProvider = PlaylistArtThemeFamily();

/// See also [playlistArtTheme].
class PlaylistArtThemeFamily extends Family<AsyncValue<ColorTheme>> {
  /// See also [playlistArtTheme].
  const PlaylistArtThemeFamily();

  /// See also [playlistArtTheme].
  PlaylistArtThemeProvider call(
    String id,
  ) {
    return PlaylistArtThemeProvider(
      id,
    );
  }

  @override
  PlaylistArtThemeProvider getProviderOverride(
    covariant PlaylistArtThemeProvider provider,
  ) {
    return call(
      provider.id,
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
  String? get name => r'playlistArtThemeProvider';
}

/// See also [playlistArtTheme].
class PlaylistArtThemeProvider extends AutoDisposeFutureProvider<ColorTheme> {
  /// See also [playlistArtTheme].
  PlaylistArtThemeProvider(
    String id,
  ) : this._internal(
          (ref) => playlistArtTheme(
            ref as PlaylistArtThemeRef,
            id,
          ),
          from: playlistArtThemeProvider,
          name: r'playlistArtThemeProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$playlistArtThemeHash,
          dependencies: PlaylistArtThemeFamily._dependencies,
          allTransitiveDependencies:
              PlaylistArtThemeFamily._allTransitiveDependencies,
          id: id,
        );

  PlaylistArtThemeProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
  }) : super.internal();

  final String id;

  @override
  Override overrideWith(
    FutureOr<ColorTheme> Function(PlaylistArtThemeRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: PlaylistArtThemeProvider._internal(
        (ref) => create(ref as PlaylistArtThemeRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<ColorTheme> createElement() {
    return _PlaylistArtThemeProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is PlaylistArtThemeProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin PlaylistArtThemeRef on AutoDisposeFutureProviderRef<ColorTheme> {
  /// The parameter `id` of this provider.
  String get id;
}

class _PlaylistArtThemeProviderElement
    extends AutoDisposeFutureProviderElement<ColorTheme>
    with PlaylistArtThemeRef {
  _PlaylistArtThemeProviderElement(super.provider);

  @override
  String get id => (origin as PlaylistArtThemeProvider).id;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member
