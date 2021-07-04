import { useAtomValue } from 'jotai/utils';
import React from 'react';
import { StatusBar, StyleSheet, Text, useWindowDimensions, View } from 'react-native';
import FastImage from 'react-native-fast-image';
import { currentQueueNameAtom, currentTrackAtom } from '../state/trackplayer';
import text from '../styles/text';
import CoverArt from './common/CoverArt';
import ImageGradientBackground from './common/ImageGradientBackground';

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

const NowPlayingLayout = () => {
  const track = useAtomValue(currentTrackAtom);

  return (
    <View
      style={{
        flex: 1,
        paddingTop: StatusBar.currentHeight,
      }}>
      <ImageGradientBackground imageUri={track?.artworkThumb as string} imageKey={`${track?.album}${track?.artist}`} />
      <NowPlayingHeader />
      <SongCoverArt />
      <SongInfo />
    </View>
  );
};

export default NowPlayingLayout;
