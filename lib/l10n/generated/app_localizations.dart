import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_ar.dart';
import 'app_localizations_ca.dart';
import 'app_localizations_cs.dart';
import 'app_localizations_da.dart';
import 'app_localizations_de.dart';
import 'app_localizations_en.dart';
import 'app_localizations_es.dart';
import 'app_localizations_fr.dart';
import 'app_localizations_gl.dart';
import 'app_localizations_it.dart';
import 'app_localizations_ja.dart';
import 'app_localizations_nb.dart';
import 'app_localizations_pa.dart';
import 'app_localizations_pl.dart';
import 'app_localizations_pt.dart';
import 'app_localizations_ru.dart';
import 'app_localizations_tr.dart';
import 'app_localizations_vi.dart';
import 'app_localizations_zh.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'generated/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('ar'),
    Locale('ca'),
    Locale('cs'),
    Locale('da'),
    Locale('de'),
    Locale('en'),
    Locale('es'),
    Locale('fr'),
    Locale('gl'),
    Locale('it'),
    Locale('ja'),
    Locale('nb'),
    Locale('pa'),
    Locale('pl'),
    Locale('pt'),
    Locale('ru'),
    Locale('tr'),
    Locale('vi'),
    Locale('zh'),
  ];

  /// No description provided for @actionsCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get actionsCancel;

  /// No description provided for @actionsDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get actionsDelete;

  /// No description provided for @actionsDownload.
  ///
  /// In en, this message translates to:
  /// **'Download'**
  String get actionsDownload;

  /// No description provided for @actionsDownloadCancel.
  ///
  /// In en, this message translates to:
  /// **'Cancel download'**
  String get actionsDownloadCancel;

  /// No description provided for @actionsDownloadDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete downloaded'**
  String get actionsDownloadDelete;

  /// No description provided for @actionsOk.
  ///
  /// In en, this message translates to:
  /// **'OK'**
  String get actionsOk;

  /// No description provided for @actionsStar.
  ///
  /// In en, this message translates to:
  /// **'Star'**
  String get actionsStar;

  /// No description provided for @actionsUnstar.
  ///
  /// In en, this message translates to:
  /// **'Unstar'**
  String get actionsUnstar;

  /// No description provided for @controlsShuffle.
  ///
  /// In en, this message translates to:
  /// **'Shuffle'**
  String get controlsShuffle;

  /// No description provided for @messagesNothingHere.
  ///
  /// In en, this message translates to:
  /// **'Nothing here…'**
  String get messagesNothingHere;

  /// No description provided for @navigationTabsHome.
  ///
  /// In en, this message translates to:
  /// **'Home'**
  String get navigationTabsHome;

  /// No description provided for @navigationTabsLibrary.
  ///
  /// In en, this message translates to:
  /// **'Library'**
  String get navigationTabsLibrary;

  /// No description provided for @navigationTabsSearch.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get navigationTabsSearch;

  /// No description provided for @navigationTabsSettings.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get navigationTabsSettings;

  /// No description provided for @resourcesAlbumActionsPlay.
  ///
  /// In en, this message translates to:
  /// **'Play album'**
  String get resourcesAlbumActionsPlay;

  /// No description provided for @resourcesAlbumActionsView.
  ///
  /// In en, this message translates to:
  /// **'View album'**
  String get resourcesAlbumActionsView;

  /// No description provided for @resourcesAlbumCount.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{{count} album} other{{count} albums}}'**
  String resourcesAlbumCount(int count);

  /// No description provided for @resourcesAlbumListsSort.
  ///
  /// In en, this message translates to:
  /// **'Sort albums'**
  String get resourcesAlbumListsSort;

  /// No description provided for @resourcesAlbumName.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{Album} other{Albums}}'**
  String resourcesAlbumName(int count);

  /// No description provided for @resourcesArtistActionsView.
  ///
  /// In en, this message translates to:
  /// **'View artist'**
  String get resourcesArtistActionsView;

  /// No description provided for @resourcesArtistCount.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{{count} artist} other{{count} artists}}'**
  String resourcesArtistCount(int count);

  /// No description provided for @resourcesArtistListsSort.
  ///
  /// In en, this message translates to:
  /// **'Sort artists'**
  String get resourcesArtistListsSort;

  /// No description provided for @resourcesArtistName.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{Artist} other{Artists}}'**
  String resourcesArtistName(int count);

  /// No description provided for @resourcesFilterAlbum.
  ///
  /// In en, this message translates to:
  /// **'Album'**
  String get resourcesFilterAlbum;

  /// No description provided for @resourcesFilterArtist.
  ///
  /// In en, this message translates to:
  /// **'Artist'**
  String get resourcesFilterArtist;

  /// No description provided for @resourcesFilterGenre.
  ///
  /// In en, this message translates to:
  /// **'Genre'**
  String get resourcesFilterGenre;

  /// No description provided for @resourcesFilterOwner.
  ///
  /// In en, this message translates to:
  /// **'Owner'**
  String get resourcesFilterOwner;

  /// No description provided for @resourcesFilterStarred.
  ///
  /// In en, this message translates to:
  /// **'Starred'**
  String get resourcesFilterStarred;

  /// No description provided for @resourcesFilterYear.
  ///
  /// In en, this message translates to:
  /// **'Year'**
  String get resourcesFilterYear;

  /// No description provided for @resourcesPlaylistActionsPlay.
  ///
  /// In en, this message translates to:
  /// **'Play playlist'**
  String get resourcesPlaylistActionsPlay;

  /// No description provided for @resourcesPlaylistCount.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{{count} playlist} other{{count} playlists}}'**
  String resourcesPlaylistCount(int count);

  /// No description provided for @resourcesPlaylistName.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{Playlist} other{Playlists}}'**
  String resourcesPlaylistName(int count);

  /// No description provided for @resourcesQueueName.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{Queue} other{Queues}}'**
  String resourcesQueueName(int count);

  /// No description provided for @resourcesSongCount.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{{count} song} other{{count} songs}}'**
  String resourcesSongCount(int count);

  /// No description provided for @resourcesSongListDeleteAllContent.
  ///
  /// In en, this message translates to:
  /// **'This will remove all downloaded song files.'**
  String get resourcesSongListDeleteAllContent;

  /// No description provided for @resourcesSongListDeleteAllTitle.
  ///
  /// In en, this message translates to:
  /// **'Delete downloads?'**
  String get resourcesSongListDeleteAllTitle;

  /// No description provided for @resourcesSongListsArtistTopSongs.
  ///
  /// In en, this message translates to:
  /// **'Top Songs'**
  String get resourcesSongListsArtistTopSongs;

  /// No description provided for @resourcesSongName.
  ///
  /// In en, this message translates to:
  /// **'{count,plural, =1{Song} other{Songs}}'**
  String resourcesSongName(int count);

  /// No description provided for @resourcesSortByAdded.
  ///
  /// In en, this message translates to:
  /// **'Recently added'**
  String get resourcesSortByAdded;

  /// No description provided for @resourcesSortByAlbum.
  ///
  /// In en, this message translates to:
  /// **'Album'**
  String get resourcesSortByAlbum;

  /// No description provided for @resourcesSortByAlbumCount.
  ///
  /// In en, this message translates to:
  /// **'Album count'**
  String get resourcesSortByAlbumCount;

  /// No description provided for @resourcesSortByArtist.
  ///
  /// In en, this message translates to:
  /// **'Artist'**
  String get resourcesSortByArtist;

  /// No description provided for @resourcesSortByFrequentlyPlayed.
  ///
  /// In en, this message translates to:
  /// **'Frequently played'**
  String get resourcesSortByFrequentlyPlayed;

  /// No description provided for @resourcesSortByName.
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get resourcesSortByName;

  /// No description provided for @resourcesSortByRandom.
  ///
  /// In en, this message translates to:
  /// **'Random'**
  String get resourcesSortByRandom;

  /// No description provided for @resourcesSortByRecentlyPlayed.
  ///
  /// In en, this message translates to:
  /// **'Recently played'**
  String get resourcesSortByRecentlyPlayed;

  /// No description provided for @resourcesSortByTitle.
  ///
  /// In en, this message translates to:
  /// **'Title'**
  String get resourcesSortByTitle;

  /// No description provided for @resourcesSortByUpdated.
  ///
  /// In en, this message translates to:
  /// **'Recently updated'**
  String get resourcesSortByUpdated;

  /// No description provided for @resourcesSortByYear.
  ///
  /// In en, this message translates to:
  /// **'Year'**
  String get resourcesSortByYear;

  /// No description provided for @searchHeaderTitle.
  ///
  /// In en, this message translates to:
  /// **'Search: {query}'**
  String searchHeaderTitle(String query);

  /// No description provided for @searchInputPlaceholder.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get searchInputPlaceholder;

  /// No description provided for @searchMoreResults.
  ///
  /// In en, this message translates to:
  /// **'More…'**
  String get searchMoreResults;

  /// No description provided for @searchNowPlayingContext.
  ///
  /// In en, this message translates to:
  /// **'Search results'**
  String get searchNowPlayingContext;

  /// No description provided for @settingsAboutActionsLicenses.
  ///
  /// In en, this message translates to:
  /// **'Licenses'**
  String get settingsAboutActionsLicenses;

  /// No description provided for @settingsAboutActionsProjectHomepage.
  ///
  /// In en, this message translates to:
  /// **'Project homepage'**
  String get settingsAboutActionsProjectHomepage;

  /// No description provided for @settingsAboutActionsSupport.
  ///
  /// In en, this message translates to:
  /// **'Support the developer 💜'**
  String get settingsAboutActionsSupport;

  /// No description provided for @settingsAboutName.
  ///
  /// In en, this message translates to:
  /// **'About'**
  String get settingsAboutName;

  /// No description provided for @settingsAboutShareLogs.
  ///
  /// In en, this message translates to:
  /// **'Share logs'**
  String get settingsAboutShareLogs;

  /// No description provided for @settingsAboutChooseLog.
  ///
  /// In en, this message translates to:
  /// **'Choose a log file'**
  String get settingsAboutChooseLog;

  /// No description provided for @settingsAboutVersion.
  ///
  /// In en, this message translates to:
  /// **'version {version}'**
  String settingsAboutVersion(String version);

  /// No description provided for @settingsMusicName.
  ///
  /// In en, this message translates to:
  /// **'Music'**
  String get settingsMusicName;

  /// No description provided for @settingsMusicOptionsScrobbleDescriptionOff.
  ///
  /// In en, this message translates to:
  /// **'Don\'t scrobble play history'**
  String get settingsMusicOptionsScrobbleDescriptionOff;

  /// No description provided for @settingsMusicOptionsScrobbleDescriptionOn.
  ///
  /// In en, this message translates to:
  /// **'Scrobble play history'**
  String get settingsMusicOptionsScrobbleDescriptionOn;

  /// No description provided for @settingsMusicOptionsScrobbleTitle.
  ///
  /// In en, this message translates to:
  /// **'Scrobble plays'**
  String get settingsMusicOptionsScrobbleTitle;

  /// No description provided for @settingsNetworkName.
  ///
  /// In en, this message translates to:
  /// **'Network'**
  String get settingsNetworkName;

  /// No description provided for @settingsNetworkOptionsMaxBitrateMobileTitle.
  ///
  /// In en, this message translates to:
  /// **'Maximum bitrate (mobile data)'**
  String get settingsNetworkOptionsMaxBitrateMobileTitle;

  /// No description provided for @settingsNetworkOptionsMaxBitrateWifiTitle.
  ///
  /// In en, this message translates to:
  /// **'Maximum bitrate (Wi-Fi)'**
  String get settingsNetworkOptionsMaxBitrateWifiTitle;

  /// No description provided for @settingsNetworkOptionsMaxBufferTitle.
  ///
  /// In en, this message translates to:
  /// **'Maximum buffer time'**
  String get settingsNetworkOptionsMaxBufferTitle;

  /// No description provided for @settingsNetworkOptionsMinBufferTitle.
  ///
  /// In en, this message translates to:
  /// **'Minimum buffer time'**
  String get settingsNetworkOptionsMinBufferTitle;

  /// No description provided for @settingsNetworkOptionsOfflineMode.
  ///
  /// In en, this message translates to:
  /// **'Offline mode'**
  String get settingsNetworkOptionsOfflineMode;

  /// No description provided for @settingsNetworkOptionsOfflineModeOff.
  ///
  /// In en, this message translates to:
  /// **'Use the internet to sync music.'**
  String get settingsNetworkOptionsOfflineModeOff;

  /// No description provided for @settingsNetworkOptionsOfflineModeOn.
  ///
  /// In en, this message translates to:
  /// **'Don\'t use the internet to sync or play music.'**
  String get settingsNetworkOptionsOfflineModeOn;

  /// No description provided for @settingsNetworkOptionsStreamFormat.
  ///
  /// In en, this message translates to:
  /// **'Preferred stream format'**
  String get settingsNetworkOptionsStreamFormat;

  /// No description provided for @settingsNetworkOptionsStreamFormatServerDefault.
  ///
  /// In en, this message translates to:
  /// **'Use server default'**
  String get settingsNetworkOptionsStreamFormatServerDefault;

  /// No description provided for @settingsNetworkValuesKbps.
  ///
  /// In en, this message translates to:
  /// **'{value}kbps'**
  String settingsNetworkValuesKbps(String value);

  /// No description provided for @settingsNetworkValuesSeconds.
  ///
  /// In en, this message translates to:
  /// **'{value} seconds'**
  String settingsNetworkValuesSeconds(String value);

  /// No description provided for @settingsNetworkValuesUnlimitedKbps.
  ///
  /// In en, this message translates to:
  /// **'Unlimited'**
  String get settingsNetworkValuesUnlimitedKbps;

  /// No description provided for @settingsResetActionsClearImageCache.
  ///
  /// In en, this message translates to:
  /// **'Clear Image Cache'**
  String get settingsResetActionsClearImageCache;

  /// No description provided for @settingsResetName.
  ///
  /// In en, this message translates to:
  /// **'Reset'**
  String get settingsResetName;

  /// No description provided for @settingsServersActionsAdd.
  ///
  /// In en, this message translates to:
  /// **'Add source'**
  String get settingsServersActionsAdd;

  /// No description provided for @settingsServersActionsDelete.
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get settingsServersActionsDelete;

  /// No description provided for @settingsServersActionsEdit.
  ///
  /// In en, this message translates to:
  /// **'Edit source'**
  String get settingsServersActionsEdit;

  /// No description provided for @settingsServersActionsSave.
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get settingsServersActionsSave;

  /// No description provided for @settingsServersActionsTestConnection.
  ///
  /// In en, this message translates to:
  /// **'Test connection'**
  String get settingsServersActionsTestConnection;

  /// No description provided for @settingsServersFieldsAddress.
  ///
  /// In en, this message translates to:
  /// **'Address'**
  String get settingsServersFieldsAddress;

  /// No description provided for @settingsServersFieldsName.
  ///
  /// In en, this message translates to:
  /// **'Name'**
  String get settingsServersFieldsName;

  /// No description provided for @settingsServersFieldsPassword.
  ///
  /// In en, this message translates to:
  /// **'Password'**
  String get settingsServersFieldsPassword;

  /// No description provided for @settingsServersFieldsUsername.
  ///
  /// In en, this message translates to:
  /// **'Username'**
  String get settingsServersFieldsUsername;

  /// No description provided for @settingsServersMessagesConnectionFailed.
  ///
  /// In en, this message translates to:
  /// **'Connection to {address} failed, check settings or server'**
  String settingsServersMessagesConnectionFailed(String address);

  /// No description provided for @settingsServersMessagesConnectionOk.
  ///
  /// In en, this message translates to:
  /// **'Connection to {address} OK!'**
  String settingsServersMessagesConnectionOk(String address);

  /// No description provided for @settingsServersName.
  ///
  /// In en, this message translates to:
  /// **'Sources'**
  String get settingsServersName;

  /// No description provided for @settingsServersOptionsForcePlaintextPasswordDescriptionOff.
  ///
  /// In en, this message translates to:
  /// **'Send password as token + salt'**
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOff;

  /// No description provided for @settingsServersOptionsForcePlaintextPasswordDescriptionOn.
  ///
  /// In en, this message translates to:
  /// **'Send password in plaintext (legacy, make sure your connection is secure!)'**
  String get settingsServersOptionsForcePlaintextPasswordDescriptionOn;

  /// No description provided for @settingsServersOptionsForcePlaintextPasswordTitle.
  ///
  /// In en, this message translates to:
  /// **'Force plaintext password'**
  String get settingsServersOptionsForcePlaintextPasswordTitle;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) => <String>[
    'ar',
    'ca',
    'cs',
    'da',
    'de',
    'en',
    'es',
    'fr',
    'gl',
    'it',
    'ja',
    'nb',
    'pa',
    'pl',
    'pt',
    'ru',
    'tr',
    'vi',
    'zh',
  ].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'ar':
      return AppLocalizationsAr();
    case 'ca':
      return AppLocalizationsCa();
    case 'cs':
      return AppLocalizationsCs();
    case 'da':
      return AppLocalizationsDa();
    case 'de':
      return AppLocalizationsDe();
    case 'en':
      return AppLocalizationsEn();
    case 'es':
      return AppLocalizationsEs();
    case 'fr':
      return AppLocalizationsFr();
    case 'gl':
      return AppLocalizationsGl();
    case 'it':
      return AppLocalizationsIt();
    case 'ja':
      return AppLocalizationsJa();
    case 'nb':
      return AppLocalizationsNb();
    case 'pa':
      return AppLocalizationsPa();
    case 'pl':
      return AppLocalizationsPl();
    case 'pt':
      return AppLocalizationsPt();
    case 'ru':
      return AppLocalizationsRu();
    case 'tr':
      return AppLocalizationsTr();
    case 'vi':
      return AppLocalizationsVi();
    case 'zh':
      return AppLocalizationsZh();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
