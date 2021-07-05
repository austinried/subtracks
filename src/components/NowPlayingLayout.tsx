import { useAtomValue } from 'jotai/utils';
import React from 'react';
import { Pressable, StatusBar, StyleSheet, Text, useWindowDimensions, View } from 'react-native';
import FastImage from 'react-native-fast-image';
import TrackPlayer, { State } from 'react-native-track-player';
import { currentQueueNameAtom, currentTrackAtom, playerStateAtom } from '../state/trackplayer';
import text from '../styles/text';
import CoverArt from './common/CoverArt';
import ImageGradientBackground from './common/ImageGradientBackground';

const NowPlayingHeader = () => {
  const queueName = useAtomValue(currentQueueNameAtom);

  return (
    <View style={headerStyles.container}>
      <FastImage source={require('../../res/arrow_left-fill.png')} style={headerStyles.icons} tintColor="white" />
      <Text numberOfLines={2} style={headerStyles.queueName}>
        {queueName || 'Nothing playing...'}
      </Text>
      <FastImage source={require('../../res/more_vertical.png')} style={headerStyles.icons} tintColor="white" />
    </View>
  );
};

const headerStyles = StyleSheet.create({
  container: {
    height: 58,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    // backgroundColor: 'green',
  },
  icons: {
    height: 22,
    width: 22,
    margin: 17,
  },
  queueName: {
    ...text.paragraph,
  },
});

const SongCoverArt = () => {
  const track = useAtomValue(currentTrackAtom);
  const layout = useWindowDimensions();

  const size = layout.width - layout.width / 7;

  return (
    <View style={coverArtStyles.container}>
      <CoverArt
        PlaceholderComponent={() => <View style={{ height: size, width: size }} />}
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
    marginTop: 10,
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

const PlayerControls = () => {
  const state = useAtomValue(playerStateAtom);

  let playPauseIcon: number;
  let playPauseStyle: any;
  let playPauseAction: () => void;

  switch (state) {
    case State.Playing:
    case State.Buffering:
    case State.Connecting:
      playPauseIcon = require('../../res/pause_circle-fill.png');
      playPauseStyle = controlsStyles.enabled;
      playPauseAction = () => TrackPlayer.pause();
      break;
    case State.Paused:
      playPauseIcon = require('../../res/play_circle-fill.png');
      playPauseStyle = controlsStyles.enabled;
      playPauseAction = () => TrackPlayer.play();
      break;
    default:
      playPauseIcon = require('../../res/play_circle-fill.png');
      playPauseStyle = controlsStyles.disabled;
      playPauseAction = () => {};
      break;
  }

  return (
    <View style={controlsStyles.container}>
      <FastImage
        source={require('../../res/previous-fill.png')}
        tintColor="white"
        style={{ ...controlsStyles.skip, ...playPauseStyle }}
      />
      <Pressable onPress={playPauseAction}>
        <FastImage source={playPauseIcon} tintColor="white" style={{ ...controlsStyles.play, ...playPauseStyle }} />
      </Pressable>
      <FastImage
        source={require('../../res/next-fill.png')}
        tintColor="white"
        style={{ ...controlsStyles.skip, ...playPauseStyle }}
      />
    </View>
  );
};

const controlsStyles = StyleSheet.create({
  container: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 40,
  },
  skip: {
    height: 40,
    width: 40,
    marginHorizontal: 18,
  },
  play: {
    height: 90,
    width: 90,
  },
  enabled: {
    opacity: 1,
  },
  disabled: {
    opacity: 0.35,
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
      <PlayerControls />
    </View>
  );
};

export default NowPlayingLayout;
