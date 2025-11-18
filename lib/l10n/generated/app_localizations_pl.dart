// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Polish (`pl`).
class AppLocalizationsPl extends AppLocalizations {
  AppLocalizationsPl([String locale = 'pl']) : super(locale);

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
  String get actionsStar => 'Ulubione';

  @override
  String get actionsUnstar => 'Usuń ulubione';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'Pusto tu…';

  @override
  String get navigationTabsHome => 'Strona główna';

  @override
  String get navigationTabsLibrary => 'Kolekcja';

  @override
  String get navigationTabsSearch => 'Wyszukaj';

  @override
  String get navigationTabsSettings => 'Ustawienia';

  @override
  String get resourcesAlbumActionsPlay => 'Otwarzaj album';

  @override
  String get resourcesAlbumActionsView => 'Zobacz album';

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
  String get resourcesAlbumListsSort => 'Sortowanie albumów';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Albumów',
      many: 'Albumów',
      few: 'Albumy',
      one: 'Album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Zobacz wykonawcę';

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
  String get resourcesArtistListsSort => 'Sortowanie wykonawców';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Wykonawców',
      many: 'Wykonawców',
      few: 'Wykonawcy',
      one: 'Wykonawca',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Wg gatunku';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Ulubione';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Odtwarzaj playlistę';

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
      many: 'Playlist',
      few: 'Playlisty',
      one: 'Playlista',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Kolejek',
      many: 'Kolejek',
      few: 'Kolejki',
      one: 'Kolejka',
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
  String get resourcesSongListsArtistTopSongs => 'Najpopularniejsze utwory';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Utworów',
      many: 'Utworów',
      few: 'Utwory',
      one: 'Utwór',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Ostatnio dodane';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Wg wykonawcy';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Często odtwarzane';

  @override
  String get resourcesSortByName => 'Wg nazwy';

  @override
  String get resourcesSortByRandom => 'Losowo';

  @override
  String get resourcesSortByRecentlyPlayed => 'Ostatnio odtwarzane';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Wg roku';

  @override
  String searchHeaderTitle(String query) {
    return 'Szukaj: $query';
  }

  @override
  String get searchInputPlaceholder => 'Szukaj';

  @override
  String get searchMoreResults => 'Więcej…';

  @override
  String get searchNowPlayingContext => 'Wyniki wyszukiwania';

  @override
  String get settingsAboutActionsLicenses => 'Licencje';

  @override
  String get settingsAboutActionsProjectHomepage => 'Strona główna projektu';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'Informacje o projekcie';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'wersja $version';
  }

  @override
  String get settingsMusicName => 'Muzyka';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Nie śledź swojej historii odtwarzania';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Śledź swoją histrorię odtwarzania';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Śledzenie odtworzeń';

  @override
  String get settingsNetworkName => 'Sieć';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Maksymalny bitrate (sieć komórkowa)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Maksymalny bitrate (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle =>
      'Maksymalny czas buforowania';

  @override
  String get settingsNetworkOptionsMinBufferTitle =>
      'Minimalny czas buforowania';

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
    return '$value sekund';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Bez ograniczeń';

  @override
  String get settingsResetActionsClearImageCache =>
      'Wyczyść pamięć podręczną obrazów';

  @override
  String get settingsResetName => 'Przywracanie';

  @override
  String get settingsServersActionsAdd => 'Dodaj serwer';

  @override
  String get settingsServersActionsDelete => 'Usuń';

  @override
  String get settingsServersActionsEdit => 'Edytuj serwer';

  @override
  String get settingsServersActionsSave => 'Zapisz';

  @override
  String get settingsServersActionsTestConnection => 'Przetestuj połączenie';

  @override
  String get settingsServersFieldsAddress => 'Adres serwera';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Hasło';

  @override
  String get settingsServersFieldsUsername => 'Nazwa użytkownika';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Błąd połączenia z $address, sprawdź ustawienia lub serwer';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Połączeno poprawnie z $address!';
  }

  @override
  String get settingsServersName => 'Serwery';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Wyślij hasło jako token i ciąg zaburzający';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Wyślij hasło jako tekst (przestarzałe, upewnij się, że Twoje połączenie jest zabezpieczone)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Wymuś hasło jako tekst (plaintext)';
}
