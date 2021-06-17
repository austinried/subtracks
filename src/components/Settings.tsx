import React from 'react';
import { Button, View } from 'react-native';
import { musicDb, settingsDb } from '../clients';

const DbControls = () => {

  const recreateMusicDb = async () => {
    try { await musicDb.deleteDb(); } catch {}
    await musicDb.createDb();
  }

  const recreateSettingsDb = async () => {
    try { await settingsDb.deleteDb(); } catch {}
    await settingsDb.createDb();
  }

  return (
    <View>
      <Button 
        title='Recreate Music DB'
        onPress={recreateMusicDb}
      />
      <Button 
        title='Recreate Settings DB'
        onPress={recreateSettingsDb}
      />
    </View>
  );
}

const SettingsView = () => (
  <View>
    <DbControls />
  </View>
)

export default SettingsView;
