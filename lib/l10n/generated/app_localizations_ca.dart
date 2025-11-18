// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Catalan Valencian (`ca`).
class AppLocalizationsCa extends AppLocalizations {
  AppLocalizationsCa([String locale = 'ca']) : super(locale);

  @override
  String get actionsCancel => 'Cancel';

  @override
  String get actionsDelete => 'Delete';

  @override
  String get actionsDownload => 'Download';

  @override
  String get actionsDownloadCancel => 'Cancel download';

  @override
  String get actionsDownloadDelete => 'Delete downloaded';

  @override
  String get actionsOk => 'OK';

  @override
  String get actionsStar => 'Afegir als favorits';

  @override
  String get actionsUnstar => 'Retirar estrella';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'Aquí no hi ha res…';

  @override
  String get navigationTabsHome => 'Inici';

  @override
  String get navigationTabsLibrary => 'Biblioteca';

  @override
  String get navigationTabsSearch => 'Cercar';

  @override
  String get navigationTabsSettings => 'Paràmetres';

  @override
  String get resourcesAlbumActionsPlay => 'Reproduir l\'àlbum';

  @override
  String get resourcesAlbumActionsView => 'Veure l\'àlbum';

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
  String get resourcesAlbumListsSort => 'Ordenar els àlbums';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Àlbums',
      one: 'Àlbum',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Veure l\'artista';

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
  String get resourcesArtistListsSort => 'Ordenar els artistes';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Artistes',
      one: 'Artista',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Per gènere';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Favorits';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay =>
      'Reproduir la llista de reproducció';

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
      other: 'Llistes de reproducció',
      one: 'Llista de reproducció',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Cues',
      one: 'Cua',
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
  String get resourcesSongListsArtistTopSongs => 'Millors cançons';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Cançons',
      one: 'Cançó',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Afegit recentment';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Per artista';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Escoltat freqüentment';

  @override
  String get resourcesSortByName => 'Pel nom';

  @override
  String get resourcesSortByRandom => 'Aleatori';

  @override
  String get resourcesSortByRecentlyPlayed => 'Reproduït recentment';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Per any';

  @override
  String searchHeaderTitle(String query) {
    return 'Cercar: $query';
  }

  @override
  String get searchInputPlaceholder => 'Cercar';

  @override
  String get searchMoreResults => 'Més…';

  @override
  String get searchNowPlayingContext => 'Resultats de la cerca';

  @override
  String get settingsAboutActionsLicenses => 'Llicències';

  @override
  String get settingsAboutActionsProjectHomepage =>
      'Pàgina d\'inici del projecte';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'Quant a';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'versió $version';
  }

  @override
  String get settingsMusicName => 'Música';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'No capturar l\'historial de reproducció';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Capturar l\'historial de reproduccions';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Capturar la lectura';

  @override
  String get settingsNetworkName => 'Xarxa';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Taxa de bits màxima (mòbil)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Taxa de bits màxima (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle =>
      'Temps màxim en memòria intermèdia';

  @override
  String get settingsNetworkOptionsMinBufferTitle =>
      'Temps mínim en memòria intermèdia';

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
    return '$value kbps';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value segons';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Il·limitat';

  @override
  String get settingsResetActionsClearImageCache =>
      'Esborrar la memòria cau d\'imatges';

  @override
  String get settingsResetName => 'Reinicialitzar';

  @override
  String get settingsServersActionsAdd => 'Afegir un servidor';

  @override
  String get settingsServersActionsDelete => 'Esborrar';

  @override
  String get settingsServersActionsEdit => 'Editar el servidor';

  @override
  String get settingsServersActionsSave => 'Desar';

  @override
  String get settingsServersActionsTestConnection => 'Comprovar la connexió';

  @override
  String get settingsServersFieldsAddress => 'Adreça';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Contrasenya';

  @override
  String get settingsServersFieldsUsername => 'Nom d’usuari';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'La connexió a $address ha fallat, comprova la configuració o el servidor';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Connexió a $address OK!';
  }

  @override
  String get settingsServersName => 'Servidors';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Enviar contrasenya com a token + salt';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Enviar la contrasenya en text sense format (obsolet, assegura\'t que la teva connexió sigui segura!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Forçar la contrasenya de text sense format';
}
