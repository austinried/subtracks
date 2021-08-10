import CoverArt from '@app/components/CoverArt'
import PressableOpacity from '@app/components/PressableOpacity'
import { usePause, usePlay } from '@app/hooks/trackplayer'
import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import React from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import { State } from 'react-native-track-player'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'

const ProgressBar = () => {
  const { position, duration } = useStore(selectTrackPlayer.progress)

  let progress = 0
  if (duration > 0) {
    progress = position / duration
  }

  return (
    <View style={progressStyles.container}>
      <View style={[progressStyles.left, { flex: progress }]} />
      <View style={[progressStyles.right, { flex: 1 - progress }]} />
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
  const track = useStore(selectTrackPlayer.currentTrack)
  const playerState = useStore(selectTrackPlayer.playerState)
  const play = usePlay()
  const pause = usePause()

  let playPauseIcon: string
  let playPauseAction: () => void

  switch (playerState) {
    case State.Playing:
      playPauseIcon = 'pause'
      playPauseAction = pause
      break
    default:
      playPauseIcon = 'play'
      playPauseAction = play
      break
  }

  return (
    <Pressable
      onPress={() => navigation.navigate('now-playing')}
      style={{ ...styles.container, display: track ? 'flex' : 'none' }}>
      <ProgressBar />
      <View style={styles.subContainer}>
        <CoverArt
          style={{ height: styles.subContainer.height, width: styles.subContainer.height }}
          coverArt={track?.coverArt || '-1'}
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
          <PressableOpacity onPress={playPauseAction} hitSlop={14}>
            <IconFA5 name={playPauseIcon} size={28} color="white" />
          </PressableOpacity>
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
  },
  detailsTitle: {
    fontFamily: font.semiBold,
    fontSize: 13,
    color: colors.text.primary,
  },
  detailsAlbum: {
    fontFamily: font.regular,
    fontSize: 13,
    color: colors.text.secondary,
  },
  controls: {
    flexDirection: 'row',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 18,
    marginLeft: 12,
  },
})

export default NowPlayingBar
