// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Galician (`gl`).
class AppLocalizationsGl extends AppLocalizations {
  AppLocalizationsGl([String locale = 'gl']) : super(locale);

  @override
  String get actionsCancel => 'Cancelar';

  @override
  String get actionsDelete => 'Eliminar';

  @override
  String get actionsDownload => 'Descargar';

  @override
  String get actionsDownloadCancel => 'Cancelar a descarga';

  @override
  String get actionsDownloadDelete => 'Eliminar o descargado';

  @override
  String get actionsOk => 'OK';

  @override
  String get actionsStar => 'Estrela';

  @override
  String get actionsUnstar => 'Retirar';

  @override
  String get controlsShuffle => 'Barallar';

  @override
  String get messagesNothingHere => 'Nada por aquí…';

  @override
  String get navigationTabsHome => 'Inicio';

  @override
  String get navigationTabsLibrary => 'Biblioteca';

  @override
  String get navigationTabsSearch => 'Buscar';

  @override
  String get navigationTabsSettings => 'Axustes';

  @override
  String get resourcesAlbumActionsPlay => 'Reproducir';

  @override
  String get resourcesAlbumActionsView => 'Ver Álbum';

  @override
  String resourcesAlbumCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count álbums',
      one: '$count álbum',
    );
    return '$_temp0';
  }

  @override
  String get resourcesAlbumListsSort => 'Ordenar Álbums';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Álbums',
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
      other: '$count artistas',
      one: '$count artista',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistListsSort => 'Ordenar Artistas';

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
  String get resourcesFilterAlbum => 'Álbum';

  @override
  String get resourcesFilterArtist => 'Artista';

  @override
  String get resourcesFilterGenre => 'Por xénero';

  @override
  String get resourcesFilterOwner => 'Dono';

  @override
  String get resourcesFilterStarred => 'Favoritas';

  @override
  String get resourcesFilterYear => 'Ano';

  @override
  String get resourcesPlaylistActionsPlay => 'Reproducir lista';

  @override
  String resourcesPlaylistCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count listas',
      one: '$count lista',
    );
    return '$_temp0';
  }

  @override
  String resourcesPlaylistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Listaxes',
      one: 'Listaxe',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Filas',
      one: 'Fila',
    );
    return '$_temp0';
  }

  @override
  String resourcesSongCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count cancións',
      one: '$count canción',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSongListDeleteAllContent =>
      'Vas eliminar todas as cancións descargadas.';

  @override
  String get resourcesSongListDeleteAllTitle => 'Eliminar descargas?';

  @override
  String get resourcesSongListsArtistTopSongs => 'Máis reproducidas';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Cancións',
      one: 'Canción',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Últimas engadidas';

  @override
  String get resourcesSortByAlbum => 'Álbum';

  @override
  String get resourcesSortByAlbumCount => 'Número de álbums';

  @override
  String get resourcesSortByArtist => 'Por artista';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Reproducidas a miúdo';

  @override
  String get resourcesSortByName => 'Por nome';

  @override
  String get resourcesSortByRandom => 'Ao chou';

  @override
  String get resourcesSortByRecentlyPlayed => 'Reproducidas a miúdo';

  @override
  String get resourcesSortByTitle => 'Título';

  @override
  String get resourcesSortByUpdated => 'Actualizado recentemente';

  @override
  String get resourcesSortByYear => 'Por ano';

  @override
  String searchHeaderTitle(String query) {
    return 'Buscar: $query';
  }

  @override
  String get searchInputPlaceholder => 'Buscar';

  @override
  String get searchMoreResults => 'Máis…';

  @override
  String get searchNowPlayingContext => 'Resultados';

  @override
  String get settingsAboutActionsLicenses => 'Licenzas';

  @override
  String get settingsAboutActionsProjectHomepage => 'Web do Proxecto';

  @override
  String get settingsAboutActionsSupport => 'Axuda ao desenvolvemento 💜';

  @override
  String get settingsAboutName => 'Acerca de';

  @override
  String get settingsAboutShareLogs => 'Compartir rexistros';

  @override
  String get settingsAboutChooseLog => 'Escolle un ficheiro de rexistro';

  @override
  String settingsAboutVersion(String version) {
    return 'versión $version';
  }

  @override
  String get settingsMusicName => 'Música';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Non rexistrar o historial de reprodución';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Rexistrar o historial de reprodución';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Rexistrar reprodución';

  @override
  String get settingsNetworkName => 'Rede';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Bitrate máx. (móbil)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Bitrate máx. (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Tempo máximo na memoria';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Tempo mínimo na memoria';

  @override
  String get settingsNetworkOptionsOfflineMode => 'Modo sen conexión';

  @override
  String get settingsNetworkOptionsOfflineModeOff =>
      'Usa internet para sincr. música.';

  @override
  String get settingsNetworkOptionsOfflineModeOn =>
      'Non usar internet para sincr. ou reproducir música.';

  @override
  String get settingsNetworkOptionsStreamFormat =>
      'Modo de reprodución preferido';

  @override
  String get settingsNetworkOptionsStreamFormatServerDefault =>
      'Usar por defecto do servidor';

  @override
  String settingsNetworkValuesKbps(String value) {
    return '${value}kbps';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value segundos';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Sen límite';

  @override
  String get settingsResetActionsClearImageCache => 'Limpar a caché de imaxes';

  @override
  String get settingsResetName => 'Restablecer';

  @override
  String get settingsServersActionsAdd => 'Engadir Servidor';

  @override
  String get settingsServersActionsDelete => 'Eliminar';

  @override
  String get settingsServersActionsEdit => 'Editar Servidor';

  @override
  String get settingsServersActionsSave => 'Gardar';

  @override
  String get settingsServersActionsTestConnection => 'Comprobar Conexión';

  @override
  String get settingsServersFieldsAddress => 'Enderezo';

  @override
  String get settingsServersFieldsName => 'Nome';

  @override
  String get settingsServersFieldsPassword => 'Contrasinal';

  @override
  String get settingsServersFieldsUsername => 'Identificador';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Fallou a conexión a $address, comproba os axustes';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Conexión con $address OK!';
  }

  @override
  String get settingsServersName => 'Servidores';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Enviar contrasinal como token + salt';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Enviar contrasinal en texto plano (herdado, pon coidado en que a conexión sexa segura!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Forzar contrasinal en texto plano';
}
