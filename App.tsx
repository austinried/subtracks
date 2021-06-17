import React from 'react';
import { RecoilRoot } from 'recoil';
import ArtistsList from './src/components/ArtistsList';

const App = () => (
  <RecoilRoot>
    <ArtistsList />
  </RecoilRoot>
);

export default App;
