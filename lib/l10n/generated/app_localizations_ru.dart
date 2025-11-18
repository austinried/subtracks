// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Russian (`ru`).
class AppLocalizationsRu extends AppLocalizations {
  AppLocalizationsRu([String locale = 'ru']) : super(locale);

  @override
  String get actionsCancel => 'Отменить';

  @override
  String get actionsDelete => 'Удалить';

  @override
  String get actionsDownload => 'Скачать';

  @override
  String get actionsDownloadCancel => 'Отменить загрузку';

  @override
  String get actionsDownloadDelete => 'Удалить загруженное';

  @override
  String get actionsOk => 'ОК';

  @override
  String get actionsStar => 'Избранное';

  @override
  String get actionsUnstar => 'Убрать из избранного';

  @override
  String get controlsShuffle => 'Перемешать';

  @override
  String get messagesNothingHere => 'Здесь ничего нет…';

  @override
  String get navigationTabsHome => 'Главная';

  @override
  String get navigationTabsLibrary => 'Библиотека';

  @override
  String get navigationTabsSearch => 'Поиск';

  @override
  String get navigationTabsSettings => 'Настройки';

  @override
  String get resourcesAlbumActionsPlay => 'Воспроизвести альбом';

  @override
  String get resourcesAlbumActionsView => 'Посмотреть альбом';

  @override
  String resourcesAlbumCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count альбомы',
      one: '$count альбом',
    );
    return '$_temp0';
  }

  @override
  String get resourcesAlbumListsSort => 'Сортировка альбомов';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Альбомов',
      many: 'Альбомов',
      few: 'Альбомы',
      one: 'Альбом',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Посмотреть исполнителя';

  @override
  String resourcesArtistCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count исполнители',
      one: '$count исполнитель',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistListsSort => 'Сортировать исполнителей';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Исполнителей',
      many: 'Исполнителей',
      few: 'Исполнители',
      one: 'Исполнитель',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'По альбомам';

  @override
  String get resourcesFilterArtist => 'По исполнителю';

  @override
  String get resourcesFilterGenre => 'По жанру';

  @override
  String get resourcesFilterOwner => 'По владельцу';

  @override
  String get resourcesFilterStarred => 'Избранные';

  @override
  String get resourcesFilterYear => 'По годам';

  @override
  String get resourcesPlaylistActionsPlay => 'Воспроизвести плейлист';

  @override
  String resourcesPlaylistCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count плейлисты',
      one: '$count плейлист',
    );
    return '$_temp0';
  }

  @override
  String resourcesPlaylistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Плейлистов',
      many: 'Плейлистов',
      few: 'Плейлисты',
      one: 'Плейлист',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Очередей',
      many: 'Очередей',
      few: 'Очереди',
      one: 'Очередь',
    );
    return '$_temp0';
  }

  @override
  String resourcesSongCount(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count треки',
      one: '$count трек',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSongListDeleteAllContent =>
      'Это удалит все загруженные файлы песен.';

  @override
  String get resourcesSongListDeleteAllTitle => 'Удалить загрузки?';

  @override
  String get resourcesSongListsArtistTopSongs => 'Лучшие треки';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Треков',
      many: 'Треков',
      few: 'Трека',
      one: 'Трек',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Недавно добавленные';

  @override
  String get resourcesSortByAlbum => 'По альбомам';

  @override
  String get resourcesSortByAlbumCount => 'По количеству альбомов';

  @override
  String get resourcesSortByArtist => 'По исполнителям';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Часто проигрываемые';

  @override
  String get resourcesSortByName => 'По имени';

  @override
  String get resourcesSortByRandom => 'Случайно';

  @override
  String get resourcesSortByRecentlyPlayed => 'Недавно проигранные';

  @override
  String get resourcesSortByTitle => 'По заголовку';

  @override
  String get resourcesSortByUpdated => 'По недавно обновленному';

  @override
  String get resourcesSortByYear => 'По году';

  @override
  String searchHeaderTitle(String query) {
    return 'Поиск: $query';
  }

  @override
  String get searchInputPlaceholder => 'Поиск';

  @override
  String get searchMoreResults => 'Больше…';

  @override
  String get searchNowPlayingContext => 'Результаты поиска';

  @override
  String get settingsAboutActionsLicenses => 'Лицензии';

  @override
  String get settingsAboutActionsProjectHomepage => 'Сайт проекта';

  @override
  String get settingsAboutActionsSupport => 'Поддержать разработчика';

  @override
  String get settingsAboutName => 'О Subtracks';

  @override
  String get settingsAboutShareLogs => 'Поделиться журналами';

  @override
  String get settingsAboutChooseLog => 'Выбрать файл журнала';

  @override
  String settingsAboutVersion(String version) {
    return 'версия $version';
  }

  @override
  String get settingsMusicName => 'Музыка';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Не отправлять историю воспроизведений';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Скробблинг истории воспроизведения';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Скробблинг';

  @override
  String get settingsNetworkName => 'Сеть';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Максимальный битрейт (мобильный интернет)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Максимальный битрейт (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle =>
      'Максимальное время буферизации';

  @override
  String get settingsNetworkOptionsMinBufferTitle =>
      'Минимальное время буферизации';

  @override
  String get settingsNetworkOptionsOfflineMode => 'Автономный режим';

  @override
  String get settingsNetworkOptionsOfflineModeOff =>
      'Использовать интернет для синхронизации музыки.';

  @override
  String get settingsNetworkOptionsOfflineModeOn =>
      'Не использовать интернет для синхронизации или воспроизведения музыки.';

  @override
  String get settingsNetworkOptionsStreamFormat =>
      'Предпочтительный формат потока';

  @override
  String get settingsNetworkOptionsStreamFormatServerDefault =>
      'Использовать сервер по умолчанию';

  @override
  String settingsNetworkValuesKbps(String value) {
    return '$value кбит/с';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value секунд';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Без ограничений';

  @override
  String get settingsResetActionsClearImageCache => 'Очистить кэш изображения';

  @override
  String get settingsResetName => 'Сброс';

  @override
  String get settingsServersActionsAdd => 'Добавить сервер';

  @override
  String get settingsServersActionsDelete => 'Удалить';

  @override
  String get settingsServersActionsEdit => 'Редактировать сервер';

  @override
  String get settingsServersActionsSave => 'Сохранить';

  @override
  String get settingsServersActionsTestConnection => 'Проверить подключение';

  @override
  String get settingsServersFieldsAddress => 'Адрес';

  @override
  String get settingsServersFieldsName => 'Имя';

  @override
  String get settingsServersFieldsPassword => 'Пароль';

  @override
  String get settingsServersFieldsUsername => 'Имя пользователя';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Не удалось подключиться к $address, проверьте настройки или сервер';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Подключение к $address установлено!';
  }

  @override
  String get settingsServersName => 'Серверы';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Отправить пароль в виде токена';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Отправить пароль в виде текста (устарело, убедитесь, что ваше соединение безопасно!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Принудительно использовать текстовой пароль';
}
