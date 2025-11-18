// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Japanese (`ja`).
class AppLocalizationsJa extends AppLocalizations {
  AppLocalizationsJa([String locale = 'ja']) : super(locale);

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
  String get messagesNothingHere => 'Nothing here…';

  @override
  String get navigationTabsHome => 'ホーム';

  @override
  String get navigationTabsLibrary => 'ライブラリ';

  @override
  String get navigationTabsSearch => '検索';

  @override
  String get navigationTabsSettings => '設定';

  @override
  String get resourcesAlbumActionsPlay => 'Play album';

  @override
  String get resourcesAlbumActionsView => 'View album';

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
  String get resourcesAlbumListsSort => 'Sort albums';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'アルバム',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'View artist';

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
  String get resourcesArtistListsSort => 'Sort artists';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'アーティスト',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Genre';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => '星付きアルバム';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Play playlist';

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
      other: 'プレイリスト',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Queues',
      one: 'Queue',
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
  String get resourcesSongListsArtistTopSongs => '人気曲';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '歌',
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
  String get resourcesSortByArtist => 'Artist';

  @override
  String get resourcesSortByFrequentlyPlayed => 'よく聴くアルバム';

  @override
  String get resourcesSortByName => 'Name';

  @override
  String get resourcesSortByRandom => 'ランダムアルバム';

  @override
  String get resourcesSortByRecentlyPlayed => '最近再生した';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Year';

  @override
  String searchHeaderTitle(String query) {
    return 'Search: $query';
  }

  @override
  String get searchInputPlaceholder => '検索';

  @override
  String get searchMoreResults => 'More…';

  @override
  String get searchNowPlayingContext => 'Search results';

  @override
  String get settingsAboutActionsLicenses => 'Licenses';

  @override
  String get settingsAboutActionsProjectHomepage => 'ホームページ';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'About';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'version $version';
  }

  @override
  String get settingsMusicName => '音楽';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Don\'t scrobble play history';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Scrobble play history';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Scrobble plays';

  @override
  String get settingsNetworkName => 'ネット';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Maximum bitrate (mobile data)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Maximum bitrate (Wi-Fi)';

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
    return '$value seconds';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Unlimited';

  @override
  String get settingsResetActionsClearImageCache => 'Clear Image Cache';

  @override
  String get settingsResetName => 'リセット';

  @override
  String get settingsServersActionsAdd => 'Add source';

  @override
  String get settingsServersActionsDelete => 'Delete';

  @override
  String get settingsServersActionsEdit => 'Edit source';

  @override
  String get settingsServersActionsSave => 'Save';

  @override
  String get settingsServersActionsTestConnection => 'Test connection';

  @override
  String get settingsServersFieldsAddress => 'Address';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Password';

  @override
  String get settingsServersFieldsUsername => 'Username';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Connection to $address failed, check settings or server';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Connection to $address OK!';
  }

  @override
  String get settingsServersName => 'サーバ';

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
