import React, { useEffect, useState } from 'react';
import { Button, TextInput, View, Text } from 'react-native';
import { useRecoilState } from 'recoil';
import { v4 as uuidv4 } from 'uuid';
import md5 from 'md5';
import { musicDb } from '../clients';
import { appSettingsState } from '../state/settings';
import { DbStorage } from '../storage/db';
import { StackScreenProps } from '@react-navigation/stack';
import { useNavigation } from '@react-navigation/core';

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
  const navigation = useNavigation();
  return (
    <View>
      <RecreateDbButton db={musicDb} title='Music' />
      <Button
        title='Now Playing'
        onPress={() => navigation.navigate('Now Playing')}
      />
    </View>
  );
}

const ServerSettingsView = () => {
  const [appSettings, setAppSettings] = useRecoilState(appSettingsState);

  const bootstrapServer = () => {
    if (appSettings.servers.length !== 0) {
      return;
    }

    const id = uuidv4();
    const salt = uuidv4();
    const address = 'http://demo.subsonic.org';

    setAppSettings({
      ...appSettings,
      servers: [
        ...appSettings.servers,
        {
          id, salt, address,
          username: 'guest',
          token: md5('guest' + salt),
        },
      ],
      activeServer: id,
    });
  };

  return (
    <View>
      <Button
        title='Add default server'
        onPress={bootstrapServer}
      />
      {appSettings.servers.map(s => (
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
