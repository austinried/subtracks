import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { RecoilRoot } from 'recoil';
import RootNavigator from './src/components/RootNavigator';

const App = () => (
  <RecoilRoot>
    <NavigationContainer>
      <RootNavigator />
    </NavigationContainer>
  </RecoilRoot>
);

export default App;
