import CoverArt from '@app/components/CoverArt'
import HeaderBar from '@app/components/HeaderBar'
import ImageGradientBackground from '@app/components/ImageGradientBackground'
import PressableOpacity from '@app/components/PressableOpacity'
import Star from '@app/components/Star'
import { useStarred } from '@app/hooks/music'
import { useNext, usePause, usePlay, usePrevious, useSeekTo } from '@app/hooks/trackplayer'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import { QueueContextType, selectTrackPlayer, TrackExt } from '@app/state/trackplayer'
import { selectTrackPlayerMap } from '@app/state/trackplayermap'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import formatDuration from '@app/util/formatDuration'
import Slider from '@react-native-community/slider'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback, useEffect, useState } from 'react'
import { ActivityIndicator, StyleSheet, Text, TextStyle, View } from 'react-native'
import { NativeStackScreenProps } from 'react-native-screens/native-stack'
import { RepeatMode, State } from 'react-native-track-player'
import IconFA from 'react-native-vector-icons/FontAwesome'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'
import Icon from 'react-native-vector-icons/Ionicons'
import IconMatCom from 'react-native-vector-icons/MaterialCommunityIcons'

function getContextName(type?: QueueContextType) {
  switch (type) {
    case 'album':
      return 'Album'
    case 'artist':
      return 'Top Songs'
    case 'playlist':
      return 'Playlist'
    case 'song':
      return 'Search Results'
    default:
      return undefined
  }
}

const NowPlayingHeader = React.memo<{
  track?: TrackExt
}>(({ track }) => {
  const queueName = useStore(selectTrackPlayer.queueName)
  const queueContextType = useStore(selectTrackPlayer.queueContextType)
  const mapTrackExtToSong = useStore(selectTrackPlayerMap.mapTrackExtToSong)

  if (!track) {
    return <></>
  }

  let contextName = getContextName(queueContextType)

  return (
    <HeaderBar
      headerStyle={headerStyles.bar}
      contextItem={mapTrackExtToSong(track)}
      HeaderCenter={() => (
        <View style={headerStyles.center}>
          {contextName !== undefined && (
            <Text numberOfLines={1} style={headerStyles.queueType}>
              {contextName}
            </Text>
          )}
          <Text numberOfLines={1} style={headerStyles.queueName}>
            {queueName || 'Nothing playing...'}
          </Text>
        </View>
      )}
    />
  )
})

const headerStyles = StyleSheet.create({
  bar: {
    backgroundColor: 'transparent',
  },
  center: {
    flex: 1,
    justifyContent: 'center',
  },
  queueType: {
    fontFamily: font.regular,
    fontSize: 14,
    color: colors.text.primary,
    textAlign: 'center',
  },
  queueName: {
    fontFamily: font.bold,
    fontSize: 16,
    color: colors.text.primary,
    textAlign: 'center',
  },
})

const SongCoverArt = () => {
  const track = useStore(selectTrackPlayer.currentTrack)

  return (
    <View style={coverArtStyles.container}>
      <CoverArt type="cover" size="original" coverArt={track?.coverArt} style={coverArtStyles.image} />
    </View>
  )
}

const coverArtStyles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    paddingBottom: 10,
    paddingHorizontal: 10,
  },
  image: {
    height: '100%',
    width: '100%',
  },
})

const SongInfo = () => {
  const track = useStore(selectTrackPlayer.currentTrack)
  const id = track?.id || '-1'
  const type = 'song'
  const starred = useStarred(id, type)
  const setStarred = useStore(selectMusic.starItem)

  return (
    <View style={infoStyles.container}>
      <View style={infoStyles.details}>
        <Text numberOfLines={1} style={infoStyles.title}>
          {track?.title}
        </Text>
        <Text numberOfLines={1} style={infoStyles.artist}>
          {track?.artist}
        </Text>
      </View>
      <View style={infoStyles.controls}>
        <PressableOpacity onPress={() => setStarred(id, type, starred)}>
          <Star size={32} starred={starred} />
        </PressableOpacity>
      </View>
    </View>
  )
}

const infoStyles = StyleSheet.create({
  container: {
    width: '100%',
    flexDirection: 'row',
    paddingHorizontal: 10,
  },
  details: {
    flex: 1,
    marginRight: 20,
  },
  controls: {
    justifyContent: 'center',
  },
  title: {
    minHeight: 30,
    fontFamily: font.bold,
    fontSize: 22,
    color: colors.text.primary,
  },
  artist: {
    minHeight: 21,
    fontFamily: font.regular,
    fontSize: 16,
    color: colors.text.secondary,
  },
})

const SeekBar = () => {
  const { position, duration } = useStore(selectTrackPlayer.progress)
  const seekTo = useSeekTo()
  const [value, setValue] = useState(0)
  const [sliding, setSliding] = useState(false)

  useEffect(() => {
    if (sliding) {
      return
    }

    setValue(position)
  }, [position, sliding])

  const onSlidingStart = useCallback(() => {
    setSliding(true)
  }, [])

  const onSlidingComplete = useCallback(
    async (val: number) => {
      await seekTo(val)
      setSliding(false)
    },
    [seekTo],
  )

  return (
    <View style={seekStyles.container}>
      <View style={seekStyles.barContainer}>
        <Slider
          style={seekStyles.slider}
          minimumTrackTintColor="white"
          maximumTrackTintColor={colors.text.secondary}
          thumbTintColor="white"
          maximumValue={duration}
          value={value}
          onSlidingStart={onSlidingStart}
          onSlidingComplete={onSlidingComplete}
        />
      </View>
      <View style={seekStyles.textContainer}>
        <Text style={seekStyles.text}>{formatDuration(value)}</Text>
        <Text style={seekStyles.text}>{formatDuration(duration)}</Text>
      </View>
    </View>
  )
}

