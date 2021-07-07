import 'react-native-gesture-handler'
import 'react-native-get-random-values'

import { enableScreens } from 'react-native-screens'
enableScreens()

import { AppRegistry } from 'react-native'
import App from './App'
import { name as appName } from './app.json'
import TrackPlayer, { Capability } from 'react-native-track-player'

AppRegistry.registerComponent(appName, () => App)
TrackPlayer.registerPlaybackService(() => require('./src/playback/service'))

async function start() {
  await TrackPlayer.setupPlayer()
  await TrackPlayer.updateOptions({
    capabilities: [
      Capability.Play,
      Capability.Pause,
      Capability.Stop,
      Capability.SkipToNext,
      Capability.SkipToPrevious,
    ],
    compactCapabilities: [
      Capability.Play, //
      Capability.Pause,
      Capability.SkipToNext,
      Capability.SkipToPrevious,
    ],
    playIcon: require('./res/notification/ic_stat_play.png'),
    pauseIcon: require('./res/notification/ic_stat_pause.png'),
    stopIcon: require('./res/notification/ic_stat_stop.png'),
    nextIcon: require('./res/notification/ic_stat_next.png'),
    previousIcon: require('./res/notification/ic_stat_previous.png'),
    icon: require('./res/notification/ic_stat_play.png'),
  })
}
start()
