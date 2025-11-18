// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Turkish (`tr`).
class AppLocalizationsTr extends AppLocalizations {
  AppLocalizationsTr([String locale = 'tr']) : super(locale);

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
  String get actionsStar => 'Yıldızla';

  @override
  String get actionsUnstar => 'Yıldızı Kaldır';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'Burada bir şey yok …';

  @override
  String get navigationTabsHome => 'Giriş';

  @override
  String get navigationTabsLibrary => 'Kütüphane';

  @override
  String get navigationTabsSearch => 'Arama';

  @override
  String get navigationTabsSettings => 'Ayarlar';

  @override
  String get resourcesAlbumActionsPlay => 'Albümü Çal';

  @override
  String get resourcesAlbumActionsView => 'Albüme Git';

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
  String get resourcesAlbumListsSort => 'Albümleri Sırala';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Albümler',
      one: 'Albüm',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Sanatçıya Git';

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
  String get resourcesArtistListsSort => 'Sanatçıları Sırala';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Sanatçılar',
      one: 'Sanatçı',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Türe göre';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Yıldızlı';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Listeyi Çal';

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
      other: 'Çalma Listeleri',
      one: 'Çalma Listesi',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Sıralar',
      one: 'Sıra',
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
  String get resourcesSongListsArtistTopSongs => 'En İyi Şarkılar';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Şarkılar',
      one: 'Şarkı',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Son Eklenenler';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Sanatçıya Göre';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Sık Dinlenenler';

  @override
  String get resourcesSortByName => 'İsme göre';

  @override
  String get resourcesSortByRandom => 'Rastgele';

  @override
  String get resourcesSortByRecentlyPlayed => 'Yeni Dinleneler';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Yıla göre';

  @override
  String searchHeaderTitle(String query) {
    return 'Aranan: $query';
  }

  @override
  String get searchInputPlaceholder => 'Ara';

  @override
  String get searchMoreResults => 'Tüm Sonuçlar …';

  @override
  String get searchNowPlayingContext => 'Arama Sonuçları';

  @override
  String get settingsAboutActionsLicenses => 'Lisanslar';

  @override
  String get settingsAboutActionsProjectHomepage => 'Proje Ana Sayfası';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'Hakkında';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'sürüm $version';
  }

  @override
  String get settingsMusicName => 'Müzik';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Müzik geçmişini profilime ekleme';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Müzik geçmişini profilime ekle';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Çalan müziği profilime ekle';

  @override
  String get settingsNetworkName => 'Ağ';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Azami bithızı (SIM interneti)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Azami bithızı (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Azami arabellek süresi';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Asgari arabellek süresi';

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
    return '${value}kb/sn';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value saniye';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Sınırsız';

  @override
  String get settingsResetActionsClearImageCache =>
      'Görüntü Önbelleğini Temizle';

  @override
  String get settingsResetName => 'Sıfırlama';

  @override
  String get settingsServersActionsAdd => 'Sunucu Ekle';

  @override
  String get settingsServersActionsDelete => 'Kaldır';

  @override
  String get settingsServersActionsEdit => 'Sunucu Ayarı';

  @override
  String get settingsServersActionsSave => 'Kaydet';

  @override
  String get settingsServersActionsTestConnection => 'Bağlantıyı Sına';

  @override
  String get settingsServersFieldsAddress => 'Adres';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Şifre';

  @override
  String get settingsServersFieldsUsername => 'Kullanıcı Adı';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return '$address ile bağlantı başarısız. Sunucu ayarlarınızın doğrulığundan emin olun.';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return '$address ile bağlantı başarılı!';
  }

  @override
  String get settingsServersName => 'Sunucular';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Şifreyi dizgecik ve tuz karışımı olarak gönder';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Şifreyi düzmetin olarak gönderir (eski yöntem, bağlantının güvenli (HTTPS) olduğu sunucularda kullanın!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Düzyazı şifre kullanmayı zorla';
}
