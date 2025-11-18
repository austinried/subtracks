// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for German (`de`).
class AppLocalizationsDe extends AppLocalizations {
  AppLocalizationsDe([String locale = 'de']) : super(locale);

  @override
  String get actionsCancel => 'Abbrechen';

  @override
  String get actionsDelete => 'Löschen';

  @override
  String get actionsDownload => 'Herunterladen';

  @override
  String get actionsDownloadCancel => 'Download abbrechen';

  @override
  String get actionsDownloadDelete => 'Heruntergeladene Inhalte löschen';

  @override
  String get actionsOk => 'OK';

  @override
  String get actionsStar => 'Markieren';

  @override
  String get actionsUnstar => 'Markierung entfernen';

  @override
  String get controlsShuffle => 'Zufall';

  @override
  String get messagesNothingHere => 'Hier ist nichts…';

  @override
  String get navigationTabsHome => 'Startseite';

  @override
  String get navigationTabsLibrary => 'Bibliothek';

  @override
  String get navigationTabsSearch => 'Suche';

  @override
  String get navigationTabsSettings => 'Einstellungen';

  @override
  String get resourcesAlbumActionsPlay => 'Album abspielen';

  @override
  String get resourcesAlbumActionsView => 'Album anzeigen';

  @override
  String resourcesAlbumCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count Alben',
      one: '$count Album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesAlbumListsSort => 'Alben sortieren';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Alben',
      one: 'Album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Interpret anzeigen';

  @override
  String resourcesArtistCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count Künstler',
      one: '$count Künstler',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistListsSort => 'Interpreten sortieren';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Interpreten',
      one: 'Interpret',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Künstler';

  @override
  String get resourcesFilterGenre => 'Nach Genre';

  @override
  String get resourcesFilterOwner => 'Besitzer';

  @override
  String get resourcesFilterStarred => 'Favoriten';

  @override
  String get resourcesFilterYear => 'Jahr';

  @override
  String get resourcesPlaylistActionsPlay => 'Wiedergabeliste abspielen';

  @override
  String resourcesPlaylistCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count Playlists',
      one: '$count Playlist',
    );
    return '$_temp0';
  }

  @override
  String resourcesPlaylistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Wiedergabelisten',
      one: 'Wiedergabeliste',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Warteschlangen',
      one: 'Warteschlange',
    );
    return '$_temp0';
  }

  @override
  String resourcesSongCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count Songs',
      one: '$count Song',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSongListDeleteAllContent =>
      'Hierdurch werden alle heruntergeladenen Inhalte entfernt.';

  @override
  String get resourcesSongListDeleteAllTitle => 'Downloads löschen?';

  @override
  String get resourcesSongListsArtistTopSongs => 'Top Lieder';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Lieder',
      one: 'Lied',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Kürzlich hinzugefügt';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Albenanzahl';

  @override
  String get resourcesSortByArtist => 'Nach Interpreten';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Häufig abgespielt';

  @override
  String get resourcesSortByName => 'Nach Name';

  @override
  String get resourcesSortByRandom => 'Zufällig';

  @override
  String get resourcesSortByRecentlyPlayed => 'Kürzlich abgespielt';

  @override
  String get resourcesSortByTitle => 'Titel';

  @override
  String get resourcesSortByUpdated => 'Kürzlich hinzugefügt';

  @override
  String get resourcesSortByYear => 'Nach Jahr';

  @override
  String searchHeaderTitle(String query) {
    return 'Suche: $query';
  }

  @override
  String get searchInputPlaceholder => 'Suche';

  @override
  String get searchMoreResults => 'Mehr…';

  @override
  String get searchNowPlayingContext => 'Suchergebnis';

  @override
  String get settingsAboutActionsLicenses => 'Lizenzen';

  @override
  String get settingsAboutActionsProjectHomepage => 'Projektseite';

  @override
  String get settingsAboutActionsSupport => 'Den Entwickler unterstützen';

  @override
  String get settingsAboutName => 'Über';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'Version $version';
  }

  @override
  String get settingsMusicName => 'Musik';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Kein Scrobble für Wiedergabeverlauf';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Scrobble Wiedergabeverlauf';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Scrobble Wiedergabe';

  @override
  String get settingsNetworkName => 'Netzwerk';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Maximale Bitrate (Mobil)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Maximale Bitrate (WLAN)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Maximale Pufferzeit';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Minimale Pufferzeit';

  @override
  String get settingsNetworkOptionsOfflineMode => 'Offline Modus';

  @override
  String get settingsNetworkOptionsOfflineModeOff =>
      'Nutze das Internet um Musik zu synchronisieren.';

  @override
  String get settingsNetworkOptionsOfflineModeOn =>
      'Nutze nicht das Internet um Musik zu synchronisieren.';

  @override
  String get settingsNetworkOptionsStreamFormat =>
      'Bevorzugtes Streaming-Format';

  @override
  String get settingsNetworkOptionsStreamFormatServerDefault =>
      'Server-Standard verwenden';

  @override
  String settingsNetworkValuesKbps(String value) {
    return '${value}kbps';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value Sekunden';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Unbegrenzt';

  @override
  String get settingsResetActionsClearImageCache =>
      'Bildzwischenspeicher löschen';

  @override
  String get settingsResetName => 'Zurücksetzen';

  @override
  String get settingsServersActionsAdd => 'Server hinzufügen';

  @override
  String get settingsServersActionsDelete => 'Löschen';

  @override
  String get settingsServersActionsEdit => 'Server bearbeiten';

  @override
  String get settingsServersActionsSave => 'Speichern';

  @override
  String get settingsServersActionsTestConnection => 'Verbindung testen';

  @override
  String get settingsServersFieldsAddress => 'Adresse';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Passwort';

  @override
  String get settingsServersFieldsUsername => 'Nutzername';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Verbindung zu $address fehlgeschlagen, überprüfe Einstellungen oder Server';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Verbindung zu $address ist OK!';
  }

  @override
  String get settingsServersName => 'Server';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Sende Passwort als Token + Salt';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Passwort als Klartext senden (Veraltet, stellen Sie sicher, dass Ihre Verbindung sicher ist!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Erzwinge Klartextpasswort';
}
