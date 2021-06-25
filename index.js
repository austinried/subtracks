import 'react-native-gesture-handler';
import 'react-native-get-random-values';

import { AppRegistry, LogBox } from 'react-native';

// ignore recoil's timer warning on android:
// https://github.com/facebookexperimental/Recoil/issues/1030
LogBox.ignoreLogs(["timer"]);

import App from './App';
import { name as appName } from './app.json';
import TrackPlayer from 'react-native-track-player';

AppRegistry.registerComponent(appName, () => App);
TrackPlayer.registerPlaybackService(() => require('./src/playback/service'));
