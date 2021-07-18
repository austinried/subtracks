import { Song } from '@app/models/music'
import { currentTrackAtom } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useAtomValue } from 'jotai/utils'
import React from 'react'
import { GestureResponderEvent, StyleSheet, Text, View } from 'react-native'
import IconFA from 'react-native-vector-icons/FontAwesome'
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

  subtitle = subtitle || 'artist'

  return (
    <View style={styles.container}>
      <PressableOpacity onPress={onPress} style={styles.item}>
        {showArt ? <CoverArt coverArtUri={song.coverArtThumbUri} style={styles.art} /> : <></>}
        <View style={styles.text}>
          <Text style={[styles.title, { color: currentTrack?.id === song.id ? colors.accent : colors.text.primary }]}>
            {song.title}
          </Text>
          <Text style={styles.subtitle}>{song[subtitle]}</Text>
        </View>
      </PressableOpacity>
      <View style={styles.controls}>
        <PressableOpacity onPress={undefined}>
          <IconFA name="star-o" size={26} color={colors.text.primary} />
        </PressableOpacity>
        <PressableOpacity onPress={undefined} style={styles.more}>
          <IconMat name="more-vert" size={32} color="white" />
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
  title: {
    fontSize: 16,
    fontFamily: font.semiBold,
  },
  subtitle: {
    fontSize: 14,
    fontFamily: font.regular,
    color: colors.text.secondary,
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    marginLeft: 10,
  },
  more: {
    marginLeft: 8,
  },
})

export default React.memo(SongItem)
