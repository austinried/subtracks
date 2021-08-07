import RootNavigator from '@app/navigation/RootNavigator'
import SplashPage from '@app/screens/SplashPage'
import colors from '@app/styles/colors'
import React from 'react'
import { StatusBar, View } from 'react-native'
import ProgressHook from './components/ProgressHook'
import { useStore } from './state/store'
import { selectTrackPlayer } from './state/trackplayer'
import { MenuProvider } from 'react-native-popup-menu'

const Debug = () => {
  const currentTrack = useStore(selectTrackPlayer.currentTrack)
  console.log(currentTrack?.title)
  return <></>
}

const App = () => (
  <MenuProvider>
    <View style={{ flex: 1, backgroundColor: colors.gradient.high }}>
      <StatusBar animated={true} backgroundColor={'rgba(0, 0, 0, 0.4)'} barStyle={'light-content'} translucent={true} />
      <SplashPage>
        <ProgressHook />
        <Debug />
        <RootNavigator />
      </SplashPage>
    </View>
  </MenuProvider>
)

export default App
