// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for French (`fr`).
class AppLocalizationsFr extends AppLocalizations {
  AppLocalizationsFr([String locale = 'fr']) : super(locale);

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
  String get actionsStar => 'Mettre en favoris';

  @override
  String get actionsUnstar => 'Enlever des favoris';

  @override
  String get controlsShuffle => 'Shuffle';

  @override
  String get messagesNothingHere => 'Rien ici…';

  @override
  String get navigationTabsHome => 'Accueil';

  @override
  String get navigationTabsLibrary => 'Bibliothèque';

  @override
  String get navigationTabsSearch => 'Recherche';

  @override
  String get navigationTabsSettings => 'Paramètres';

  @override
  String get resourcesAlbumActionsPlay => 'Jouer l\'album';

  @override
  String get resourcesAlbumActionsView => 'Voir l\'album';

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
  String get resourcesAlbumListsSort => 'Trier les albums';

  @override
  String resourcesAlbumName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Albums',
      one: 'Album',
    );
    return '$_temp0';
  }

  @override
  String get resourcesArtistActionsView => 'Voir l\'artiste';

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
  String get resourcesArtistListsSort => 'Trier les artistes';

  @override
  String resourcesArtistName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Artistes',
      one: 'Artiste',
    );
    return '$_temp0';
  }

  @override
  String get resourcesFilterAlbum => 'Album';

  @override
  String get resourcesFilterArtist => 'Artist';

  @override
  String get resourcesFilterGenre => 'Par Genre';

  @override
  String get resourcesFilterOwner => 'Owner';

  @override
  String get resourcesFilterStarred => 'Favoris';

  @override
  String get resourcesFilterYear => 'Year';

  @override
  String get resourcesPlaylistActionsPlay => 'Lire la playlist';

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
      other: 'Playlists',
      one: 'Playlist',
    );
    return '$_temp0';
  }

  @override
  String resourcesQueueName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Files d\'attente',
      one: 'File d\'attente',
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
  String get resourcesSongListsArtistTopSongs => 'Meilleures Chansons';

  @override
  String resourcesSongName(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: 'Chansons',
      one: 'Chanson',
    );
    return '$_temp0';
  }

  @override
  String get resourcesSortByAdded => 'Récemment Ajouté';

  @override
  String get resourcesSortByAlbum => 'Album';

  @override
  String get resourcesSortByAlbumCount => 'Album count';

  @override
  String get resourcesSortByArtist => 'Par Artiste';

  @override
  String get resourcesSortByFrequentlyPlayed => 'Fréquemment Joué';

  @override
  String get resourcesSortByName => 'Par Nom';

  @override
  String get resourcesSortByRandom => 'Aléatoire';

  @override
  String get resourcesSortByRecentlyPlayed => 'Récemment Joué';

  @override
  String get resourcesSortByTitle => 'Title';

  @override
  String get resourcesSortByUpdated => 'Recently updated';

  @override
  String get resourcesSortByYear => 'Par Année';

  @override
  String searchHeaderTitle(String query) {
    return 'Recherche : $query';
  }

  @override
  String get searchInputPlaceholder => 'Recherche';

  @override
  String get searchMoreResults => 'Plus…';

  @override
  String get searchNowPlayingContext => 'Résultats de recherche';

  @override
  String get settingsAboutActionsLicenses => 'Licences';

  @override
  String get settingsAboutActionsProjectHomepage => 'Page d\'accueil du projet';

  @override
  String get settingsAboutActionsSupport => 'Support the developer 💜';

  @override
  String get settingsAboutName => 'À propos';

  @override
  String get settingsAboutShareLogs => 'Share logs';

  @override
  String get settingsAboutChooseLog => 'Choose a log file';

  @override
  String settingsAboutVersion(String version) {
    return 'version $version';
  }

  @override
  String get settingsMusicName => 'Musique';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOff =>
      'Ne pas scrobbler l\'historique de lecture';

  @override
  String get settingsMusicOptionsScrobbleDescriptionOn =>
      'Scrobbler l\'historique de lecture';

  @override
  String get settingsMusicOptionsScrobbleTitle => 'Scrobbler la lecture';

  @override
  String get settingsNetworkName => 'Réseau';

  @override
  String get settingsNetworkOptionsMaxBitrateMobileTitle =>
      'Débit binaire maximum (mobile)';

  @override
  String get settingsNetworkOptionsMaxBitrateWifiTitle =>
      'Débit binaire maximum (Wi-Fi)';

  @override
  String get settingsNetworkOptionsMaxBufferTitle =>
      'Temps maximum en mémoire tampon';

  @override
  String get settingsNetworkOptionsMinBufferTitle =>
      'Temps minimum en mémoire tampon';

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
    return '${value}kbit/s';
  }

  @override
  String settingsNetworkValuesSeconds(String value) {
    return '$value secondes';
  }

  @override
  String get settingsNetworkValuesUnlimitedKbps => 'Illimité';

  @override
  String get settingsResetActionsClearImageCache => 'Vider le cache d\'images';

  @override
  String get settingsResetName => 'Réinitialiser';

  @override
  String get settingsServersActionsAdd => 'Ajouter un serveur';

  @override
  String get settingsServersActionsDelete => 'Supprimer';

  @override
  String get settingsServersActionsEdit => 'Modifier le serveur';

  @override
  String get settingsServersActionsSave => 'Sauvegarder';

  @override
  String get settingsServersActionsTestConnection => 'Tester la connexion';

  @override
  String get settingsServersFieldsAddress => 'Adresse';

  @override
  String get settingsServersFieldsName => 'Name';

  @override
  String get settingsServersFieldsPassword => 'Mot de passe';

  @override
  String get settingsServersFieldsUsername => 'Nom d\'utilisateur';

  @override
  String settingsServersMessagesConnectionFailed(String address) {
    return 'Échec de la connexion à $address, vérifiez les paramètres ou le serveur';
  }

  @override
  String settingsServersMessagesConnectionOk(String address) {
    return 'Connexion à $address OK !';
  }

  @override
  String get settingsServersName => 'Serveurs';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff =>
      'Envoyer le mot de passe sous forme de jeton + salage';

  @override
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn =>
      'Envoyer le mot de passe en test clair (héritage, assurez-vous que la connexion est sécurisée !)';

  @override
  String get settingsServersOptionsForcePlaintextPasswordTitle =>
      'Forcer le mot de passe en texte clair';
}
