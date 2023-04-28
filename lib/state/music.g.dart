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

typedef ArtistRef = AutoDisposeStreamProviderRef<Artist>;

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
    this.id,
  ) : super.internal(
          (ref) => artist(
            ref,
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
        );

  final String id;

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

String _$albumHash() => r'914f37fe8bc2d883de2548ce9411aa10e8a9868a';
typedef AlbumRef = AutoDisposeStreamProviderRef<Album>;

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
    this.id,
  ) : super.internal(
          (ref) => album(
            ref,
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
        );

  final String id;

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

String _$albumDownloadStatusHash() =>
    r'0b6c5f09f8327a624172d7ef33e1911c87b2cb60';
typedef AlbumDownloadStatusRef
    = AutoDisposeStreamProviderRef<ListDownloadStatus>;

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
    this.id,
  ) : super.internal(
          (ref) => albumDownloadStatus(
            ref,
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
        );

  final String id;

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

String _$playlistDownloadStatusHash() =>
    r'd563e8746265b2022c59e70869d136cdf6c03ab9';
typedef PlaylistDownloadStatusRef
    = AutoDisposeStreamProviderRef<ListDownloadStatus>;

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
    this.id,
  ) : super.internal(
          (ref) => playlistDownloadStatus(
            ref,
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
        );

  final String id;

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

String _$songHash() => r'bd8ac3b046180c9fa7676e437542d5afe4dcdb7d';
typedef SongRef = AutoDisposeStreamProviderRef<Song>;

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
    this.id,
  ) : super.internal(
          (ref) => song(
            ref,
            id,
          ),
          from: songProvider,
          name: r'songProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product') ? null : _$songHash,
          dependencies: SongFamily._dependencies,
          allTransitiveDependencies: SongFamily._allTransitiveDependencies,
        );

  final String id;

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

String _$albumSongsListHash() => r'dfc357b4c2b6a7dcc5e4701dc395c49ec8c8b88a';
typedef AlbumSongsListRef = AutoDisposeFutureProviderRef<List<Song>>;

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
    this.id,
    this.opt,
  ) : super.internal(
          (ref) => albumSongsList(
            ref,
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
        );

  final String id;
  final ListQuery opt;

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

String _$songsByAlbumListHash() => r'd6ed7fd9d8ceb3c5743b8c5538c7ffd50d2a8284';
typedef SongsByAlbumListRef = AutoDisposeFutureProviderRef<List<Song>>;

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
    this.opt,
  ) : super.internal(
          (ref) => songsByAlbumList(
            ref,
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
        );

  final ListQuery opt;

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

String _$playlistHash() => r'df0c8441c67c487155574caeeb11f4cdfe25ed31';
typedef PlaylistRef = AutoDisposeStreamProviderRef<Playlist>;

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
    this.id,
  ) : super.internal(
          (ref) => playlist(
            ref,
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
        );

  final String id;

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

String _$playlistSongsListHash() => r'c3593829c6c91dc6958df6dc4cb6e89e3e7647a5';
typedef PlaylistSongsListRef = AutoDisposeFutureProviderRef<List<Song>>;

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
    this.id,
    this.opt,
  ) : super.internal(
          (ref) => playlistSongsList(
            ref,
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
        );

  final String id;
  final ListQuery opt;

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

String _$albumsInIdsHash() => r'089ceafa122e1b3ce87c216bf3650cae5f656bdc';
typedef AlbumsInIdsRef = AutoDisposeFutureProviderRef<List<Album>>;

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
    this.ids,
  ) : super.internal(
          (ref) => albumsInIds(
            ref,
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
        );

  final IList<String> ids;

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

String _$albumsByArtistIdHash() => r'e9a5c9255b6c7b6cfb22f4a4bacc4e518787465d';
typedef AlbumsByArtistIdRef = AutoDisposeStreamProviderRef<IList<Album>>;

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
    this.id,
  ) : super.internal(
          (ref) => albumsByArtistId(
            ref,
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
        );

  final String id;

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

String _$albumGenresHash() => r'17ba64391a678a4eb88017d782a843eeb92b2049';
typedef AlbumGenresRef = AutoDisposeStreamProviderRef<IList<String>>;

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
    this.page,
  ) : super.internal(
          (ref) => albumGenres(
            ref,
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
        );

  final Pagination page;

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

String _$albumsByGenreHash() => r'd35cb85a50df93b7f50c28c934ea182b69534b8b';
typedef AlbumsByGenreRef = AutoDisposeStreamProviderRef<IList<Album>>;

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
    this.genre,
    this.page,
  ) : super.internal(
          (ref) => albumsByGenre(
            ref,
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
        );

  final String genre;
  final Pagination page;

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

String _$songsByGenreCountHash() => r'408bc1d82a6dae29e7b9a85fbf6988ba854551f7';
typedef SongsByGenreCountRef = AutoDisposeStreamProviderRef<int>;

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
    this.genre,
  ) : super.internal(
          (ref) => songsByGenreCount(
            ref,
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
        );

  final String genre;

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
// ignore_for_file: unnecessary_raw_strings, subtype_of_sealed_class, invalid_use_of_internal_member, do_not_use_environment, prefer_const_constructors, public_member_api_docs, avoid_private_typedef_functions
