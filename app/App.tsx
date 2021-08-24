import RootNavigator from '@app/navigation/RootNavigator'
import SplashPage from '@app/screens/SplashPage'
import colors from '@app/styles/colors'
import React from 'react'
import { StatusBar, View, StyleSheet } from 'react-native'
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
  <MenuProvider backHandler={true}>
    <View style={styles.appContainer}>
      <StatusBar animated={true} backgroundColor={'rgba(0, 0, 0, 0.3)'} barStyle={'light-content'} translucent={true} />
      <SplashPage>
        <ProgressHook />
        <Debug />
        <RootNavigator />
      </SplashPage>
    </View>
  </MenuProvider>
)

const styles = StyleSheet.create({
  appContainer: {
    flex: 1,
    backgroundColor: 'green', // change test
  },
})

export default App
