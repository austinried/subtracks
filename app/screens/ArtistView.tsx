import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import HeaderBar from '@app/components/HeaderBar'
import ListItem from '@app/components/ListItem'
import { Album, Song } from '@app/models/library'
import { useStore, useStoreDeep } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { mapById } from '@app/util/state'
import { useLayout } from '@react-native-community/hooks'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback, useEffect } from 'react'
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native'
import { useAnimatedScrollHandler, useAnimatedStyle, useSharedValue } from 'react-native-reanimated'

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
      <CoverArt type="cover" coverArt={album.coverArt} style={{ height, width }} resizeMode={'cover'} />
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
  const setQueue = useStore(selectTrackPlayer.setQueue)

  return (
    <>
      <Header>Top Songs</Header>
      {songs.slice(0, 5).map((s, i) => (
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

const ArtistAlbums = React.memo<{
  albums: Album[]
}>(({ albums }) => {
  const albumsLayout = useLayout()

  const sortedAlbums = [...albums]
    .sort((a, b) => a.name.localeCompare(b.name))
    .sort((a, b) => (b.year || 0) - (a.year || 0))

  const albumSize = albumsLayout.width / 2 - styles.contentContainer.paddingHorizontal / 2

  return (
    <>
      <Header>Albums</Header>
      <View style={styles.albums} onLayout={albumsLayout.onLayout}>
        {sortedAlbums.map(a => (
          <AlbumItem key={a.id} album={a} height={albumSize} width={albumSize} />
        ))}
      </View>
    </>
  )
})

const ArtistViewFallback = React.memo(() => (
  <GradientBackground style={styles.fallback}>
    <ActivityIndicator size="large" color={colors.accent} />
  </GradientBackground>
))

const ArtistView = React.memo<{ id: string; title: string }>(({ id, title }) => {
  const artist = useStoreDeep(useCallback(store => store.entities.artists[id], [id]))
  const topSongIds = useStoreDeep(useCallback(store => store.entities.artistNameTopSongs[artist?.name], [artist?.name]))
  const topSongs = useStoreDeep(
    useCallback(store => (topSongIds ? mapById(store.entities.songs, topSongIds) : undefined), [topSongIds]),
  )
  const albumIds = useStoreDeep(useCallback(store => store.entities.artistAlbums[id], [id]))
  const albums = useStoreDeep(
    useCallback(store => (albumIds ? mapById(store.entities.albums, albumIds) : undefined), [albumIds]),
  )

  const fetchArtist = useStore(store => store.fetchLibraryArtist)
  const fetchTopSongs = useStore(store => store.fetchLibraryArtistTopSongs)

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

  useEffect(() => {
    if (!artist || !albumIds) {
      fetchArtist(id)
    }
  }, [artist, albumIds, fetchArtist, id])

  useEffect(() => {
    if (artist && !topSongIds) {
      fetchTopSongs(artist.name)
    }
  }, [artist, fetchTopSongs, topSongIds])

  if (!artist) {
    return <ArtistViewFallback />
  }

  return (
    <View style={styles.container}>
      <HeaderBar title={title} headerStyle={[styles.header, animatedOpacity]} />
      <GradientScrollView
        onLayout={coverLayout.onLayout}
        offset={artistCoverHeight}
        style={styles.scroll}
        contentContainerStyle={styles.scrollContent}
        onScroll={onScroll}>
        <CoverArt type="artist" artistId={artist.id} style={styles.artistCover} resizeMode={'cover'} />
        <View style={styles.titleContainer}>
          <Text style={styles.title}>{artist.name}</Text>
        </View>
        <View style={styles.contentContainer}>
          {topSongs && albums ? (
            topSongs.length > 0 ? (
              <>
                <TopSongs songs={topSongs} name={artist.name} artistId={artist.id} />
                <ArtistAlbums albums={albums} />
              </>
            ) : (
              <ArtistAlbums albums={albums} />
            )
          ) : (
            <ActivityIndicator size="large" color={colors.accent} style={styles.loading} />
          )}
        </View>
      </GradientScrollView>
    </View>
  )
})

const artistCoverHeight = 350

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    position: 'absolute',
    zIndex: 1,
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
  contentContainer: {
    minHeight: artistCoverHeight * 2,
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
  loading: {
    marginTop: 30,
  },
})

export default ArtistView
