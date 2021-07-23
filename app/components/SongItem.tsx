import { Song } from '@app/models/music'
import { currentTrackAtom } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useAtomValue } from 'jotai/utils'
import React, { useState } from 'react'
import { GestureResponderEvent, StyleSheet, Text, View } from 'react-native'
import IconFA from 'react-native-vector-icons/FontAwesome'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'
import IconMat from 'react-native-vector-icons/MaterialIcons'
import CoverArt from './CoverArt'
import PressableOpacity from './PressableOpacity'

const SongItem: React.FC<{
  song: Song
  onPress?: (event: GestureResponderEvent) => void
  showArt?: boolean
  subtitle?: 'artist' | 'album'
}> = ({ song, onPress, showArt, subtitle }) => {
  const currentTrack = useAtomValue(currentTrackAtom)
  const [starred, setStarred] = useState(false)

  subtitle = subtitle || 'artist'
  const playing = currentTrack?.id === song.id

  return (
    <View style={styles.container}>
      <PressableOpacity onPress={onPress} style={styles.item}>
        {showArt ? <CoverArt coverArtUri={song.coverArtThumbUri} style={styles.art} /> : <></>}
        <View style={styles.text}>
          <View style={styles.textLine}>
            {playing ? <IconFA5 name="play" size={10} color={colors.accent} style={styles.playingIcon} /> : <></>}
            <Text style={[styles.title, { color: playing ? colors.accent : colors.text.primary }]}>{song.title}</Text>
          </View>
          <View style={styles.textLine}>
            {starred ? (
              <IconMat
                name="file-download-done"
                size={17}
                color={colors.text.secondary}
                style={styles.downloadedIcon}
              />
            ) : (
              <></>
            )}
            <Text style={styles.subtitle}>{song[subtitle]}</Text>
          </View>
        </View>
      </PressableOpacity>
      <View style={styles.controls}>
        <PressableOpacity onPress={() => setStarred(!starred)}>
          {starred ? (
            <IconFA name="star" size={26} color={colors.accent} />
          ) : (
            <IconFA name="star-o" size={26} color={colors.text.secondary} />
          )}
        </PressableOpacity>
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 14,
    minHeight: 50,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  item: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'flex-start',
  },
  art: {
    marginRight: 10,
    height: 50,
    width: 50,
  },
  text: {
    flex: 1,
  },
  textLine: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  title: {
    fontSize: 16,
    fontFamily: font.semiBold,
  },
  playingIcon: {
    marginRight: 5,
    marginLeft: 1,
  },
  downloadedIcon: {
    marginRight: 2,
    marginLeft: -3,
  },
  subtitle: {
    fontSize: 14,
    fontFamily: font.regular,
    color: colors.text.secondary,
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    marginLeft: 16,
  },
  more: {
    marginLeft: 8,
  },
})

export default React.memo(SongItem)
