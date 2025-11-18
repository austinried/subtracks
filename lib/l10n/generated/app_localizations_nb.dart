// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Norwegian Bokmål (`nb`).
class AppLocalizationsNb extends AppLocalizations {
  AppLocalizationsNb([String locale = 'nb']) : super(locale);

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
  String get actionsStar => 'Stjernemerk';

  @override
  String get actionsUnstar => 'Fjern stjernemerking';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'Ingenting her …';

  @override
  String get navigationTabsHome => 'Hjem';

  @override
  String get navigationTabsLibrary => 'Bibliotek';

  @override
  String get navigationTabsSearch => 'Søk';

  @override
  String get navigationTabsSettings => 'Innstillinger';

  @override
  String get resourcesAlbumActionsPlay => 'Spill album';

  @override
  String get resourcesAlbumActionsView => 'Vis album';

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
  String get resourcesAlbumListsSort => 'Sorter album';

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
  String get resourcesArtistActionsView => 'Vis artist';

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
  String get resourcesArtistListsSort => 'Sorter artister';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Artister',
      one: 'Artist',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Etter sjanger';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Stjernemerket';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Spill av spilleliste';

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
  String get resourcesSongListsArtistTopSongs => 'Toppspor';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Spor',
      one: 'Spor',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Nylig tillagt';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Etter artist';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Ofte spilt';

  @override
  String get resourcesSortByName => 'Etter navn';

  @override
  String get resourcesSortByRandom => 'Tilfeldig';

  @override
  String get resourcesSortByRecentlyPlayed => 'Nylig spilt';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Etter år';

  @override
  String searchHeaderTitle(String query) {
    return 'Søk: $query';
  }

  @override
  String get searchInputPlaceholder => 'Søk';

  @override
  String get searchMoreResults => 'Mer …';

  @override
  String get searchNowPlayingContext => 'Søkeresultater';

  @override
  String get settingsAboutActionsLicenses => 'Lisenser';

  @override
  String get settingsAboutActionsProjectHomepage => 'Prosjekthjemmeside';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'Om';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'versjon $version';
  }

  @override
  String get settingsMusicName => 'Musikk';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Ikke utfør sporinfodeling av avspillingshistorikk';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Sporinfodelings-avspillinghistorikk';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Sporinfodelingsavspillinger';

  @override
  String get settingsNetworkName => 'Nettverk';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Maksimal bitrate (mobil)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Maksimal bitrate (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle =>
      'Maksimal mellomlagringstid';

  @override
  String get settingsNetworkOptionsMinBufferTitle =>
      'Minimal mellomlagringstid';

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
    return '$value sekunder';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Ubegrenset';

  @override
  String get settingsResetActionsClearImageCache => 'Tøm bildehurtiglager';

  @override
  String get settingsResetName => 'Tilbakestill';

  @override
  String get settingsServersActionsAdd => 'Legg til tjener';

  @override
  String get settingsServersActionsDelete => 'Slett';

  @override
  String get settingsServersActionsEdit => 'Rediger tjener';

  @override
  String get settingsServersActionsSave => 'Lagre';

  @override
  String get settingsServersActionsTestConnection => 'Test tilkobling';

  @override
  String get settingsServersFieldsAddress => 'Adresse';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Passord';

  @override
  String get settingsServersFieldsUsername => 'Brukernavn';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Tilkobling til $address mislyktes. Sjekk innstillingene eller tjeneren.';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Tilkobling til $address OK.';
  }

  @override
  String get settingsServersName => 'Tjenere';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Send passord som symbol + salt';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Send passord i klartekst (Foreldet. Forsikre deg om at tilkoblingen er sikker.)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Påtving klartekstspassord';
}
