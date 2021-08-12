import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import ListItem from '@app/components/ListItem'
import PressableOpacity from '@app/components/PressableOpacity'
import { useArtistInfo } from '@app/hooks/music'
import { useSetQueue } from '@app/hooks/trackplayer'
import { Album, Song } from '@app/models/music'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { useLayout } from '@react-native-community/hooks'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback } from 'react'
import { ActivityIndicator, StatusBar, StyleSheet, Text, View } from 'react-native'
import FastImage from 'react-native-fast-image'
import Animated, { useAnimatedScrollHandler, useAnimatedStyle, useSharedValue } from 'react-native-reanimated'
import IconMat from 'react-native-vector-icons/MaterialIcons'

const AlbumItem = React.memo<{
  album: Album
  height: number
  width: number
}>(({ album, height, width }) => {
  const navigation = useNavigation()

  if (height <= 0 || width <= 0) {
    return <></>
  }

  return (
    <AlbumContextPressable
      album={album}
      onPress={() => navigation.navigate('album', { id: album.id, title: album.name })}
      menuStyle={[styles.albumItem, { width }]}
      triggerOuterWrapperStyle={{ width }}>
      <CoverArt coverArt={album.coverArt} style={{ height, width }} resizeMode={FastImage.resizeMode.cover} />
      <Text style={styles.albumTitle}>{album.name}</Text>
      <Text style={styles.albumYear}> {album.year ? album.year : ''}</Text>
    </AlbumContextPressable>
  )
})

const TopSongs = React.memo<{
  songs: Song[]
  name: string
  artistId: string
}>(({ songs, name, artistId }) => {
  const setQueue = useSetQueue()

  return (
    <>
      <Header>Top Songs</Header>
      {songs.map((s, i) => (
        <ListItem
          key={i}
          item={s}
          contextId={artistId}
          queueId={i}
          showArt={true}
          subtitle={s.album}
          onPress={() => setQueue(songs, name, 'artist', artistId, i)}
        />
      ))}
    </>
  )
})

const ArtistDetailsFallback = React.memo(() => (
  <GradientBackground style={styles.fallback}>
    <ActivityIndicator size="large" color={colors.accent} />
  </GradientBackground>
))

const NowPlayingHeader = React.memo<{ title: string; animatedOpacity: { opacity: number } }>(
  ({ title, animatedOpacity }) => {
    const navigation = useNavigation()

    const back = useCallback(() => {
      navigation.goBack()
    }, [navigation])

    return (
      <Animated.View style={[headerStyles.container, animatedOpacity]}>
        <PressableOpacity onPress={back} style={headerStyles.icons} ripple={true}>
          <IconMat name="arrow-back" color="white" size={25} />
        </PressableOpacity>
        <View style={headerStyles.center}>
          <Text numberOfLines={1} style={headerStyles.queueName}>
            {title}
          </Text>
        </View>
        {/* <PressableOpacity style={headerStyles.icons} ripple={true}>
          <IconMat name="more-vert" color="white" size={25} />
        </PressableOpacity> */}
      </Animated.View>
    )
  },
)

const headerStyles = StyleSheet.create({
  container: {
    height: dimensions.top(),
    paddingTop: StatusBar.currentHeight,
    backgroundColor: colors.gradient.high,
    width: '100%',
    position: 'absolute',
    zIndex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  icons: {
    height: 42,
    width: 42,
    marginHorizontal: 8,
  },
  center: {
    flex: 1,
  },
  queueName: {
    fontFamily: font.semiBold,
    fontSize: 18,
    color: colors.text.primary,
    flex: 1,
    textAlignVertical: 'center',
    paddingLeft: 14,
  },
})

const ArtistView = React.memo<{ id: string; title: string }>(({ id, title }) => {
  const artist = useArtistInfo(id)
  const albumsLayout = useLayout()
  const coverLayout = useLayout()
  const headerOpacity = useSharedValue(0)

  const onScroll = useAnimatedScrollHandler({
    onScroll: event => {
      headerOpacity.value = Math.max(0, event.contentOffset.y - 70) / (artistCoverHeight - (70 + dimensions.header))
    },
  })

  const animatedOpacity = useAnimatedStyle(() => {
    return {
      opacity: headerOpacity.value,
    }
  })

  const albumSize = albumsLayout.width / 2 - styles.container.paddingHorizontal / 2

  if (!artist) {
    return <ArtistDetailsFallback />
  }

  const _albums = [...artist.albums]
    .sort((a, b) => a.name.localeCompare(b.name))
    .sort((a, b) => (b.year || 0) - (a.year || 0))

  return (
    <View style={{ flex: 1 }}>
      <NowPlayingHeader title={title} animatedOpacity={animatedOpacity} />
      <GradientScrollView
        onLayout={coverLayout.onLayout}
        offset={artistCoverHeight}
        style={styles.scroll}
        contentContainerStyle={styles.scrollContent}
        onScroll={onScroll}>
        <CoverArt
          artistId={artist.id}
          style={styles.artistCover}
          resizeMode={FastImage.resizeMode.cover}
          round={false}
          imageSize="original"
        />
        <View style={styles.titleContainer}>
          <Text style={styles.title}>{artist.name}</Text>
        </View>
        <View style={styles.container}>
          {artist.topSongs.length > 0 ? (
            <TopSongs songs={artist.topSongs} name={artist.name} artistId={artist.id} />
          ) : (
            <></>
          )}
          <Header>Albums</Header>
          <View style={styles.albums} onLayout={albumsLayout.onLayout}>
            {_albums.map(a => (
              <AlbumItem key={a.id} album={a} height={albumSize} width={albumSize} />
            ))}
          </View>
        </View>
      </GradientScrollView>
    </View>
  )
})

const artistCoverHeight = 350

const styles = StyleSheet.create({
  headerText: {
    flex: 1,
    textAlign: 'center',
    textAlignVertical: 'center',
    fontFamily: font.semiBold,
    fontSize: 22,
    color: colors.text.primary,
  },
  scroll: {
    flex: 1,
  },
  fallback: {
    alignItems: 'center',
    paddingTop: 100,
  },
  scrollContent: {
    alignItems: 'center',
  },
  container: {
    width: '100%',
    paddingHorizontal: 20,
  },
  titleContainer: {
    width: '100%',
    height: artistCoverHeight,
    justifyContent: 'flex-end',
  },
  title: {
    fontFamily: font.bold,
    fontSize: 44,
    color: colors.text.primary,
    textAlign: 'center',
    textShadowColor: 'black',
    textShadowOffset: { width: 0, height: 0 },
    textShadowRadius: 16,
    paddingHorizontal: 10,
    marginBottom: 10,
  },
  artistCover: {
    position: 'absolute',
    height: artistCoverHeight,
    width: '100%',
  },
  albums: {
    width: '100%',
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
  },
  albumItem: {
    marginBottom: 20,
  },
  albumTitle: {
    fontFamily: font.semiBold,
    fontSize: 14,
    color: colors.text.primary,
    marginTop: 4,
    textAlign: 'center',
  },
  albumYear: {
    color: colors.text.secondary,
    fontFamily: font.regular,
    textAlign: 'center',
  },
})

export default ArtistView
