import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { RecoilRoot } from 'recoil';
import SplashPage from './src/components/SplashPage';
import RootNavigator from './src/components/navigation/RootNavigator';

const App = () => (
  <RecoilRoot>
    <SplashPage>
      <NavigationContainer>
        <RootNavigator />
      </NavigationContainer>
    </SplashPage>
  </RecoilRoot>
);

export default App;
