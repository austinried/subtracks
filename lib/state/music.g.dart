// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'music.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$artistHash() => r'2e2f1e28d5133b3ad10254a3c646de7d8a150c8e';

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

/// See also [artist].
@ProviderFor(artist)
const artistProvider = ArtistFamily();

/// See also [artist].
class ArtistFamily extends Family<AsyncValue<Artist>> {
  /// See also [artist].
  const ArtistFamily();

  /// See also [artist].
  ArtistProvider call(
    String id,
  ) {
    return ArtistProvider(
      id,
    );
  }

  @override
  ArtistProvider getProviderOverride(
    covariant ArtistProvider provider,
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
  String? get name => r'artistProvider';
}

/// See also [artist].
class ArtistProvider extends AutoDisposeStreamProvider<Artist> {
  /// See also [artist].
  ArtistProvider(
    String id,
  ) : this._internal(
          (ref) => artist(
            ref as ArtistRef,
            id,
          ),
          from: artistProvider,
          name: r'artistProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$artistHash,
          dependencies: ArtistFamily._dependencies,
          allTransitiveDependencies: ArtistFamily._allTransitiveDependencies,
          id: id,
        );

  ArtistProvider._internal(
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
    Stream<Artist> Function(ArtistRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: ArtistProvider._internal(
        (ref) => create(ref as ArtistRef),
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
  AutoDisposeStreamProviderElement<Artist> createElement() {
    return _ArtistProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is ArtistProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin ArtistRef on AutoDisposeStreamProviderRef<Artist> {
  /// The parameter `id` of this provider.
  String get id;
}

class _ArtistProviderElement extends AutoDisposeStreamProviderElement<Artist>
    with ArtistRef {
  _ArtistProviderElement(super.provider);

  @override
  String get id => (origin as ArtistProvider).id;
}

String _$albumHash() => r'914f37fe8bc2d883de2548ce9411aa10e8a9868a';

/// See also [album].
@ProviderFor(album)
const albumProvider = AlbumFamily();

/// See also [album].
class AlbumFamily extends Family<AsyncValue<Album>> {
  /// See also [album].
  const AlbumFamily();

  /// See also [album].
  AlbumProvider call(
    String id,
  ) {
    return AlbumProvider(
      id,
    );
  }

  @override
  AlbumProvider getProviderOverride(
    covariant AlbumProvider provider,
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
  String? get name => r'albumProvider';
}

/// See also [album].
class AlbumProvider extends AutoDisposeStreamProvider<Album> {
  /// See also [album].
  AlbumProvider(
    String id,
  ) : this._internal(
          (ref) => album(
            ref as AlbumRef,
            id,
          ),
          from: albumProvider,
          name: r'albumProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumHash,
          dependencies: AlbumFamily._dependencies,
          allTransitiveDependencies: AlbumFamily._allTransitiveDependencies,
          id: id,
        );

  AlbumProvider._internal(
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
    Stream<Album> Function(AlbumRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumProvider._internal(
        (ref) => create(ref as AlbumRef),
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
  AutoDisposeStreamProviderElement<Album> createElement() {
    return _AlbumProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumRef on AutoDisposeStreamProviderRef<Album> {
  /// The parameter `id` of this provider.
  String get id;
}

class _AlbumProviderElement extends AutoDisposeStreamProviderElement<Album>
    with AlbumRef {
  _AlbumProviderElement(super.provider);

  @override
  String get id => (origin as AlbumProvider).id;
}

String _$albumDownloadStatusHash() =>
    r'0b6c5f09f8327a624172d7ef33e1911c87b2cb60';

/// See also [albumDownloadStatus].
@ProviderFor(albumDownloadStatus)
const albumDownloadStatusProvider = AlbumDownloadStatusFamily();

/// See also [albumDownloadStatus].
class AlbumDownloadStatusFamily extends Family<AsyncValue<ListDownloadStatus>> {
  /// See also [albumDownloadStatus].
  const AlbumDownloadStatusFamily();

  /// See also [albumDownloadStatus].
  AlbumDownloadStatusProvider call(
    String id,
  ) {
    return AlbumDownloadStatusProvider(
      id,
    );
  }

  @override
  AlbumDownloadStatusProvider getProviderOverride(
    covariant AlbumDownloadStatusProvider provider,
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
  String? get name => r'albumDownloadStatusProvider';
}

/// See also [albumDownloadStatus].
class AlbumDownloadStatusProvider
    extends AutoDisposeStreamProvider<ListDownloadStatus> {
  /// See also [albumDownloadStatus].
  AlbumDownloadStatusProvider(
    String id,
  ) : this._internal(
          (ref) => albumDownloadStatus(
            ref as AlbumDownloadStatusRef,
            id,
          ),
          from: albumDownloadStatusProvider,
          name: r'albumDownloadStatusProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumDownloadStatusHash,
          dependencies: AlbumDownloadStatusFamily._dependencies,
          allTransitiveDependencies:
              AlbumDownloadStatusFamily._allTransitiveDependencies,
          id: id,
        );

  AlbumDownloadStatusProvider._internal(
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
    Stream<ListDownloadStatus> Function(AlbumDownloadStatusRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumDownloadStatusProvider._internal(
        (ref) => create(ref as AlbumDownloadStatusRef),
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
  AutoDisposeStreamProviderElement<ListDownloadStatus> createElement() {
    return _AlbumDownloadStatusProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumDownloadStatusProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumDownloadStatusRef
    on AutoDisposeStreamProviderRef<ListDownloadStatus> {
  /// The parameter `id` of this provider.
  String get id;
}

class _AlbumDownloadStatusProviderElement
    extends AutoDisposeStreamProviderElement<ListDownloadStatus>
    with AlbumDownloadStatusRef {
  _AlbumDownloadStatusProviderElement(super.provider);

  @override
  String get id => (origin as AlbumDownloadStatusProvider).id;
}

String _$playlistDownloadStatusHash() =>
    r'd563e8746265b2022c59e70869d136cdf6c03ab9';

/// See also [playlistDownloadStatus].
@ProviderFor(playlistDownloadStatus)
const playlistDownloadStatusProvider = PlaylistDownloadStatusFamily();

/// See also [playlistDownloadStatus].
class PlaylistDownloadStatusFamily
    extends Family<AsyncValue<ListDownloadStatus>> {
  /// See also [playlistDownloadStatus].
  const PlaylistDownloadStatusFamily();

  /// See also [playlistDownloadStatus].
  PlaylistDownloadStatusProvider call(
    String id,
  ) {
    return PlaylistDownloadStatusProvider(
      id,
    );
  }

  @override
  PlaylistDownloadStatusProvider getProviderOverride(
    covariant PlaylistDownloadStatusProvider provider,
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
  String? get name => r'playlistDownloadStatusProvider';
}

/// See also [playlistDownloadStatus].
class PlaylistDownloadStatusProvider
    extends AutoDisposeStreamProvider<ListDownloadStatus> {
  /// See also [playlistDownloadStatus].
  PlaylistDownloadStatusProvider(
    String id,
  ) : this._internal(
          (ref) => playlistDownloadStatus(
            ref as PlaylistDownloadStatusRef,
            id,
          ),
          from: playlistDownloadStatusProvider,
          name: r'playlistDownloadStatusProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$playlistDownloadStatusHash,
          dependencies: PlaylistDownloadStatusFamily._dependencies,
          allTransitiveDependencies:
              PlaylistDownloadStatusFamily._allTransitiveDependencies,
          id: id,
        );

  PlaylistDownloadStatusProvider._internal(
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
    Stream<ListDownloadStatus> Function(PlaylistDownloadStatusRef provider)
        create,
  ) {
    return ProviderOverride(
      origin: this,
      override: PlaylistDownloadStatusProvider._internal(
        (ref) => create(ref as PlaylistDownloadStatusRef),
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
  AutoDisposeStreamProviderElement<ListDownloadStatus> createElement() {
    return _PlaylistDownloadStatusProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is PlaylistDownloadStatusProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin PlaylistDownloadStatusRef
    on AutoDisposeStreamProviderRef<ListDownloadStatus> {
  /// The parameter `id` of this provider.
  String get id;
}

class _PlaylistDownloadStatusProviderElement
    extends AutoDisposeStreamProviderElement<ListDownloadStatus>
    with PlaylistDownloadStatusRef {
  _PlaylistDownloadStatusProviderElement(super.provider);

  @override
  String get id => (origin as PlaylistDownloadStatusProvider).id;
}

String _$songHash() => r'bd8ac3b046180c9fa7676e437542d5afe4dcdb7d';

/// See also [song].
@ProviderFor(song)
const songProvider = SongFamily();

/// See also [song].
class SongFamily extends Family<AsyncValue<Song>> {
  /// See also [song].
  const SongFamily();

  /// See also [song].
  SongProvider call(
    String id,
  ) {
    return SongProvider(
      id,
    );
  }

  @override
  SongProvider getProviderOverride(
    covariant SongProvider provider,
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
  String? get name => r'songProvider';
}

/// See also [song].
class SongProvider extends AutoDisposeStreamProvider<Song> {
  /// See also [song].
  SongProvider(
    String id,
  ) : this._internal(
          (ref) => song(
            ref as SongRef,
            id,
          ),
          from: songProvider,
          name: r'songProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product') ? null : _$songHash,
          dependencies: SongFamily._dependencies,
          allTransitiveDependencies: SongFamily._allTransitiveDependencies,
          id: id,
        );

  SongProvider._internal(
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
    Stream<Song> Function(SongRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: SongProvider._internal(
        (ref) => create(ref as SongRef),
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
  AutoDisposeStreamProviderElement<Song> createElement() {
    return _SongProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is SongProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin SongRef on AutoDisposeStreamProviderRef<Song> {
  /// The parameter `id` of this provider.
  String get id;
}

class _SongProviderElement extends AutoDisposeStreamProviderElement<Song>
    with SongRef {
  _SongProviderElement(super.provider);

  @override
  String get id => (origin as SongProvider).id;
}

String _$albumSongsListHash() => r'dfc357b4c2b6a7dcc5e4701dc395c49ec8c8b88a';

/// See also [albumSongsList].
@ProviderFor(albumSongsList)
const albumSongsListProvider = AlbumSongsListFamily();

/// See also [albumSongsList].
class AlbumSongsListFamily extends Family<AsyncValue<List<Song>>> {
  /// See also [albumSongsList].
  const AlbumSongsListFamily();

  /// See also [albumSongsList].
  AlbumSongsListProvider call(
    String id,
    ListQuery opt,
  ) {
    return AlbumSongsListProvider(
      id,
      opt,
    );
  }

  @override
  AlbumSongsListProvider getProviderOverride(
    covariant AlbumSongsListProvider provider,
  ) {
    return call(
      provider.id,
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
  String? get name => r'albumSongsListProvider';
}

/// See also [albumSongsList].
class AlbumSongsListProvider extends AutoDisposeFutureProvider<List<Song>> {
  /// See also [albumSongsList].
  AlbumSongsListProvider(
    String id,
    ListQuery opt,
  ) : this._internal(
          (ref) => albumSongsList(
            ref as AlbumSongsListRef,
            id,
            opt,
          ),
          from: albumSongsListProvider,
          name: r'albumSongsListProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumSongsListHash,
          dependencies: AlbumSongsListFamily._dependencies,
          allTransitiveDependencies:
              AlbumSongsListFamily._allTransitiveDependencies,
          id: id,
          opt: opt,
        );

  AlbumSongsListProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
    required this.opt,
  }) : super.internal();

  final String id;
  final ListQuery opt;

  @override
  Override overrideWith(
    FutureOr<List<Song>> Function(AlbumSongsListRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumSongsListProvider._internal(
        (ref) => create(ref as AlbumSongsListRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
        opt: opt,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<List<Song>> createElement() {
    return _AlbumSongsListProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumSongsListProvider &&
        other.id == id &&
        other.opt == opt;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);
    hash = _SystemHash.combine(hash, opt.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumSongsListRef on AutoDisposeFutureProviderRef<List<Song>> {
  /// The parameter `id` of this provider.
  String get id;

  /// The parameter `opt` of this provider.
  ListQuery get opt;
}

class _AlbumSongsListProviderElement
    extends AutoDisposeFutureProviderElement<List<Song>>
    with AlbumSongsListRef {
  _AlbumSongsListProviderElement(super.provider);

  @override
  String get id => (origin as AlbumSongsListProvider).id;
  @override
  ListQuery get opt => (origin as AlbumSongsListProvider).opt;
}

String _$songsByAlbumListHash() => r'd6ed7fd9d8ceb3c5743b8c5538c7ffd50d2a8284';

/// See also [songsByAlbumList].
@ProviderFor(songsByAlbumList)
const songsByAlbumListProvider = SongsByAlbumListFamily();

/// See also [songsByAlbumList].
class SongsByAlbumListFamily extends Family<AsyncValue<List<Song>>> {
  /// See also [songsByAlbumList].
  const SongsByAlbumListFamily();

  /// See also [songsByAlbumList].
  SongsByAlbumListProvider call(
    ListQuery opt,
  ) {
    return SongsByAlbumListProvider(
      opt,
    );
  }

  @override
  SongsByAlbumListProvider getProviderOverride(
    covariant SongsByAlbumListProvider provider,
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
  String? get name => r'songsByAlbumListProvider';
}

/// See also [songsByAlbumList].
class SongsByAlbumListProvider extends AutoDisposeFutureProvider<List<Song>> {
  /// See also [songsByAlbumList].
  SongsByAlbumListProvider(
    ListQuery opt,
  ) : this._internal(
          (ref) => songsByAlbumList(
            ref as SongsByAlbumListRef,
            opt,
          ),
          from: songsByAlbumListProvider,
          name: r'songsByAlbumListProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$songsByAlbumListHash,
          dependencies: SongsByAlbumListFamily._dependencies,
          allTransitiveDependencies:
              SongsByAlbumListFamily._allTransitiveDependencies,
          opt: opt,
        );

  SongsByAlbumListProvider._internal(
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
    FutureOr<List<Song>> Function(SongsByAlbumListRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: SongsByAlbumListProvider._internal(
        (ref) => create(ref as SongsByAlbumListRef),
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
  AutoDisposeFutureProviderElement<List<Song>> createElement() {
    return _SongsByAlbumListProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is SongsByAlbumListProvider && other.opt == opt;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, opt.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin SongsByAlbumListRef on AutoDisposeFutureProviderRef<List<Song>> {
  /// The parameter `opt` of this provider.
  ListQuery get opt;
}

class _SongsByAlbumListProviderElement
    extends AutoDisposeFutureProviderElement<List<Song>>
    with SongsByAlbumListRef {
  _SongsByAlbumListProviderElement(super.provider);

  @override
  ListQuery get opt => (origin as SongsByAlbumListProvider).opt;
}

String _$playlistHash() => r'df0c8441c67c487155574caeeb11f4cdfe25ed31';

/// See also [playlist].
@ProviderFor(playlist)
const playlistProvider = PlaylistFamily();

/// See also [playlist].
class PlaylistFamily extends Family<AsyncValue<Playlist>> {
  /// See also [playlist].
  const PlaylistFamily();

  /// See also [playlist].
  PlaylistProvider call(
    String id,
  ) {
    return PlaylistProvider(
      id,
    );
  }

  @override
  PlaylistProvider getProviderOverride(
    covariant PlaylistProvider provider,
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
  String? get name => r'playlistProvider';
}

/// See also [playlist].
class PlaylistProvider extends AutoDisposeStreamProvider<Playlist> {
  /// See also [playlist].
  PlaylistProvider(
    String id,
  ) : this._internal(
          (ref) => playlist(
            ref as PlaylistRef,
            id,
          ),
          from: playlistProvider,
          name: r'playlistProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$playlistHash,
          dependencies: PlaylistFamily._dependencies,
          allTransitiveDependencies: PlaylistFamily._allTransitiveDependencies,
          id: id,
        );

  PlaylistProvider._internal(
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
    Stream<Playlist> Function(PlaylistRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: PlaylistProvider._internal(
        (ref) => create(ref as PlaylistRef),
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
  AutoDisposeStreamProviderElement<Playlist> createElement() {
    return _PlaylistProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is PlaylistProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin PlaylistRef on AutoDisposeStreamProviderRef<Playlist> {
  /// The parameter `id` of this provider.
  String get id;
}

class _PlaylistProviderElement
    extends AutoDisposeStreamProviderElement<Playlist> with PlaylistRef {
  _PlaylistProviderElement(super.provider);

  @override
  String get id => (origin as PlaylistProvider).id;
}

String _$playlistSongsListHash() => r'c3593829c6c91dc6958df6dc4cb6e89e3e7647a5';

/// See also [playlistSongsList].
@ProviderFor(playlistSongsList)
const playlistSongsListProvider = PlaylistSongsListFamily();

/// See also [playlistSongsList].
class PlaylistSongsListFamily extends Family<AsyncValue<List<Song>>> {
  /// See also [playlistSongsList].
  const PlaylistSongsListFamily();

  /// See also [playlistSongsList].
  PlaylistSongsListProvider call(
    String id,
    ListQuery opt,
  ) {
    return PlaylistSongsListProvider(
      id,
      opt,
    );
  }

  @override
  PlaylistSongsListProvider getProviderOverride(
    covariant PlaylistSongsListProvider provider,
  ) {
    return call(
      provider.id,
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
  String? get name => r'playlistSongsListProvider';
}

/// See also [playlistSongsList].
class PlaylistSongsListProvider extends AutoDisposeFutureProvider<List<Song>> {
  /// See also [playlistSongsList].
  PlaylistSongsListProvider(
    String id,
    ListQuery opt,
  ) : this._internal(
          (ref) => playlistSongsList(
            ref as PlaylistSongsListRef,
            id,
            opt,
          ),
          from: playlistSongsListProvider,
          name: r'playlistSongsListProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$playlistSongsListHash,
          dependencies: PlaylistSongsListFamily._dependencies,
          allTransitiveDependencies:
              PlaylistSongsListFamily._allTransitiveDependencies,
          id: id,
          opt: opt,
        );

  PlaylistSongsListProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
    required this.opt,
  }) : super.internal();

  final String id;
  final ListQuery opt;

  @override
  Override overrideWith(
    FutureOr<List<Song>> Function(PlaylistSongsListRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: PlaylistSongsListProvider._internal(
        (ref) => create(ref as PlaylistSongsListRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
        opt: opt,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<List<Song>> createElement() {
    return _PlaylistSongsListProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is PlaylistSongsListProvider &&
        other.id == id &&
        other.opt == opt;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);
    hash = _SystemHash.combine(hash, opt.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin PlaylistSongsListRef on AutoDisposeFutureProviderRef<List<Song>> {
  /// The parameter `id` of this provider.
  String get id;

  /// The parameter `opt` of this provider.
  ListQuery get opt;
}

class _PlaylistSongsListProviderElement
    extends AutoDisposeFutureProviderElement<List<Song>>
    with PlaylistSongsListRef {
  _PlaylistSongsListProviderElement(super.provider);

  @override
  String get id => (origin as PlaylistSongsListProvider).id;
  @override
  ListQuery get opt => (origin as PlaylistSongsListProvider).opt;
}

String _$albumsInIdsHash() => r'089ceafa122e1b3ce87c216bf3650cae5f656bdc';

/// See also [albumsInIds].
@ProviderFor(albumsInIds)
const albumsInIdsProvider = AlbumsInIdsFamily();

/// See also [albumsInIds].
class AlbumsInIdsFamily extends Family<AsyncValue<List<Album>>> {
  /// See also [albumsInIds].
  const AlbumsInIdsFamily();

  /// See also [albumsInIds].
  AlbumsInIdsProvider call(
    IList<String> ids,
  ) {
    return AlbumsInIdsProvider(
      ids,
    );
  }

  @override
  AlbumsInIdsProvider getProviderOverride(
    covariant AlbumsInIdsProvider provider,
  ) {
    return call(
      provider.ids,
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
  String? get name => r'albumsInIdsProvider';
}

/// See also [albumsInIds].
class AlbumsInIdsProvider extends AutoDisposeFutureProvider<List<Album>> {
  /// See also [albumsInIds].
  AlbumsInIdsProvider(
    IList<String> ids,
  ) : this._internal(
          (ref) => albumsInIds(
            ref as AlbumsInIdsRef,
            ids,
          ),
          from: albumsInIdsProvider,
          name: r'albumsInIdsProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumsInIdsHash,
          dependencies: AlbumsInIdsFamily._dependencies,
          allTransitiveDependencies:
              AlbumsInIdsFamily._allTransitiveDependencies,
          ids: ids,
        );

  AlbumsInIdsProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.ids,
  }) : super.internal();

  final IList<String> ids;

  @override
  Override overrideWith(
    FutureOr<List<Album>> Function(AlbumsInIdsRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumsInIdsProvider._internal(
        (ref) => create(ref as AlbumsInIdsRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        ids: ids,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<List<Album>> createElement() {
    return _AlbumsInIdsProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumsInIdsProvider && other.ids == ids;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, ids.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumsInIdsRef on AutoDisposeFutureProviderRef<List<Album>> {
  /// The parameter `ids` of this provider.
  IList<String> get ids;
}

class _AlbumsInIdsProviderElement
    extends AutoDisposeFutureProviderElement<List<Album>> with AlbumsInIdsRef {
  _AlbumsInIdsProviderElement(super.provider);

  @override
  IList<String> get ids => (origin as AlbumsInIdsProvider).ids;
}

String _$albumsByArtistIdHash() => r'e9a5c9255b6c7b6cfb22f4a4bacc4e518787465d';

/// See also [albumsByArtistId].
@ProviderFor(albumsByArtistId)
const albumsByArtistIdProvider = AlbumsByArtistIdFamily();

/// See also [albumsByArtistId].
class AlbumsByArtistIdFamily extends Family<AsyncValue<IList<Album>>> {
  /// See also [albumsByArtistId].
  const AlbumsByArtistIdFamily();

  /// See also [albumsByArtistId].
  AlbumsByArtistIdProvider call(
    String id,
  ) {
    return AlbumsByArtistIdProvider(
      id,
    );
  }

  @override
  AlbumsByArtistIdProvider getProviderOverride(
    covariant AlbumsByArtistIdProvider provider,
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
  String? get name => r'albumsByArtistIdProvider';
}

/// See also [albumsByArtistId].
class AlbumsByArtistIdProvider extends AutoDisposeStreamProvider<IList<Album>> {
  /// See also [albumsByArtistId].
  AlbumsByArtistIdProvider(
    String id,
  ) : this._internal(
          (ref) => albumsByArtistId(
            ref as AlbumsByArtistIdRef,
            id,
          ),
          from: albumsByArtistIdProvider,
          name: r'albumsByArtistIdProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumsByArtistIdHash,
          dependencies: AlbumsByArtistIdFamily._dependencies,
          allTransitiveDependencies:
              AlbumsByArtistIdFamily._allTransitiveDependencies,
          id: id,
        );

  AlbumsByArtistIdProvider._internal(
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
    Stream<IList<Album>> Function(AlbumsByArtistIdRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumsByArtistIdProvider._internal(
        (ref) => create(ref as AlbumsByArtistIdRef),
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
  AutoDisposeStreamProviderElement<IList<Album>> createElement() {
    return _AlbumsByArtistIdProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumsByArtistIdProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumsByArtistIdRef on AutoDisposeStreamProviderRef<IList<Album>> {
  /// The parameter `id` of this provider.
  String get id;
}

class _AlbumsByArtistIdProviderElement
    extends AutoDisposeStreamProviderElement<IList<Album>>
    with AlbumsByArtistIdRef {
  _AlbumsByArtistIdProviderElement(super.provider);

  @override
  String get id => (origin as AlbumsByArtistIdProvider).id;
}

String _$albumGenresHash() => r'17ba64391a678a4eb88017d782a843eeb92b2049';

/// See also [albumGenres].
@ProviderFor(albumGenres)
const albumGenresProvider = AlbumGenresFamily();

/// See also [albumGenres].
class AlbumGenresFamily extends Family<AsyncValue<IList<String>>> {
  /// See also [albumGenres].
  const AlbumGenresFamily();

  /// See also [albumGenres].
  AlbumGenresProvider call(
    Pagination page,
  ) {
    return AlbumGenresProvider(
      page,
    );
  }

  @override
  AlbumGenresProvider getProviderOverride(
    covariant AlbumGenresProvider provider,
  ) {
    return call(
      provider.page,
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
  String? get name => r'albumGenresProvider';
}

/// See also [albumGenres].
class AlbumGenresProvider extends AutoDisposeStreamProvider<IList<String>> {
  /// See also [albumGenres].
  AlbumGenresProvider(
    Pagination page,
  ) : this._internal(
          (ref) => albumGenres(
            ref as AlbumGenresRef,
            page,
          ),
          from: albumGenresProvider,
          name: r'albumGenresProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumGenresHash,
          dependencies: AlbumGenresFamily._dependencies,
          allTransitiveDependencies:
              AlbumGenresFamily._allTransitiveDependencies,
          page: page,
        );

  AlbumGenresProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.page,
  }) : super.internal();

  final Pagination page;

  @override
  Override overrideWith(
    Stream<IList<String>> Function(AlbumGenresRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumGenresProvider._internal(
        (ref) => create(ref as AlbumGenresRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        page: page,
      ),
    );
  }

  @override
  AutoDisposeStreamProviderElement<IList<String>> createElement() {
    return _AlbumGenresProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumGenresProvider && other.page == page;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, page.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumGenresRef on AutoDisposeStreamProviderRef<IList<String>> {
  /// The parameter `page` of this provider.
  Pagination get page;
}

class _AlbumGenresProviderElement
    extends AutoDisposeStreamProviderElement<IList<String>>
    with AlbumGenresRef {
  _AlbumGenresProviderElement(super.provider);

  @override
  Pagination get page => (origin as AlbumGenresProvider).page;
}

String _$albumsByGenreHash() => r'd35cb85a50df93b7f50c28c934ea182b69534b8b';

/// See also [albumsByGenre].
@ProviderFor(albumsByGenre)
const albumsByGenreProvider = AlbumsByGenreFamily();

/// See also [albumsByGenre].
class AlbumsByGenreFamily extends Family<AsyncValue<IList<Album>>> {
  /// See also [albumsByGenre].
  const AlbumsByGenreFamily();

  /// See also [albumsByGenre].
  AlbumsByGenreProvider call(
    String genre,
    Pagination page,
  ) {
    return AlbumsByGenreProvider(
      genre,
      page,
    );
  }

  @override
  AlbumsByGenreProvider getProviderOverride(
    covariant AlbumsByGenreProvider provider,
  ) {
    return call(
      provider.genre,
      provider.page,
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
  String? get name => r'albumsByGenreProvider';
}

/// See also [albumsByGenre].
class AlbumsByGenreProvider extends AutoDisposeStreamProvider<IList<Album>> {
  /// See also [albumsByGenre].
  AlbumsByGenreProvider(
    String genre,
    Pagination page,
  ) : this._internal(
          (ref) => albumsByGenre(
            ref as AlbumsByGenreRef,
            genre,
            page,
          ),
          from: albumsByGenreProvider,
          name: r'albumsByGenreProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$albumsByGenreHash,
          dependencies: AlbumsByGenreFamily._dependencies,
          allTransitiveDependencies:
              AlbumsByGenreFamily._allTransitiveDependencies,
          genre: genre,
          page: page,
        );

  AlbumsByGenreProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.genre,
    required this.page,
  }) : super.internal();

  final String genre;
  final Pagination page;

  @override
  Override overrideWith(
    Stream<IList<Album>> Function(AlbumsByGenreRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: AlbumsByGenreProvider._internal(
        (ref) => create(ref as AlbumsByGenreRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        genre: genre,
        page: page,
      ),
    );
  }

  @override
  AutoDisposeStreamProviderElement<IList<Album>> createElement() {
    return _AlbumsByGenreProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is AlbumsByGenreProvider &&
        other.genre == genre &&
        other.page == page;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, genre.hashCode);
    hash = _SystemHash.combine(hash, page.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin AlbumsByGenreRef on AutoDisposeStreamProviderRef<IList<Album>> {
  /// The parameter `genre` of this provider.
  String get genre;

  /// The parameter `page` of this provider.
  Pagination get page;
}

class _AlbumsByGenreProviderElement
    extends AutoDisposeStreamProviderElement<IList<Album>>
    with AlbumsByGenreRef {
  _AlbumsByGenreProviderElement(super.provider);

  @override
  String get genre => (origin as AlbumsByGenreProvider).genre;
  @override
  Pagination get page => (origin as AlbumsByGenreProvider).page;
}

String _$songsByGenreCountHash() => r'408bc1d82a6dae29e7b9a85fbf6988ba854551f7';

/// See also [songsByGenreCount].
@ProviderFor(songsByGenreCount)
const songsByGenreCountProvider = SongsByGenreCountFamily();

/// See also [songsByGenreCount].
class SongsByGenreCountFamily extends Family<AsyncValue<int>> {
  /// See also [songsByGenreCount].
  const SongsByGenreCountFamily();

  /// See also [songsByGenreCount].
  SongsByGenreCountProvider call(
    String genre,
  ) {
    return SongsByGenreCountProvider(
      genre,
    );
  }

  @override
  SongsByGenreCountProvider getProviderOverride(
    covariant SongsByGenreCountProvider provider,
  ) {
    return call(
      provider.genre,
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
  String? get name => r'songsByGenreCountProvider';
}

/// See also [songsByGenreCount].
class SongsByGenreCountProvider extends AutoDisposeStreamProvider<int> {
  /// See also [songsByGenreCount].
  SongsByGenreCountProvider(
    String genre,
  ) : this._internal(
          (ref) => songsByGenreCount(
            ref as SongsByGenreCountRef,
            genre,
          ),
          from: songsByGenreCountProvider,
          name: r'songsByGenreCountProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$songsByGenreCountHash,
          dependencies: SongsByGenreCountFamily._dependencies,
          allTransitiveDependencies:
              SongsByGenreCountFamily._allTransitiveDependencies,
          genre: genre,
        );

  SongsByGenreCountProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.genre,
  }) : super.internal();

  final String genre;

  @override
  Override overrideWith(
    Stream<int> Function(SongsByGenreCountRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: SongsByGenreCountProvider._internal(
        (ref) => create(ref as SongsByGenreCountRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        genre: genre,
      ),
    );
  }

  @override
  AutoDisposeStreamProviderElement<int> createElement() {
    return _SongsByGenreCountProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is SongsByGenreCountProvider && other.genre == genre;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, genre.hashCode);

    return _SystemHash.finish(hash);
  }
}

mixin SongsByGenreCountRef on AutoDisposeStreamProviderRef<int> {
  /// The parameter `genre` of this provider.
  String get genre;
}

class _SongsByGenreCountProviderElement
    extends AutoDisposeStreamProviderElement<int> with SongsByGenreCountRef {
  _SongsByGenreCountProviderElement(super.provider);

  @override
  String get genre => (origin as SongsByGenreCountProvider).genre;
}

String _$songsListHash() => r'4b037f06bb871a60a805fcb2e5769be3199b3e94';

/// See also [songsList].
@ProviderFor(songsList)
const songsListProvider = SongsListFamily();

/// See also [songsList].
class SongsListFamily extends Family<AsyncValue<IList<Song>>> {
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
class SongsListProvider extends AutoDisposeStreamProvider<IList<Song>> {
  /// See also [songsList].
  SongsListProvider(
    ListQuery opt,
  ) : this._internal(
          (ref) => songsList(
            ref as SongsListRef,
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
          opt: opt,
        );

  SongsListProvider._internal(
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
    Stream<IList<Song>> Function(SongsListRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: SongsListProvider._internal(
        (ref) => create(ref as SongsListRef),
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
  AutoDisposeStreamProviderElement<IList<Song>> createElement() {
    return _SongsListProviderElement(this);
  }

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

mixin SongsListRef on AutoDisposeStreamProviderRef<IList<Song>> {
  /// The parameter `opt` of this provider.
  ListQuery get opt;
}

class _SongsListProviderElement
    extends AutoDisposeStreamProviderElement<IList<Song>> with SongsListRef {
  _SongsListProviderElement(super.provider);

  @override
  ListQuery get opt => (origin as SongsListProvider).opt;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member
