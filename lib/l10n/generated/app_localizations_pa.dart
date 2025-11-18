// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Panjabi Punjabi (`pa`).
class AppLocalizationsPa extends AppLocalizations {
  AppLocalizationsPa([String locale = 'pa']) : super(locale);

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
  String get actionsStar => 'ਤਾਰਾ';

  @override
  String get actionsUnstar => 'ਤਾਰਾ ਹਟਾਓ';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'ਇੱਥੇ ਕੁਝ ਨਹੀਂ ਹੈ…';

  @override
  String get navigationTabsHome => 'ਘਰ';

  @override
  String get navigationTabsLibrary => 'ਲਾਇਬ੍ਰੇਰੀ';

  @override
  String get navigationTabsSearch => 'ਖੋਜ';

  @override
  String get navigationTabsSettings => 'ਸੈਟਿੰਗਾਂ';

  @override
  String get resourcesAlbumActionsPlay => 'ਐਲਬਮ ਚਲਾਓ';

  @override
  String get resourcesAlbumActionsView => 'ਐਲਬਮ ਦੇਖੋ';

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
  String get resourcesAlbumListsSort => 'ਐਲਬਮਾਂ ਨੂੰ ਕਰਮਬੱਧ ਕਰੋ';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'ਐਲਬਮਾਂ',
      one: 'ਐਲਬਮ',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'ਕਲਾਕਾਰ ਦੇਖੋ';

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
  String get resourcesArtistListsSort => 'ਕਲਾਕਾਰਾਂ ਦੀ ਛਾਂਟੀ';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'ਕਲਾਕਾਰਾਂ',
      one: 'ਕਲਾਕਾਰ',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'ਸ਼ੈਲੀ ਦੁਆਰਾ';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'ਸਟਾਰ ਕੀਤੇ ਗਏ';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'ਪਲੇਲਿਸਟ ਚਲਾਓ';

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
      other: 'ਪਲੇਲਿਸਟਸ',
      one: 'ਪਲੇਲਿਸਟ',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'ਕਤਾਰਾਂ',
      one: 'ਕਤਾਰ',
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
  String get resourcesSongListsArtistTopSongs => 'ਉੱਤਮ ਗਾਣੇ';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'ਗਾਣੇ',
      one: 'ਗਾਣਾ',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'ਤਾਜ਼ਾ ਸ਼ਾਮਿਲ';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'ਕਲਾਕਾਰ ਦੁਆਰਾ';

  @override
  String get resourcesSortByFrequentlyPlayed => 'ਅਕਸਰ ਚਲਾਏ ਗਏ';

  @override
  String get resourcesSortByName => 'ਨਾਮ ਦੁਆਰਾ';

  @override
  String get resourcesSortByRandom => 'ਅਟਕਲ-ਪੱਚੂ';

  @override
  String get resourcesSortByRecentlyPlayed => 'ਹਾਲ ਹੀ ਵਿੱਚ ਚਲਾਏ ਗਏ';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'ਸਾਲ ਦੁਆਰਾ';

  @override
  String searchHeaderTitle(String query) {
    return 'ਖੋਜ: $query';
  }

  @override
  String get searchInputPlaceholder => 'ਖੋਜੋ';

  @override
  String get searchMoreResults => 'ਹੋਰ…';

  @override
  String get searchNowPlayingContext => 'ਖੋਜ ਨਤੀਜੇ';

  @override
  String get settingsAboutActionsLicenses => 'ਲਾਇਸੰਸ';

  @override
  String get settingsAboutActionsProjectHomepage => 'ਪ੍ਰੋਜੈਕਟ ਹੋਮਪੇਜ';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'ਬਾਰੇ';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'ਸੰਸਕਰਣ $version';
  }

  @override
  String get settingsMusicName => 'ਸੰਗੀਤ';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'ਸੁਣੇ ਹੋਏ ਗਾਣੇ ਸਕ੍ਰੋਬਲ ਨਾ ਕਰੋ';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'ਸੁਣੇ ਹੋਏ ਗਾਣੇ ਸਕ੍ਰੋਬਲ ਕਰੋ';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'ਗਾਣੇ ਸਕ੍ਰੋਬਲ ਕਰੋ';

  @override
  String get settingsNetworkName => 'ਨੈੱਟਵਰਕ';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'ਵੱਧ ਤੋਂ ਵੱਧ ਬਿੱਟਰੇਟ (ਮੋਬਾਈਲ)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'ਵੱਧ ਤੋਂ ਵੱਧ ਬਿੱਟਰੇਟ (ਵਾਈ-ਫਾਈ)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'ਵੱਧ ਤੋਂ ਵੱਧ ਬਫਰ ਸਮਾਂ';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'ਘੱਟੋ-ਘੱਟ ਬਫਰ ਸਮਾਂ';

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
    return '$value ਸਕਿੰਟ';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'ਅਸੀਮਿਤ';

  @override
  String get settingsResetActionsClearImageCache => 'ਚਿੱਤਰ ਕੈਸ਼ ਸਾਫ਼ ਕਰੋ';

  @override
  String get settingsResetName => 'ਰੀਸੈਟ ਕਰੋ';

  @override
  String get settingsServersActionsAdd => 'ਸਰਵਰ ਸ਼ਾਮਲ ਕਰੋ';

  @override
  String get settingsServersActionsDelete => 'ਮਿਟਾਓ';

  @override
  String get settingsServersActionsEdit => 'ਸਰਵਰ ਦਾ ਸੰਪਾਦਨ';

  @override
  String get settingsServersActionsSave => 'ਸੇਵ ਕਰੋ';

  @override
  String get settingsServersActionsTestConnection => 'ਟੈਸਟ ਕਨੈਕਸ਼ਨ';

  @override
  String get settingsServersFieldsAddress => 'ਪਤਾ';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'ਪਾਸਵਰਡ';

  @override
  String get settingsServersFieldsUsername => 'ਯੂਜ਼ਰਨੇਮ';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return '$address ਨਾਲ ਕਨੈਕਸ਼ਨ ਅਸਫਲ, ਸੈਟਿੰਗਾਂ ਜਾਂ ਸਰਵਰ ਦੀ ਜਾਂਚ ਕਰੋ';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return '$address ਨਾਲ ਕਨੈਕਸ਼ਨ ਠੀਕ ਹੈ!';
  }

  @override
  String get settingsServersName => 'ਸਰਵਰ';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'ਪਾਸਵਰਡ ਨੂੰ ਟੋਕਨ + ਸਾਲ੍ਟ ਵਜੋਂ ਭੇਜੋ';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'ਪਾਸਵਰਡ ਨੂੰ ਸਾਦੇ ਟੈਕਸਟ ਵਿੱਚ ਭੇਜੋ (ਪੁਰਾਣਾ, ਯਕੀਨੀ ਬਣਾਓ ਕਿ ਤੁਹਾਡਾ ਕਨੈਕਸ਼ਨ ਸੁਰੱਖਿਅਤ ਹੈ!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'ਸਾਦੇ ਪਾਸਵਰਡ ਨੂੰ ਜਬਰੀ ਕਰੋ';
}
