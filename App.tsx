import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import SplashPage from './src/components/SplashPage';
import RootNavigator from './src/components/navigation/RootNavigator';
import { Provider } from 'jotai';

const App = () => (
  <Provider>
    <SplashPage>
      <NavigationContainer>
        <RootNavigator />
      </NavigationContainer>
    </SplashPage>
  </Provider>
);

export default App;
