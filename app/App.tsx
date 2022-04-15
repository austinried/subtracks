import RootNavigator from '@app/navigation/RootNavigator'
import SplashPage from '@app/screens/SplashPage'
import colors from '@app/styles/colors'
import React from 'react'
import { StatusBar, StyleSheet, View } from 'react-native'
import { MenuProvider } from 'react-native-popup-menu'
import { QueryClientProvider } from 'react-query'
import ProgressHook from './components/ProgressHook'
import queryClient from './queryClient'
import { useStore } from './state/store'

const Debug = () => {
  const currentTrackTitle = useStore(store => store.currentTrack?.title)
  console.log('currentTrackTitle', currentTrackTitle)
  return <></>
}

const App = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <MenuProvider backHandler={true}>
        <View style={styles.appContainer}>
          <StatusBar
            animated={true}
            backgroundColor={'rgba(0, 0, 0, 0.3)'}
            barStyle={'light-content'}
            translucent={true}
          />
          <SplashPage>
            <ProgressHook />
            <Debug />
            <RootNavigator />
          </SplashPage>
        </View>
      </MenuProvider>
    </QueryClientProvider>
  )
}

const styles = StyleSheet.create({
  appContainer: {
    flex: 1,
    backgroundColor: colors.gradient.high,
  },
})

export default App
