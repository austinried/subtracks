// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Danish (`da`).
class AppLocalizationsDa extends AppLocalizations {
  AppLocalizationsDa([String locale = 'da']) : super(locale);

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
  String get actionsStar => 'Star';

  @override
  String get actionsUnstar => 'Unstar';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'Intet her…';

  @override
  String get navigationTabsHome => 'Hjem';

  @override
  String get navigationTabsLibrary => 'Bibliotek';

  @override
  String get navigationTabsSearch => 'Søg';

  @override
  String get navigationTabsSettings => 'Indstillinger';

  @override
  String get resourcesAlbumActionsPlay => 'Afspil album';

  @override
  String get resourcesAlbumActionsView => 'Se album';

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
  String get resourcesAlbumListsSort => 'Sortér albums';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Albums',
      one: 'Album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Se kunstnere';

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
  String get resourcesArtistListsSort => 'Sortér kunstnere';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Kunstnere',
      one: 'Kunstner',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Efter genre';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Starred';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Afspil spilleliste';

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
      other: 'Spillelister',
      one: 'Spilleliste',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Køer',
      one: 'Kø',
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
  String get resourcesSongListsArtistTopSongs => 'Top sange';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Sange',
      one: 'Sang',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Recently added';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Efter kunstner';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Frequently played';

  @override
  String get resourcesSortByName => 'Efter navn';

  @override
  String get resourcesSortByRandom => 'Tilfældig';

  @override
  String get resourcesSortByRecentlyPlayed => 'Recently played';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Efter år';

  @override
  String searchHeaderTitle(String query) {
    return 'Søg: $query';
  }

  @override
  String get searchInputPlaceholder => 'Søg';

  @override
  String get searchMoreResults => 'Mere…';

  @override
  String get searchNowPlayingContext => 'Søgeresultater';

  @override
  String get settingsAboutActionsLicenses => 'Licenser';

  @override
  String get settingsAboutActionsProjectHomepage => 'Projekt hjemmeside';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'Omkring';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'version $version';
  }

  @override
  String get settingsMusicName => 'Musik';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Don\'t scrobble play history';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Scrobble afspilningshistorik';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Scrobble afspilninger';

  @override
  String get settingsNetworkName => 'Netværk';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Maksimum bitrate (mobil)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Maksimum bitrate (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Maksimum buffertid';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Minimum buffertid';

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
    return '$value sekunder';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Ubegrænset';

  @override
  String get settingsResetActionsClearImageCache => 'Ryd billede cache';

  @override
  String get settingsResetName => 'Nulstil';

  @override
  String get settingsServersActionsAdd => 'Tilføj server';

  @override
  String get settingsServersActionsDelete => 'Slet';

  @override
  String get settingsServersActionsEdit => 'Redigér server';

  @override
  String get settingsServersActionsSave => 'Gem';

  @override
  String get settingsServersActionsTestConnection => 'Test forbindelse';

  @override
  String get settingsServersFieldsAddress => 'Adresse';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Adgangskode';

  @override
  String get settingsServersFieldsUsername => 'Brugernavn';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Forbindelse til $address mislykkedes, tjek indstillinger eller server';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Forbindelse til $address OK!';
  }

  @override
  String get settingsServersName => 'Servere';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Send password as token + salt';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Send password in plaintext (legacy, make sure your connection is secure!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Force plaintext password';
}
