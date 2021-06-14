import React from 'react';
import { RecoilRoot } from 'recoil';
import Layout from './src/components/NowPlayingLayout';

const App = () => {
  return (
    <RecoilRoot>
      <Layout />
    </RecoilRoot>
  );
}

export default App;
