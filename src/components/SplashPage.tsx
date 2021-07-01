import React, { useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import RNFS from 'react-native-fs';
import TrackPlayer, { Capability, Track } from 'react-native-track-player';
import paths from '../paths';

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
    await mkdir(paths.imageCache);
    await mkdir(paths.songCache);
    await mkdir(paths.songs);

    await TrackPlayer.setupPlayer();
    TrackPlayer.updateOptions({
      capabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.Stop,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
      ],
      compactCapabilities: [Capability.Play, Capability.Pause, Capability.SkipToNext, Capability.SkipToPrevious],
    });

    const castlevania: Track = {
      id: 'castlevania',
      url: 'http://www.vgmuseum.com/mrp/cv1/music/03.mp3',
      title: 'Stage 1: Castle Entrance',
      artist: 'Kinuyo Yamashita and S.Terishima',
      duration: 110,
      artwork: 'https://webgames.host/uploads/2017/03/castlevania-3-draculas-curse.jpg',
      genre: 'BGM',
      date: new Date(1989, 1).toISOString(),
    };

    await TrackPlayer.add([castlevania]);
    // TrackPlayer.play();
  };

  const promise = Promise.all([prepare(), minSplashTime]);

  useEffect(() => {
    promise.then(() => {
      setReady(true);
    });
  });

  if (!ready) {
    return <Text>Loading THE GOOD SHIT...</Text>;
  }
  return <View style={{ flex: 1 }}>{children}</View>;
};

export default SplashPage;
