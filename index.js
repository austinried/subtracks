import 'react-native-gesture-handler'

import { enableScreens } from 'react-native-screens'
enableScreens()

import { LogBox } from 'react-native'
LogBox.ignoreLogs([
  "The action 'POP_TO_TOP'",
  '`new NativeEventEmitter()` was called with a non-null argument without the required `addListener` method.',
  '`new NativeEventEmitter()` was called with a non-null argument without the required `removeListeners` method.',
  "[react-native-gesture-handler] Seems like you're using an old API with gesture components, check out new Gestures system!",
])

import i18next from 'i18next'
import { initReactI18next } from 'react-i18next'
import { backend, languageDetector } from '@app/i18n/i18n'
import * as RNLocalize from 'react-native-localize'

i18next.use(backend).use(languageDetector).use(initReactI18next).init({
  compatibilityJSON: 'v3',
  fallbackLng: 'en',
  debug: true,
})

RNLocalize.addEventListener('change', () => {
  languageDetector.detect(lng => i18next.changeLanguage(lng))
})

import { AppRegistry } from 'react-native'
import App from '@app/App'
import { name as appName } from '@app/app.json'
import TrackPlayer, { Capability } from 'react-native-track-player'

AppRegistry.registerComponent(appName, () => App)
TrackPlayer.registerPlaybackService(() => require('@app/trackplayer/service'))

async function start() {
  await TrackPlayer.setupPlayer()
  await TrackPlayer.updateOptions({
    capabilities: [
      Capability.Play,
      Capability.Pause,
      Capability.Stop,
      Capability.SkipToNext,
      Capability.SkipToPrevious,
      Capability.SeekTo,
    ],
    compactCapabilities: [
      Capability.Play, //
      Capability.Pause,
      Capability.SkipToNext,
      Capability.SkipToPrevious,
    ],
    playIcon: require('@res/icons/notification/ic_stat_play.png'),
    pauseIcon: require('@res/icons/notification/ic_stat_pause.png'),
    stopIcon: require('@res/icons/notification/ic_stat_stop.png'),
    nextIcon: require('@res/icons/notification/ic_stat_next.png'),
    previousIcon: require('@res/icons/notification/ic_stat_previous.png'),
    icon: require('@res/icons/notification/ic_stat_play.png'),
  })
}
start()
