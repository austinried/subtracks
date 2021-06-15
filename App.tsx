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

import { SubsonicApiClient } from './src/subsonic/api';
import md5 from 'md5';

const password = 'test';
const salt = 'salty';
const token = md5(password + salt);

const client = new SubsonicApiClient('http://navidrome.home', 'austin', token, salt);

client.ping();
client.getArtists();
