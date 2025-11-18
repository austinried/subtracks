// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Arabic (`ar`).
class AppLocalizationsAr extends AppLocalizations {
  AppLocalizationsAr([String locale = 'ar']) : super(locale);

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
  String get actionsStar => 'مميز';

  @override
  String get actionsUnstar => 'ازل التمييز';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'لا شيء هنا…';

  @override
  String get navigationTabsHome => 'الرئيسية';

  @override
  String get navigationTabsLibrary => 'المكتبة';

  @override
  String get navigationTabsSearch => 'بحث';

  @override
  String get navigationTabsSettings => 'الإعدادات';

  @override
  String get resourcesAlbumActionsPlay => 'شَغل الألبوم';

  @override
  String get resourcesAlbumActionsView => 'أعرض الألبوم';

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
  String get resourcesAlbumListsSort => 'فرز الألبومات';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '',
      many: '',
      few: 'ألبوم',
      one: '',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'أظهر الفنان';

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
  String get resourcesArtistListsSort => 'فرز الفنانين';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'فنانان',
      many: 'فنانان',
      few: 'فنان',
      one: '',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'حسب النوع';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'موسوم';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'شغل قائمة التشغيل';

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
      other: 'قائمتان تشغيل',
      many: 'قائمتان تشغيل',
      few: 'قائمة تشغيل',
      one: '',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'صفين',
      many: 'صفين',
      few: 'صف',
      one: '',
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
  String get resourcesSongListsArtistTopSongs => 'أشهر الأغاني';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '',
      many: '',
      few: 'أُغْنِيَة',
      one: '',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'أضيف حديثا';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'حسب الفنان/ة';

  @override
  String get resourcesSortByFrequentlyPlayed => 'مشغل كثيرا';

  @override
  String get resourcesSortByName => 'حسب الاسم';

  @override
  String get resourcesSortByRandom => 'عشوائي';

  @override
  String get resourcesSortByRecentlyPlayed => 'شغل حديثا';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'حسب السنة';

  @override
  String searchHeaderTitle(String query) {
    return 'بحث: $query';
  }

  @override
  String get searchInputPlaceholder => 'بحث';

  @override
  String get searchMoreResults => 'المزيد…';

  @override
  String get searchNowPlayingContext => 'نتائج البحث';

  @override
  String get settingsAboutActionsLicenses => 'الرخص';

  @override
  String get settingsAboutActionsProjectHomepage => 'موقع المشروع';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'حول';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'الإصدار $version';
  }

  @override
  String get settingsMusicName => 'موسيقى';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'لا تستورد سجل التشغيل';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn => 'استيراد سجل التشغيل';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'استيراد سجل التشغيل';

  @override
  String get settingsNetworkName => 'الشبكة';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'أقصى معدل نقل بيانات (mobile)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'أقصى معدل نقل بيانات (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle =>
      'الحد الأقصى من وقت التخزين المؤقت';

  @override
  String get settingsNetworkOptionsMinBufferTitle =>
      'الحد الأدنى من وقت التخزين المؤقت';

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
    return '$value كيلو بايت في الثانية';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value ثواني';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'غير محدود';

  @override
  String get settingsResetActionsClearImageCache =>
      'مسح ذاكرة التخزين المؤقت للصور';

  @override
  String get settingsResetName => 'إعادة ضبط';

  @override
  String get settingsServersActionsAdd => 'أضف سيرفر';

  @override
  String get settingsServersActionsDelete => 'حذف';

  @override
  String get settingsServersActionsEdit => 'عدل السيرفر';

  @override
  String get settingsServersActionsSave => 'حفظ';

  @override
  String get settingsServersActionsTestConnection => 'أخبر الأتصال';

  @override
  String get settingsServersFieldsAddress => 'العناوين';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'كلمة المرور';

  @override
  String get settingsServersFieldsUsername => 'إسم المستخدم';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'الأتصال ب $address فشل، ابحث في الإعدادات او السيرفر';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'الأتصال ب $address جيد!';
  }

  @override
  String get settingsServersName => 'السيرفرات';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'أرسل كلمة المرور على شكل توكِن';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'أرسل كلمة المرور بنص عادي (قديم ، تأكد من أن اتصالك آمن!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'أظهر كلمة المرور';
}
