import React from 'react'
import SplashPage from '@app/screens/SplashPage'
import RootNavigator from '@app/navigation/RootNavigator'
import { Provider } from 'jotai'
import { StatusBar, View } from 'react-native'
import colors from '@app/styles/colors'
import TrackPlayerState from '@app/components/TrackPlayerState'

const App = () => (
  <Provider>
    <StatusBar animated={true} backgroundColor={'rgba(0, 0, 0, 0.4)'} barStyle={'light-content'} translucent={true} />
    <TrackPlayerState />
    <View style={{ flex: 1, backgroundColor: colors.gradient.high }}>
      <SplashPage>
        <RootNavigator />
      </SplashPage>
    </View>
  </Provider>
)

export default App
