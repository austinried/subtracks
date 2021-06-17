import React from 'react';
import { RecoilRoot } from 'recoil';
import SettingsView from './src/components/Settings';

const App = () => (
  <RecoilRoot>
    <SettingsView />
  </RecoilRoot>
);

export default App;
