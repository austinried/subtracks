// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Portuguese (`pt`).
class AppLocalizationsPt extends AppLocalizations {
  AppLocalizationsPt([String locale = 'pt']) : super(locale);

  @override
  String get actionsCancel => 'Cancelar';

  @override
  String get actionsDelete => 'Apagar';

  @override
  String get actionsDownload => 'Descarregar';

  @override
  String get actionsDownloadCancel => 'Cancelar descarga';

  @override
  String get actionsDownloadDelete => 'Apagar descarga';

  @override
  String get actionsOk => 'OK';

  @override
  String get actionsStar => 'Favorito';

  @override
  String get actionsUnstar => 'Remover favorito';

  @override
  String get controlsShuffle => 'Aleatório';

  @override
  String get messagesNothingHere => 'Não existe nada…';

  @override
  String get navigationTabsHome => 'Início';

  @override
  String get navigationTabsLibrary => 'Biblioteca';

  @override
  String get navigationTabsSearch => 'Procurar';

  @override
  String get navigationTabsSettings => 'Definições';

  @override
  String get resourcesAlbumActionsPlay => 'Tocar Álbum';

  @override
  String get resourcesAlbumActionsView => 'Ver Álbum';

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
  String get resourcesAlbumListsSort => 'Ordenar Álbuns';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Álbuns',
      one: 'Álbum',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Ver Artista';

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
  String get resourcesArtistListsSort => 'Ordenar Artistas';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Artistas',
      one: 'Artista',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Álbum';

  @override
  String get resourcesFilterArtist => 'Artista';

  @override
  String get resourcesFilterGenre => 'Por Género';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Favoritos';

  @override
  String get resourcesFilterYear => 'Ano';

  @override
  String get resourcesPlaylistActionsPlay => 'Tocar Playlist';

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
      other: 'Listas',
      one: 'Lista',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Filas',
      one: 'Fila',
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
  String get resourcesSongListsArtistTopSongs => 'Top Músicas';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Músicas',
      one: 'Música',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Adicionado recentemente';

  @override
  String get resourcesSortByAlbum => 'Álbum';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Por Artista';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Mais Tocado';

  @override
  String get resourcesSortByName => 'Por Nome';

  @override
  String get resourcesSortByRandom => 'Aleatório';

  @override
  String get resourcesSortByRecentlyPlayed => 'Ouviu recentemente';

  @override
  String get resourcesSortByTitle => 'Título';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Por Ano';

  @override
  String searchHeaderTitle(String query) {
    return 'Procurar: $query';
  }

  @override
  String get searchInputPlaceholder => 'Procurar';

  @override
  String get searchMoreResults => 'Mais…';

  @override
  String get searchNowPlayingContext => 'Resultados da Pesquisa';

  @override
  String get settingsAboutActionsLicenses => 'Licenças';

  @override
  String get settingsAboutActionsProjectHomepage => 'Página do Projeto';

  @override
  String get settingsAboutActionsSupport => 'Apoie o programador 💜';

  @override
  String get settingsAboutName => 'Acerca';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'versão $version';
  }

  @override
  String get settingsMusicName => 'Música';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Não enviar histórico de reproduções por scrobble';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Enviar histórico de reproduções por scrobble';

  @override
  String get settingsMusicOptionsScrobbleTitle =>
      'Enviar reproduções por scrobble';

  @override
  String get settingsNetworkName => 'Rede';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Bitrate Máximo (móvel)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Bitrate Máximo (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle => 'Tempo de buffer máximo';

  @override
  String get settingsNetworkOptionsMinBufferTitle => 'Tempo de buffer mínimo';

  @override
  String get settingsNetworkOptionsOfflineMode => 'Modo offline';

  @override
  String get settingsNetworkOptionsOfflineModeOff =>
      'Usar a internet para sincronizar música.';

  @override
  String get settingsNetworkOptionsOfflineModeOn =>
      'Não usar a internet para sincronizar ou tocar música.';

  @override
  String get settingsNetworkOptionsStreamFormat =>
      'Formato preferido de streaming';

  @override
  String get settingsNetworkOptionsStreamFormatServerDefault =>
      'Use server default';

  @override
  String settingsNetworkValuesKbps(String value) {
    return '${value}kbps';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value segundos';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Ilimitado';

  @override
  String get settingsResetActionsClearImageCache => 'Limpar cache de Imagens';

  @override
  String get settingsResetName => 'Redefinir';

  @override
  String get settingsServersActionsAdd => 'Adicionar Servidor';

  @override
  String get settingsServersActionsDelete => 'Apagar';

  @override
  String get settingsServersActionsEdit => 'Editar Servidor';

  @override
  String get settingsServersActionsSave => 'Guardar';

  @override
  String get settingsServersActionsTestConnection => 'Testar Ligação';

  @override
  String get settingsServersFieldsAddress => 'Endereço';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Senha';

  @override
  String get settingsServersFieldsUsername => 'Nome de utilizador';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Ligação a $address falhou, verifique definições ou servidor';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Ligação a $address OK!';
  }

  @override
  String get settingsServersName => 'Servidores';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Enviar senha como token + sal';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Enviar senha em texto simples (antigo, certifique-se que a sua ligação é segura!)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Forçar password em texto simples';
}
