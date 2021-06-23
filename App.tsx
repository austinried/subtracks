import React from 'react';
import { RecoilRoot } from 'recoil';
import SplashPage from './src/components/SplashPage';
import RootNavigator from './src/components/navigation/RootNavigator';
import BottomTabNavigator from './src/components/navigation/BottomTabNavigator';

const App = () => (
  <RecoilRoot>
    <SplashPage>
      <BottomTabNavigator />
    </SplashPage>
  </RecoilRoot>
);

export default App;
