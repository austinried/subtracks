// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Italian (`it`).
class AppLocalizationsIt extends AppLocalizations {
  AppLocalizationsIt([String locale = 'it']) : super(locale);

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
  String get actionsStar => 'Aggiungi ai preferiti';

  @override
  String get actionsUnstar => 'Rimuovi dai preferiti';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'Non c\'è niente qui…';

  @override
  String get navigationTabsHome => 'Home';

  @override
  String get navigationTabsLibrary => 'Libreria';

  @override
  String get navigationTabsSearch => 'Cerca';

  @override
  String get navigationTabsSettings => 'Impostazioni';

  @override
  String get resourcesAlbumActionsPlay => 'Riproduci album';

  @override
  String get resourcesAlbumActionsView => 'Vedi album';

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
  String get resourcesAlbumListsSort => 'Ordina album';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Album',
      one: 'Album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Vedi artista';

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
  String get resourcesArtistListsSort => 'Ordina artisti';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Artisti',
      one: 'Artista',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Per genere';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Preferiti';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Riproduci playlist';

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
      other: 'Playlist',
      one: 'Playlist',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Code',
      one: 'Coda',
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
  String get resourcesSongListsArtistTopSongs => 'Brani più popolari';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Brani',
      one: 'Brano',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Aggiunti di recente';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Per artista';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Ascoltati frequentemente';

  @override
  String get resourcesSortByName => 'Per nome';

  @override
  String get resourcesSortByRandom => 'Casuale';

  @override
  String get resourcesSortByRecentlyPlayed => 'Ascoltati di recente';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Per anno';

  @override
  String searchHeaderTitle(String query) {
    return 'Ricerca: $query';
  }

  @override
  String get searchInputPlaceholder => 'Ricerca';

  @override
  String get searchMoreResults => 'Mostra di più…';

  @override
  String get searchNowPlayingContext => 'Risultati della ricerca';

  @override
  String get settingsAboutActionsLicenses => 'Licenze';

  @override
  String get settingsAboutActionsProjectHomepage =>
      'Pagina principale del progetto';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'Informazioni';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'versione $version';
  }

  @override
  String get settingsMusicName => 'Musica';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Non eseguire lo scrobbling della cronologia d\'ascolto';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Scrobbling della cronologia di ascolto';

  @override
  String get settingsMusicOptionsScrobbleTitle =>
      'Scrobbling delle riproduzioni';

  @override
  String get settingsNetworkName => 'Rete';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Bitrate massimo (rete dati)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Bitrate massimo (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Tempo di buffer massimo';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Tempo di buffer minimo';

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
    return '${value}kbps';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value secondi';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Illimitato';

  @override
  String get settingsResetActionsClearImageCache =>
      'Pulisci la cache delle immagini';

  @override
  String get settingsResetName => 'Reimposta';

  @override
  String get settingsServersActionsAdd => 'Aggiungi server';

  @override
  String get settingsServersActionsDelete => 'Rimuovi';

  @override
  String get settingsServersActionsEdit => 'Modifica server';

  @override
  String get settingsServersActionsSave => 'Salva';

  @override
  String get settingsServersActionsTestConnection => 'Prova connessione';

  @override
  String get settingsServersFieldsAddress => 'Indirizzo';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Password';

  @override
  String get settingsServersFieldsUsername => 'Nome utente';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Connessione a $address fallita, controlla le impostazioni o il server';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Connesso a $address con successo!';
  }

  @override
  String get settingsServersName => 'Server';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Invia la password come token + salt';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Invia password in chiaro (deprecato, assicurati che la tua connessione sia sicura!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Forza password in chiaro';
}
