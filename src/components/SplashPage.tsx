import React, { useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import RNFS from 'react-native-fs';
import { musicDb, settingsDb } from '../clients';

async function mkdir(path: string): Promise<void> {
  const exists = await RNFS.exists(path);
  if (exists) {
    const isDir = (await RNFS.stat(path)).isDirectory();
    if (!isDir) {
      throw new Error(`path exists and is not a directory: ${path}`);
    } else {
      return;
    }
  }

  return await RNFS.mkdir(path);
}

const SplashPage: React.FC<{}> = ({ children }) => {
  const [ready, setReady] = useState(false);

  const minSplashTime = new Promise(resolve => setTimeout(resolve, 1));

  const prepare = async () => {
    const filesPath = RNFS.DocumentDirectoryPath;

    await mkdir(`${filesPath}/image_cache`);
    await mkdir(`${filesPath}/song_cache`);
    await mkdir(`${filesPath}/songs`);

    await musicDb.openDb();
    await settingsDb.openDb();

    if (!(await musicDb.dbExists())) {
      await musicDb.createDb();
    }
    if (!(await settingsDb.dbExists())) {
      await settingsDb.createDb();
    }
  }

  const promise = Promise.all([
    prepare(), minSplashTime,
  ]);

  useEffect(() => {
    promise.then(() => {
      setReady(true);
    });
  })

  if (!ready) {
    return <Text>Loading THE GOOD SHIT...</Text>
  }
  return (
    <View style={{ flex: 1 }}>{children}</View>
  );
}

export default SplashPage;
