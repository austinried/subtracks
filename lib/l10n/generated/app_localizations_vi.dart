// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Vietnamese (`vi`).
class AppLocalizationsVi extends AppLocalizations {
  AppLocalizationsVi([String locale = 'vi']) : super(locale);

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
  String get actionsStar => 'Đánh dấu sao';

  @override
  String get actionsUnstar => 'Bỏ dấu sao';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'Không có gì ở đây…';

  @override
  String get navigationTabsHome => 'Trang chủ';

  @override
  String get navigationTabsLibrary => 'Thư Viện';

  @override
  String get navigationTabsSearch => 'Tìm kiếm';

  @override
  String get navigationTabsSettings => 'Thiết Lập';

  @override
  String get resourcesAlbumActionsPlay => 'Phát Album';

  @override
  String get resourcesAlbumActionsView => 'Xem Album';

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
  String get resourcesAlbumListsSort => 'Sắp xếp Album';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Xem Nghệ sĩ';

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
  String get resourcesArtistListsSort => 'Sắp xếp nghệ sĩ';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Nghệ sĩ',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Theo thể loại';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Có gắn dấu sao';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Phát Danh sách phát';

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
      other: 'Danh sách phát',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Hàng chờ',
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
  String get resourcesSongListsArtistTopSongs => 'Bài hát hàng đầu';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Bài hát',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Thêm vào gần đây';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Theo nghệ sĩ';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Thường xuyên chơi';

  @override
  String get resourcesSortByName => 'Theo tên';

  @override
  String get resourcesSortByRandom => 'Ngẫu Nhiên';

  @override
  String get resourcesSortByRecentlyPlayed => 'Đã phát gần đây';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Theo năm';

  @override
  String searchHeaderTitle(String query) {
    return 'Tìm kiếm: $query';
  }

  @override
  String get searchInputPlaceholder => 'Tìm kiếm';

  @override
  String get searchMoreResults => 'Nhiều hơn…';

  @override
  String get searchNowPlayingContext => 'Kết quả tìm kiếm';

  @override
  String get settingsAboutActionsLicenses => 'Giấy phép';

  @override
  String get settingsAboutActionsProjectHomepage => 'Trang chủ Dự án';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'Giới thiệu';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'phiên bản $version';
  }

  @override
  String get settingsMusicName => 'Âm nhạc';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Đừng scrobble lịch sử chơi';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Lịch sử chơi Scrobble';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Scrobble lượt chơi';

  @override
  String get settingsNetworkName => 'Mạng';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Tối đa bitrate (mobile)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Tối đa bitrate (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Thời gian đệm tối đa';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Thời gian đệm tối thiểu';

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
    return '$value giây';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Không giới hạn';

  @override
  String get settingsResetActionsClearImageCache => 'Xóa bộ nhớ đệm hình ảnh';

  @override
  String get settingsResetName => 'Cài đặt lại';

  @override
  String get settingsServersActionsAdd => 'Thêm máy chủ';

  @override
  String get settingsServersActionsDelete => 'Xóa';

  @override
  String get settingsServersActionsEdit => 'Chỉnh sửa máy chủ';

  @override
  String get settingsServersActionsSave => 'Lưu';

  @override
  String get settingsServersActionsTestConnection => 'Kiểm tra kết nối';

  @override
  String get settingsServersFieldsAddress => 'Địa chỉ';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Mật Khẩu';

  @override
  String get settingsServersFieldsUsername => 'Tên đăng nhập';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Kết nối với $address không thành công, hãy kiểm tra cài đặt hoặc máy chủ';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Kết nối với $address OK!';
  }

  @override
  String get settingsServersName => 'Máy chủ';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Gửi mật khẩu dưới dạng token + salt';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Gửi mật khẩu ở dạng văn bản rõ ràng (kế thừa, đảm bảo kết nối của bạn an toàn!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Buộc mật khẩu văn bản thuần túy';
}
