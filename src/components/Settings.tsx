import React, { useContext, useEffect, useState } from 'react';
import { Button, TextInput, View, Text } from 'react-native';
import { useRecoilState } from 'recoil';
import { v4 as uuidv4 } from 'uuid';
import md5 from 'md5';
import { musicDb, settingsDb } from '../clients';
import { appSettingsState, serversState } from '../state/settings';
import { DbStorage } from '../storage/db';
import { NavigationContext } from 'navigation-react';

const RecreateDbButton: React.FC<{ db: DbStorage, title: string }> = ({ db, title }) => {
  const [inProgress, setInProgress] = useState(false);

  const recreateDb = async () => {
    setInProgress(true);
    try{
      try { await db.deleteDb(); } catch {}
      await db.createDb();
    } finally {
      setInProgress(false);
    }
  }

  return (
    <Button
      title={`Recreate ${title} DB`}
      onPress={recreateDb}
      disabled={inProgress}
    />
  )
}

const DbControls = () => {
  const { stateNavigator } = useContext(NavigationContext);
  return (
    <View>
      <RecreateDbButton db={musicDb} title='Music' />
      <RecreateDbButton db={settingsDb} title='Settings' />
      <Button
        title='Now Playing'
        onPress={() => stateNavigator.navigate('nowplaying')}
      />
    </View>
  );
}

const ServerSettingsView = () => {
  const [servers, setServers] = useRecoilState(serversState);
  const [appSettings, setAppSettings] = useRecoilState(appSettingsState);

  const bootstrapServer = () => {
    if (servers.length !== 0) {
      return;
    }

    const id = uuidv4();
    const salt = uuidv4();
    const address = 'http://demo.subsonic.org';

    setServers([{
      id, salt, address,
      username: 'guest',
      token: md5('guest' + salt),
    }]);

    setAppSettings({
      server: id,
    });
  };

  return (
    <View>
      <Button
        title='Add default server'
        onPress={bootstrapServer}
      />
      {servers.map(s => (
        <View key={s.id}>
          <Text>{s.address}</Text>
          <Text>{s.username}</Text>
        </View>
      ))}
    </View>
  );
}

const SettingsView = () => (
  <View>
    <DbControls />
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <ServerSettingsView />
    </React.Suspense>
  </View>
)

export default SettingsView;
