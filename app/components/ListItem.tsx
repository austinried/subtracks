import { useStarred } from '@app/hooks/music'
import { useIsPlaying } from '@app/hooks/trackplayer'
import { AlbumListItem, Artist, ListableItem, Song } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback } from 'react'
import { ImageResizeMode, StyleProp, StyleSheet, Text, View, ViewStyle } from 'react-native'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'
import IconMat from 'react-native-vector-icons/MaterialIcons'
import { AlbumContextPressable, ArtistContextPressable, SongContextPressable } from './ContextMenu'
import CoverArt from './CoverArt'
import PressableOpacity from './PressableOpacity'
import Star from './Star'

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
  CustomArt?: React.FC<{style: any, resizeMode: ImageResizeMode}> | null
}> = ({ item, contextId, queueId, onPress, showArt, showStar, subtitle, listStyle, style, CustomArt }) => {
  const navigation = useNavigation()
  const starred = useStarred(item.id, item.itemType)

  showStar = showStar === undefined ? true : showStar
  listStyle = listStyle || 'small'

  const sizeStyle = listStyle === 'big' ? bigStyles : smallStyles

  if (!onPress) {
    switch (item.itemType) {
      case 'album':
        onPress = () => navigation.navigate('album', { id: item.id, title: item.name })
        break
      case 'artist':
        onPress = () => navigation.navigate('artist', { id: item.id, title: item.name })
        break
      case 'playlist':
        onPress = () => navigation.navigate('playlist', { id: item.id, title: item.name })
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
      <PressableOpacity onPress={onPress} style={styles.item}>
        {children}
      </PressableOpacity>
    ),
    [onPress],
  )
  const albumPressable = useCallback(
    ({ children }) => (
      <AlbumContextPressable album={item as AlbumListItem} onPress={onPress} triggerWrapperStyle={styles.item}>
        {children}
      </AlbumContextPressable>
    ),
    [item, onPress],
  )
  const songPressable = useCallback(
    ({ children }) => (
      <SongContextPressable song={item as Song} onPress={onPress} triggerWrapperStyle={styles.item}>
        {children}
      </SongContextPressable>
    ),
    [item, onPress],
  )
  const artistPressable = useCallback(
    ({ children }) => (
      <ArtistContextPressable artist={item as Artist} onPress={onPress} triggerWrapperStyle={styles.item}>
        {children}
      </ArtistContextPressable>
    ),
    [item, onPress],
  )

  let PressableComponent = itemPressable
  if (item.itemType === 'album') {
    PressableComponent = albumPressable
  } else if (item.itemType === 'song') {
    PressableComponent = songPressable
  } else if (item.itemType === 'artist') {
    PressableComponent = artistPressable
  }

  const starItem = useStore(selectMusic.starItem)
  const toggleStarred = useCallback(() => {
    if (item.itemType !== 'playlist') {
      starItem(item.id, item.itemType, starred)
    }
  }, [item.id, item.itemType, starItem, starred])

  let title = <></>
  if (item.itemType === 'song' && queueId !== undefined) {
    title = <TitleTextSong contextId={contextId} queueId={queueId} title={item.title} />
  } else if (item.itemType !== 'song') {
    title = <TitleText title={item.name} />
  }

  const artStyle = { ...styles.art, ...sizeStyle.art }
  const resizeMode = 'cover'
  let coverArt = <></>
  if(CustomArt != null) {
    coverArt = <CustomArt style={artStyle} resizeMode={resizeMode} />;
  }
  else {
    if (item.itemType === 'artist') {
      coverArt = <CoverArt type="artist" artistId={item.id} round={true} style={artStyle} resizeMode={resizeMode} />
    } else {
      coverArt = <CoverArt type="cover" coverArt={item.coverArt} style={artStyle} resizeMode={resizeMode} />
    }
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
        {showStar && (
          <PressableOpacity onPress={toggleStarred} style={styles.controlItem}>
            <Star size={26} starred={starred} />
          </PressableOpacity>
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

export default React.memo(ListItem)
