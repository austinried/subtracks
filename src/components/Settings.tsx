import { useNavigation } from '@react-navigation/core';
import md5 from 'md5';
import React from 'react';
import { Button, Text, View } from 'react-native';
import { useRecoilState } from 'recoil';
import { v4 as uuidv4 } from 'uuid';
import { appSettingsState } from '../state/settings';

const TestControls = () => {
  const navigation = useNavigation();
  return (
    <View>
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
    <TestControls />
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <ServerSettingsView />
    </React.Suspense>
  </View>
)

export default SettingsView;
