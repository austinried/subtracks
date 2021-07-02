import { useAtomValue } from 'jotai/utils';
import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, View, StatusBar, useWindowDimensions } from 'react-native';
import FastImage from 'react-native-fast-image';
import { currentQueueNameAtom, currentTrackAtom } from '../state/trackplayer';
import colors from '../styles/colors';
import text from '../styles/text';
import CoverArt from './common/CoverArt';
import GradientBackground from './common/GradientBackground';
import ImageColors from 'react-native-image-colors';

const NowPlayingHeader = () => {
  const queueName = useAtomValue(currentQueueNameAtom);

  return (
    <View style={headerStyles.container}>
      <FastImage source={require('../../res/arrow_left-fill.png')} style={headerStyles.backArrow} tintColor="white" />
      <Text numberOfLines={2} style={headerStyles.queueName}>
        {queueName}
      </Text>
      <FastImage source={require('../../res/more_vertical.png')} style={headerStyles.more} tintColor="white" />
    </View>
  );
};

const headerStyles = StyleSheet.create({
  container: {
    height: 60,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    // backgroundColor: 'green',
  },
  backArrow: {
    height: 24,
    width: 24,
    margin: 20,
  },
  queueName: {
    ...text.paragraph,
  },
  more: {
    height: 24,
    width: 24,
    margin: 20,
  },
});

const SongCoverArt = () => {
  const track = useAtomValue(currentTrackAtom);
  const layout = useWindowDimensions();

  const size = layout.width - layout.width / 6;

  return (
    <View style={coverArtStyles.container}>
      <CoverArt
        PlaceholderComponent={() => (
          <View style={{ height: size, width: size }}>
            <Text>Failed</Text>
          </View>
        )}
        height={size}
        width={size}
        coverArtUri={track?.artwork as string}
      />
    </View>
  );
};

const coverArtStyles = StyleSheet.create({
  container: {
    width: '100%',
    alignItems: 'center',
    marginTop: 20,
  },
});

const SongInfo = () => {
  const track = useAtomValue(currentTrackAtom);

  return (
    <View style={infoStyles.container}>
      <Text style={infoStyles.title}>{track?.title}</Text>
      <Text style={infoStyles.artist}>{track?.artist}</Text>
    </View>
  );
};

const infoStyles = StyleSheet.create({
  container: {
    width: '100%',
    alignItems: 'center',
    marginTop: 20,
    paddingHorizontal: 20,
  },
  title: {
    ...text.songListTitle,
    fontSize: 22,
    textAlign: 'center',
  },
  artist: {
    ...text.songListSubtitle,
    fontSize: 14,
    textAlign: 'center',
  },
});

interface AndroidImageColors {
  dominant?: string;
  average?: string;
  vibrant?: string;
  darkVibrant?: string;
  lightVibrant?: string;
  darkMuted?: string;
  lightMuted?: string;
  muted?: string;
  platform: 'android';
}

interface IOSImageColors {
  background: string;
  primary: string;
  secondary: string;
  detail: string;
  quality: Config['quality'];
  platform: 'ios';
}

interface Config {
  fallback?: string;
  pixelSpacing?: number;
  quality?: 'lowest' | 'low' | 'high' | 'highest';
  cache?: boolean;
  key?: string;
}

declare type ImageColorsResult = AndroidImageColors | IOSImageColors;

const NowPlayingLayout = () => {
  const track = useAtomValue(currentTrackAtom);
  const [imageColors, setImageColors] = useState<ImageColorsResult | undefined>(undefined);
  const ica = imageColors as AndroidImageColors;

  useEffect(() => {
    async function getColors() {
      if (track?.artwork === undefined) {
        return;
      }

      const cachedResult = ImageColors.cache.getItem(track.artwork as string);
      if (cachedResult) {
        setImageColors(cachedResult);
        return;
      }

      const result = await ImageColors.getColors(track.artwork as string, {
        cache: true,
      });
      setImageColors(result);
    }
    getColors();
  }, [track]);

  return (
    <View
      style={{
        flex: 1,
        paddingTop: StatusBar.currentHeight,
      }}>
      <GradientBackground
        colors={[ica ? (ica.muted as string) : colors.gradient.high, colors.gradient.low]}
        locations={[0.1, 1.0]}
      />
      <NowPlayingHeader />
      <SongCoverArt />
      <SongInfo />
    </View>
  );
};

export default NowPlayingLayout;
