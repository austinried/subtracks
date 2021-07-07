import React from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import { currentTrackAtom, playerStateAtom, usePause, usePlay, useProgress } from '../state/trackplayer'
import CoverArt from './common/CoverArt'
import colors from '../styles/colors'
import { Font } from '../styles/text'
import PressableImage from './common/PressableImage'
import { State } from 'react-native-track-player'

const ProgressBar = () => {
  const { position, duration } = useProgress()

  let progress = 0
  if (duration > 0) {
    progress = position / duration
  }

  return (
    <View style={progressStyles.container}>
      <View style={{ ...progressStyles.left, flex: progress }} />
      <View style={{ ...progressStyles.right, flex: 1 - progress }} />
    </View>
  )
}

const progressStyles = StyleSheet.create({
  container: {
    height: 2,
    flexDirection: 'row',
  },
  left: {
    backgroundColor: colors.text.primary,
  },
  right: {
    backgroundColor: '#595959',
  },
})

const NowPlayingBar = () => {
  const navigation = useNavigation()
  const track = useAtomValue(currentTrackAtom)
  const playerState = useAtomValue(playerStateAtom)
  const play = usePlay()
  const pause = usePause()

  let playPauseIcon: number
  let playPauseAction: () => void

  switch (playerState) {
    case State.Playing:
    case State.Buffering:
    case State.Connecting:
      playPauseIcon = require('../../res/pause-fill.png')
      playPauseAction = pause
      break
    default:
      playPauseIcon = require('../../res/play-fill.png')
      playPauseAction = play
      break
  }

  return (
    <Pressable
      onPress={() => navigation.navigate('NowPlaying')}
      style={{ ...styles.container, display: track ? 'flex' : 'none' }}>
      <ProgressBar />
      <View style={styles.subContainer}>
        <CoverArt
          PlaceholderComponent={() => <Text>hi</Text>}
          height={styles.subContainer.height}
          width={styles.subContainer.height}
          coverArtUri={track?.artworkThumb}
        />
        <View style={styles.detailsContainer}>
          <Text numberOfLines={1} style={styles.detailsTitle}>
            {track?.title}
          </Text>
          <Text numberOfLines={1} style={styles.detailsAlbum}>
            {track?.artist}
          </Text>
        </View>
        <View style={styles.controls}>
          <PressableImage
            onPress={playPauseAction}
            source={playPauseIcon}
            style={styles.play}
            tintColor="white"
            hitSlop={14}
          />
        </View>
      </View>
    </Pressable>
  )
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    backgroundColor: colors.gradient.high,
    borderBottomColor: colors.gradient.low,
    borderBottomWidth: 1,
  },
  subContainer: {
    height: 60,
    flexDirection: 'row',
  },
  detailsContainer: {
    flex: 1,
    height: '100%',
    justifyContent: 'center',
    marginLeft: 10,
    // backgroundColor: 'green',
  },
  detailsTitle: {
    fontFamily: Font.semiBold,
    fontSize: 13,
    color: colors.text.primary,
  },
  detailsAlbum: {
    fontFamily: Font.regular,
    fontSize: 13,
    color: colors.text.secondary,
  },
  controls: {
    flexDirection: 'row',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 14,
    marginLeft: 12,
  },
  play: {
    height: 32,
    width: 32,
  },
})

export default NowPlayingBar