const seekStyles = StyleSheet.create({
  container: {
    width: '100%',
    marginTop: 16,
  },
  barContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 0,
  },
  bars: {
    backgroundColor: colors.text.primary,
    height: 4,
  },
  slider: {
    flex: 1,
    height: 40,
  },
  barLeft: {
    marginRight: -6,
  },
  barRight: {
    opacity: 0.3,
    marginLeft: -6,
  },
  indicator: {
    height: 12,
    width: 12,
    borderRadius: 6,
    backgroundColor: colors.text.primary,
    elevation: 1,
  },
  textContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 10,
  },
  text: {
    fontFamily: font.regular,
    fontSize: 15,
    color: colors.text.primary,
  },
})

const PlayerControls = () => {
  const state = useStore(selectTrackPlayer.playerState)
  const play = usePlay()
  const pause = usePause()
  const next = useNext()
  const previous = usePrevious()
  const shuffled = useStore(selectTrackPlayer.shuffled)
  const toggleShuffle = useStore(selectTrackPlayer.toggleShuffle)
  const repeatMode = useStore(selectTrackPlayer.repeatMode)
  const toggleRepeat = useStore(selectTrackPlayer.toggleRepeatMode)
  const navigation = useNavigation()

  let playPauseIcon: string
  let playPauseAction: undefined | (() => void)
  let disabled: boolean

  switch (state) {
    case State.Playing:
      disabled = false
      playPauseIcon = 'pause-circle'
      playPauseAction = pause
      break
    case State.Buffering:
      disabled = false
      playPauseIcon = 'circle'
      playPauseAction = pause
      break
    default:
      disabled = false
      playPauseIcon = 'play-circle'
      playPauseAction = play
      break
  }

  const repeatExtOpacity: TextStyle = {
    opacity: repeatMode === RepeatMode.Track ? 1 : 0,
  }

  return (
    <View style={controlsStyles.container}>
      <View style={controlsStyles.top}>
        <View style={controlsStyles.center}>
          <PressableOpacity onPress={() => toggleRepeat()} disabled={disabled} hitSlop={16}>
            <Icon name="repeat" size={26} color={repeatMode === RepeatMode.Off ? 'white' : colors.accent} />
            <Text style={[controlsStyles.repeatExt, repeatExtOpacity]}>1</Text>
          </PressableOpacity>
        </View>

        <View style={controlsStyles.center}>
          <PressableOpacity onPress={previous} disabled={disabled}>
            <IconFA5 name="step-backward" size={36} color="white" />
          </PressableOpacity>
          <PressableOpacity onPress={playPauseAction} disabled={disabled} style={controlsStyles.play}>
            <IconFA name={playPauseIcon} size={82} color="white" />
            {state === State.Buffering && (
              <ActivityIndicator
                style={controlsStyles.buffering}
                color={colors.gradient.low}
                size="large"
                animating={true}
              />
            )}
          </PressableOpacity>
          <PressableOpacity onPress={next} disabled={disabled}>
            <IconFA5 name="step-forward" size={36} color="white" />
          </PressableOpacity>
        </View>

        <View style={controlsStyles.center}>
          <PressableOpacity onPress={() => toggleShuffle()} disabled={disabled} hitSlop={16}>
            <Icon name="shuffle" size={26} color={shuffled ? colors.accent : 'white'} />
          </PressableOpacity>
        </View>
      </View>
      <View style={controlsStyles.bottom}>
        {/* <PressableOpacity onPress={undefined} disabled={true} hitSlop={16}>
          <IconMatCom name="cast-audio" size={20} color="white" />
        </PressableOpacity> */}
        <PressableOpacity onPress={() => navigation.navigate('queue')} disabled={disabled} hitSlop={16}>
          <IconMatCom name="playlist-play" size={24} color="white" />
        </PressableOpacity>
      </View>
    </View>
  )
}

const controlsStyles = StyleSheet.create({
  container: {
    width: '100%',
    paddingHorizontal: 10,
  },
  top: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingBottom: 8,
  },
  bottom: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    paddingTop: 10,
    paddingBottom: 40,
  },
  play: {
    marginHorizontal: 30,
  },
  center: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  repeatExt: {
    color: colors.accent,
    fontFamily: font.bold,
    fontSize: 14,
    position: 'absolute',
    top: 26,
  },
  buffering: {
    position: 'absolute',
  },
})

type RootStackParamList = {
  top: undefined
  main: undefined
}
type NowPlayingProps = NativeStackScreenProps<RootStackParamList, 'main'>

const NowPlayingView: React.FC<NowPlayingProps> = ({ navigation }) => {
  const track = useStore(selectTrackPlayer.currentTrack)

  useEffect(() => {
    if (!track) {
      navigation.navigate('top')
    }
  })

  const imagePath = typeof track?.artwork === 'string' ? track?.artwork.replace('file://', '') : undefined

  return (
    <View style={styles.container}>
      <ImageGradientBackground imagePath={imagePath} />
      <NowPlayingHeader track={track} />
      <View style={styles.content}>
        <SongCoverArt />
        <SongInfo />
        <SeekBar />
        <PlayerControls />
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
  },
})

export default NowPlayingView
