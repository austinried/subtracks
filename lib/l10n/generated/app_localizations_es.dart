// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Spanish Castilian (`es`).
class AppLocalizationsEs extends AppLocalizations {
  AppLocalizationsEs([String locale = 'es']) : super(locale);

  @override
  String get actionsCancel => 'Cancelar';

  @override
  String get actionsDelete => 'Borrar';

  @override
  String get actionsDownload => 'Descargar';

  @override
  String get actionsDownloadCancel => 'Anular descargar';

  @override
  String get actionsDownloadDelete => 'Eliminar descargado';

  @override
  String get actionsOk => 'Ok';

  @override
  String get actionsStar => 'Favorito';

  @override
  String get actionsUnstar => 'Retirar favorito';

  @override
  String get controlsShuffle => 'Reproducir aleatoriamente';

  @override
  String get messagesNothingHere => 'Nada aquí…';

  @override
  String get navigationTabsHome => 'Casa';

  @override
  String get navigationTabsLibrary => 'Biblioteca';

  @override
  String get navigationTabsSearch => 'Buscar';

  @override
  String get navigationTabsSettings => 'Entorno';

  @override
  String get resourcesAlbumActionsPlay => 'Reproducir Álbum';

  @override
  String get resourcesAlbumActionsView => 'Ver Álbum';

  @override
  String resourcesAlbumCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count albums',
      one: '$count album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesAlbumListsSort => 'Ordenar Álbumes';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Álbumes',
      one: 'Álbum',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Ver Artista';

  @override
  String resourcesArtistCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count artists',
      one: '$count artist',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistListsSort => 'Oredenar Artistas';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Artistas',
      one: 'Artista',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Por Género';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Estrellas';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Reproducir Lista de reproducción';

  @override
  String resourcesPlaylistCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count playlists',
      one: '$count playlist',
    );
    return '$_temp0';
  }

  @override
  String resourcesPlaylistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Listas de reproducción',
      one: 'Lista de reproducción',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Colas',
      one: 'Cola',
    );
    return '$_temp0';
  }

  @override
  String resourcesSongCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count songs',
      one: '$count song',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSongListDeleteAllContent =>
      'This will remove all downloaded song files.';

  @override
  String get resourcesSongListDeleteAllTitle => 'Delete downloads?';

  @override
  String get resourcesSongListsArtistTopSongs => 'Mejores Canciones';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Canciones',
      one: 'Cancion',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Recientemente Añadido';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Por Artista';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Jugado Frecuentemente';

  @override
  String get resourcesSortByName => 'Por Nombre';

  @override
  String get resourcesSortByRandom => 'Aleatorio';

  @override
  String get resourcesSortByRecentlyPlayed => 'Recientemente Jugado';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Por Año';

  @override
  String searchHeaderTitle(String query) {
    return 'Buscar: $query';
  }

  @override
  String get searchInputPlaceholder => 'Buscar';

  @override
  String get searchMoreResults => 'Más…';

  @override
  String get searchNowPlayingContext => 'Resultados de la búsqueda';

  @override
  String get settingsAboutActionsLicenses => 'Licencias';

  @override
  String get settingsAboutActionsProjectHomepage =>
      'Página de inicio del proyecto';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'Información';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'versión $version';
  }

  @override
  String get settingsMusicName => 'Música';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'No hagas historial de reproducción de scrobble';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Historial de reproducción de scrobble';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Obras de teatro de Scrobble';

  @override
  String get settingsNetworkName => 'Sitio';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Tasa de bits máxima (mobile)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Tasa de bits máxima (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Máxima de buffer tiempo';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Mínimo de buffer tiempo';

  @override
  String get settingsNetworkOptionsOfflineMode => 'Offline mode';

  @override
  String get settingsNetworkOptionsOfflineModeOff =>
      'Use the internet to sync music.';

  @override
  String get settingsNetworkOptionsOfflineModeOn =>
      'Don\'t use the internet to sync or play music.';

  @override
  String get settingsNetworkOptionsStreamFormat => 'Preferred stream format';

  @override
  String get settingsNetworkOptionsStreamFormatServerDefault =>
      'Use server default';

  @override
  String settingsNetworkValuesKbps(String value) {
    return '$value kilobytes por segundo';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value segundos';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Ilimitados';

  @override
  String get settingsResetActionsClearImageCache => 'Caché de imágenes claras';

  @override
  String get settingsResetName => 'Reinicializar';

  @override
  String get settingsServersActionsAdd => 'Agregar Servidor';

  @override
  String get settingsServersActionsDelete => 'Supr';

  @override
  String get settingsServersActionsEdit => 'Editar Servidor';

  @override
  String get settingsServersActionsSave => 'Enviar';

  @override
  String get settingsServersActionsTestConnection => 'Conexión de prueba';

  @override
  String get settingsServersFieldsAddress => 'Alocución';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'La contraseña';

  @override
  String get settingsServersFieldsUsername => 'Nombre de usuario';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'La conexión a $address falló, verifique la configuracón o el servidor';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return '¡Conexión con $address Ok!';
  }

  @override
  String get settingsServersName => 'Servidores';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Enviar contraseña como token + sal';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Enviar contraseña en texto plano (¡legado, asegúrese de que su conexión sea segura!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Forzar contraseña de texto sin formato';
}
