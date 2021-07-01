import React from 'react';
import { DarkTheme, NavigationContainer } from '@react-navigation/native';
import SplashPage from './src/components/SplashPage';
import RootNavigator from './src/components/navigation/RootNavigator';
import { Provider } from 'jotai';
import { StatusBar } from 'react-native';
import colors from './src/styles/colors';

const theme = { ...DarkTheme };
theme.colors.background = colors.gradient.high;

const App = () => (
  <Provider>
    <StatusBar animated={true} backgroundColor={'rgba(0, 0, 0, 0.4)'} barStyle={'light-content'} translucent={true} />
    <SplashPage>
      <NavigationContainer theme={theme}>
        <RootNavigator />
      </NavigationContainer>
    </SplashPage>
  </Provider>
);

export default App;
