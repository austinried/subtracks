// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Chinese (`zh`).
class AppLocalizationsZh extends AppLocalizations {
  AppLocalizationsZh([String locale = 'zh']) : super(locale);

  @override
  String get actionsCancel => '取消';

  @override
  String get actionsDelete => '删除';

  @override
  String get actionsDownload => '下载';

  @override
  String get actionsDownloadCancel => '取消下载';

  @override
  String get actionsDownloadDelete => '删除已下载';

  @override
  String get actionsOk => '确定';

  @override
  String get actionsStar => '收藏';

  @override
  String get actionsUnstar => '移除收藏';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => '什么都没有…';

  @override
  String get navigationTabsHome => '首页';

  @override
  String get navigationTabsLibrary => '所有';

  @override
  String get navigationTabsSearch => '搜索';

  @override
  String get navigationTabsSettings => '设置';

  @override
  String get resourcesAlbumActionsPlay => '播放专辑';

  @override
  String get resourcesAlbumActionsView => '查看专辑';

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
  String get resourcesAlbumListsSort => '专辑排序';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '专辑',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => '查看歌手';

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
  String get resourcesArtistListsSort => '歌手排序';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '歌手',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => '专辑';

  @override
  String get resourcesFilterArtist => '歌手';

  @override
  String get resourcesFilterGenre => '根据类型';

  @override
  String get resourcesFilterOwner => '所有者';

  @override
  String get resourcesFilterStarred => '已收藏';

  @override
  String get resourcesFilterYear => '年份';

  @override
  String get resourcesPlaylistActionsPlay => '全部播放';

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
      other: '播放列表',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '队列',
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
  String get resourcesSongListDeleteAllContent => '该操作会删除所有已下载的歌曲文件。';

  @override
  String get resourcesSongListDeleteAllTitle => '删除下载？';

  @override
  String get resourcesSongListsArtistTopSongs => '热门歌曲';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '歌曲',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => '最近添加';

  @override
  String get resourcesSortByAlbum => '专辑';

  @override
  String get resourcesSortByAlbumCount => '专辑数量';

  @override
  String get resourcesSortByArtist => '根据歌手';

  @override
  String get resourcesSortByFrequentlyPlayed => '播放最多';

  @override
  String get resourcesSortByName => '根据名称';

  @override
  String get resourcesSortByRandom => '随机';

  @override
  String get resourcesSortByRecentlyPlayed => '最近播放';

  @override
  String get resourcesSortByTitle => '标题';

  @override
  String get resourcesSortByUpdated => '最近添加';

  @override
  String get resourcesSortByYear => '根据年份';

  @override
  String searchHeaderTitle(String query) {
    return '搜索： $query';
  }

  @override
  String get searchInputPlaceholder => '搜索';

  @override
  String get searchMoreResults => '更多…';

  @override
  String get searchNowPlayingContext => '搜索结果';

  @override
  String get settingsAboutActionsLicenses => '许可';

  @override
  String get settingsAboutActionsProjectHomepage => '项目地址';

  @override
  String get settingsAboutActionsSupport => '支持开发者';

  @override
  String get settingsAboutName => '关于';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return '版本 $version';
  }

  @override
  String get settingsMusicName => '音乐';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff => '不记录scrobble历史';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn => 'Scrobble播放历史';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Scrobble模式';

  @override
  String get settingsNetworkName => '网络';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle => '最大比特率 (3G/4G/5G)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle => '最大比特率 (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => '最大缓冲时间';

  @override
  String get settingsNetworkOptionsMinBufferTitle => '最小缓冲时间';

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
    return '$value 秒';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => '不限制';

  @override
  String get settingsResetActionsClearImageCache => '清除图片缓存';

  @override
  String get settingsResetName => '重置';

  @override
  String get settingsServersActionsAdd => '添加服务器';

  @override
  String get settingsServersActionsDelete => '删除';

  @override
  String get settingsServersActionsEdit => '编辑服务器';

  @override
  String get settingsServersActionsSave => '保存';

  @override
  String get settingsServersActionsTestConnection => '测试连接';

  @override
  String get settingsServersFieldsAddress => '地址';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => '密码';

  @override
  String get settingsServersFieldsUsername => '用户名';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return '连接到 $address 失败，检查设置或服务器';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return '连接到 $address 正常！';
  }

  @override
  String get settingsServersName => '服务器';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      '密码以 token + salt 加密发送';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      '密码以明文发送（不推荐，注意链接安全！）';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle => '强制使用明文密码';
}
