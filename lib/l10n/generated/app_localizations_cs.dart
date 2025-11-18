// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Czech (`cs`).
class AppLocalizationsCs extends AppLocalizations {
  AppLocalizationsCs([String locale = 'cs']) : super(locale);

  @override
  String get actionsCancel => 'Zrušit';

  @override
  String get actionsDelete => 'Smazat';

  @override
  String get actionsDownload => 'Stáhnout';

  @override
  String get actionsDownloadCancel => 'Zrušit stahování';

  @override
  String get actionsDownloadDelete => 'Smazat stažené';

  @override
  String get actionsOk => 'OK';

  @override
  String get actionsStar => 'Ohodnotit';

  @override
  String get actionsUnstar => 'Zrušit hodnocení';

  @override
  String get controlsShuffle => 'Náhodně';

  @override
  String get messagesNothingHere => 'Zde nic není…';

  @override
  String get navigationTabsHome => 'Domů';

  @override
  String get navigationTabsLibrary => 'Knihovna';

  @override
  String get navigationTabsSearch => 'Hledat';

  @override
  String get navigationTabsSettings => 'Nastavení';

  @override
  String get resourcesAlbumActionsPlay => 'Přehrát album';

  @override
  String get resourcesAlbumActionsView => 'Zobrazit album';

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
  String get resourcesAlbumListsSort => 'Seřadit alba';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Alba',
      many: 'Alba',
      few: 'Alba',
      one: 'Album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Zobrazit umělce';

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
  String get resourcesArtistListsSort => 'Seřadit umělce';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Umělci',
      many: 'Umělci',
      few: 'Umělci',
      one: 'Umělec',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Umělec';

  @override
  String get resourcesFilterGenre => 'Podle žánru';

  @override
  String get resourcesFilterOwner => 'Majitele';

  @override
  String get resourcesFilterStarred => 'Ohodnocené';

  @override
  String get resourcesFilterYear => 'Rok';

  @override
  String get resourcesPlaylistActionsPlay => 'Přehrát seznam skladeb';

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
      other: 'Seznamy skladeb',
      many: 'Seznamy skladeb',
      few: 'Seznamy skladeb',
      one: 'Seznam skladeb',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Fronty',
      many: 'Fronty',
      few: 'Fronty',
      one: 'Fronta',
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
      'Toto odstraní všechny stažené soubory s hudbou.';

  @override
  String get resourcesSongListDeleteAllTitle => 'Smazat stažené?';

  @override
  String get resourcesSongListsArtistTopSongs => 'Top skladby';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Skladby',
      many: 'Skladby',
      few: 'Skladby',
      one: 'Skladba',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Nedávno přidané';

  @override
  String get resourcesSortByAlbum => 'Alba';

  @override
  String get resourcesSortByAlbumCount => 'Počtu alb';

  @override
  String get resourcesSortByArtist => 'Umělce';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Často přehrávané';

  @override
  String get resourcesSortByName => 'Názvu';

  @override
  String get resourcesSortByRandom => 'Náhodně';

  @override
  String get resourcesSortByRecentlyPlayed => 'Často přehrávané';

  @override
  String get resourcesSortByTitle => 'Názvu';

  @override
  String get resourcesSortByUpdated => 'Naposledy upravené';

  @override
  String get resourcesSortByYear => 'Roku';

  @override
  String searchHeaderTitle(String query) {
    return 'Hledat: $query';
  }

  @override
  String get searchInputPlaceholder => 'Hledat';

  @override
  String get searchMoreResults => 'Více…';

  @override
  String get searchNowPlayingContext => 'Výsledky hledání';

  @override
  String get settingsAboutActionsLicenses => 'Licence';

  @override
  String get settingsAboutActionsProjectHomepage => 'Stránka projektu';

  @override
  String get settingsAboutActionsSupport => 'Podpořit vývojáře 💜';

  @override
  String get settingsAboutName => 'O aplikaci';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'verze $version';
  }

  @override
  String get settingsMusicName => 'Music';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Don\'t scrobble play history';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Scrobble play history';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Scrobble plays';

  @override
  String get settingsNetworkName => 'Síť';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Maximální datový tok (mobil)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Maximální datový tok (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Maximum buffer time';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Minimum buffer time';

  @override
  String get settingsNetworkOptionsOfflineMode => 'Offline mode';

  @override
  String get settingsNetworkOptionsOfflineModeOff =>
      'Use the internet to sync music.';

  @override
  String get settingsNetworkOptionsOfflineModeOn =>
      'Don\'t use the internet to sync or play music.';

  @override
  String get settingsNetworkOptionsStreamFormat =>
      'Preferovaný formát pro streamování';

  @override
  String get settingsNetworkOptionsStreamFormatServerDefault =>
      'Použít nastavení serveru';

  @override
  String settingsNetworkValuesKbps(String value) {
    return '${value}kbps';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value sekund';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Neomezeno';

  @override
  String get settingsResetActionsClearImageCache => 'Smazat mezipaměť obrázků';

  @override
  String get settingsResetName => 'Resetovat';

  @override
  String get settingsServersActionsAdd => 'Přidat server';

  @override
  String get settingsServersActionsDelete => 'Odstranit';

  @override
  String get settingsServersActionsEdit => 'Upravit server';

  @override
  String get settingsServersActionsSave => 'Uložit';

  @override
  String get settingsServersActionsTestConnection => 'Otestovat spojení';

  @override
  String get settingsServersFieldsAddress => 'Adresa';

  @override
  String get settingsServersFieldsName => 'Jméno';

  @override
  String get settingsServersFieldsPassword => 'Heslo';

  @override
  String get settingsServersFieldsUsername => 'Uživ. jméno';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Připojení k $address selhalo, zkontrolujte nastavení nebo server';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Připojení k $address je OK!';
  }

  @override
  String get settingsServersName => 'Servery';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Posílat heslo jako token + salt';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Posílat heslo v prostém textu (zastaralé, ujistěte se, že je vaše připojení zabezpečené!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Vynutit heslo ve formátu prostého textu';
}
