import { useIsPlaying } from '@app/hooks/trackplayer'
import { Album, Artist, ListableItem, Song } from '@app/models/library'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback } from 'react'
import { StyleProp, StyleSheet, Text, View, ViewStyle } from 'react-native'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'
import IconMat from 'react-native-vector-icons/MaterialIcons'
import { AlbumContextPressable, ArtistContextPressable, SongContextPressable } from './ContextMenu'
import CoverArt from './CoverArt'
import PressableOpacity from './PressableOpacity'
import { PressableStar } from './Star'
import equal from 'fast-deep-equal/es6/react'

const TitleTextSong = React.memo<{
  contextId?: string
  queueId: number
  title?: string
}>(({ contextId, queueId, title }) => {
  const playing = useIsPlaying(contextId, queueId)

  return (
    <View style={styles.textLine}>
      <Text style={[styles.title, { color: playing ? colors.accent : colors.text.primary }]}>
        {playing && (
          <View style={styles.playingIcon}>
            <IconFA5 name="play" size={9} color={colors.accent} />
          </View>
        )}
        {title}
      </Text>
    </View>
  )
})

const TitleText = React.memo<{
  title?: string
}>(({ title }) => {
  return (
    <View style={styles.textLine}>
      <Text style={styles.title}>{title}</Text>
    </View>
  )
})

const ListItem: React.FC<{
  item: ListableItem
  contextId?: string
  queueId?: number
  onPress?: () => void
  showArt?: boolean
  showStar?: boolean
  listStyle?: 'big' | 'small'
  subtitle?: string
  style?: StyleProp<ViewStyle>
  disabled?: boolean
}> = ({ item, contextId, queueId, onPress, showArt, showStar, subtitle, listStyle, style, disabled }) => {
  const navigation = useNavigation()

  showStar = showStar === undefined ? true : showStar
  listStyle = listStyle || 'small'

  const sizeStyle = listStyle === 'big' ? bigStyles : smallStyles

  if (!onPress) {
    switch (item.itemType) {
      case 'album':
        onPress = () => navigation.navigate('album', { id: item.id, title: item.name, album: item })
        break
      case 'artist':
        onPress = () => navigation.navigate('artist', { id: item.id, title: item.name })
        break
      case 'playlist':
        onPress = () => navigation.navigate('playlist', { id: item.id, title: item.name, playlist: item })
        break
    }
  }

  if (!subtitle) {
    switch (item.itemType) {
      case 'song':
      case 'album':
        subtitle = item.artist
        break
      case 'playlist':
        subtitle = item.comment
        break
    }
  }

  const itemPressable = useCallback(
    ({ children }) => (
      <PressableOpacity onPress={onPress} style={styles.item} disabled={disabled}>
        {children}
      </PressableOpacity>
    ),
    [disabled, onPress],
  )
  const albumPressable = useCallback(
    ({ children }) => (
      <AlbumContextPressable
        album={item as Album}
        onPress={onPress}
        triggerWrapperStyle={styles.item}
        disabled={disabled}>
        {children}
      </AlbumContextPressable>
    ),
    [disabled, item, onPress],
  )
  const songPressable = useCallback(
    ({ children }) => (
      <SongContextPressable song={item as Song} onPress={onPress} triggerWrapperStyle={styles.item} disabled={disabled}>
        {children}
      </SongContextPressable>
    ),
    [disabled, item, onPress],
  )
  const artistPressable = useCallback(
    ({ children }) => (
      <ArtistContextPressable
        artist={item as Artist}
        onPress={onPress}
        triggerWrapperStyle={styles.item}
        disabled={disabled}>
        {children}
      </ArtistContextPressable>
    ),
    [disabled, item, onPress],
  )

  let PressableComponent = itemPressable
  if (item.itemType === 'album') {
    PressableComponent = albumPressable
  } else if (item.itemType === 'song') {
    PressableComponent = songPressable
  } else if (item.itemType === 'artist') {
    PressableComponent = artistPressable
  }

  let title = <></>
  if (item.itemType === 'song' && queueId !== undefined) {
    title = <TitleTextSong contextId={contextId} queueId={queueId} title={item.title} />
  } else if (item.itemType !== 'song') {
    title = <TitleText title={item.name} />
  }

  const artStyle = { ...styles.art, ...sizeStyle.art }
  const resizeMode = 'cover'
  let coverArt = <></>
  if (item.itemType === 'artist') {
    coverArt = (
      <CoverArt
        type="artist"
        artistId={item.id}
        round={true}
        style={artStyle}
        resizeMode={resizeMode}
        size="thumbnail"
      />
    )
  } else if (item.itemType === 'song') {
    coverArt = (
      <CoverArt type="album" albumId={item.albumId} style={artStyle} resizeMode={resizeMode} size="thumbnail" />
    )
  } else {
    coverArt = (
      <CoverArt type="cover" coverArt={item.coverArt} style={artStyle} resizeMode={resizeMode} size="thumbnail" />
    )
  }

  return (
    <View style={[styles.container, sizeStyle.container, style]}>
      <PressableComponent>
        {showArt && coverArt}
        <View style={styles.text}>
          {title}
          {subtitle !== undefined && (
            <View style={styles.textLine}>
              {false && (
                <IconMat
                  name="file-download-done"
                  size={17}
                  color={colors.text.secondary}
                  style={styles.downloadedIcon}
                />
              )}
              <Text style={styles.subtitle}>{subtitle}</Text>
            </View>
          )}
        </View>
      </PressableComponent>
      <View style={styles.controls}>
        {showStar && item.itemType !== 'playlist' && (
          <PressableStar id={item.id} type={item.itemType} size={26} style={styles.controlItem} disabled={disabled} />
        )}
      </View>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 14,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  item: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
  },
  art: {
    marginRight: 10,
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
    color: colors.text.primary,
  },
  playingIcon: {
    paddingRight: 4,
    paddingBottom: 1,
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
  },
  controlItem: {
    marginLeft: 16,
  },
})

const smallStyles = StyleSheet.create({
  container: {
    minHeight: 50,
  },
  art: {
    height: 50,
    width: 50,
  },
})

const bigStyles = StyleSheet.create({
  container: {
    minHeight: 70,
  },
  art: {
    height: 70,
    width: 70,
  },
})

export default React.memo(ListItem, equal)
