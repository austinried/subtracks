import 'react-native-gesture-handler';
import 'react-native-get-random-values';

import { enableScreens } from 'react-native-screens';
enableScreens();

import { AppRegistry } from 'react-native';
import App from './App';
import { name as appName } from './app.json';
import TrackPlayer from 'react-native-track-player';

AppRegistry.registerComponent(appName, () => App);
TrackPlayer.registerPlaybackService(() => require('./src/playback/service'));
