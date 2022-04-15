import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import HeaderBar from '@app/components/HeaderBar'
import ListItem from '@app/components/ListItem'
import { withSuspenseMemo } from '@app/components/withSuspense'
import { useQueryArtist, useQueryArtistTopSongs } from '@app/hooks/query'
import { useSetQueue } from '@app/hooks/trackplayer'
import { Album, Song } from '@app/models/library'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { useLayout } from '@react-native-community/hooks'
import { useNavigation } from '@react-navigation/native'
import equal from 'fast-deep-equal/es6/react'
import React from 'react'
import { useTranslation } from 'react-i18next'
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
      onPress={() => navigation.navigate('album', { id: album.id, title: album.name, album })}
      menuStyle={[styles.albumItem, { width }]}
      triggerOuterWrapperStyle={{ width }}>
      <CoverArt type="cover" coverArt={album.coverArt} style={{ height, width }} resizeMode="cover" size="thumbnail" />
      <Text style={styles.albumTitle}>{album.name}</Text>
      <Text style={styles.albumYear}> {album.year ? album.year : ''}</Text>
    </AlbumContextPressable>
  )
}, equal)

const TopSongs = withSuspenseMemo<{
  songs: Song[]
  name: string
}>(
  ({ songs, name }) => {
    const { setQueue, isReady, contextId } = useSetQueue('artist', songs)
    const { t } = useTranslation('resources.song.lists')

    return (
      <>
        <Header>{t('artistTopSongs')}</Header>
        {songs.slice(0, 5).map((s, i) => (
          <ListItem
            key={i}
            item={s}
            contextId={contextId}
            queueId={i}
            showArt={true}
            subtitle={s.album}
            onPress={() => setQueue({ title: name, playTrack: i })}
            disabled={!isReady}
          />
        ))}
      </>
    )
  },
  null,
  equal,
)

const ArtistAlbums = withSuspenseMemo<{
  albums: Album[]
}>(
  ({ albums }) => {
    const albumsLayout = useLayout()
    const { t } = useTranslation('resources.album')

    const sortedAlbums = [...albums]
      .sort((a, b) => a.name.localeCompare(b.name))
      .sort((a, b) => (b.year || 0) - (a.year || 0))

    const albumSize = albumsLayout.width / 2 - styles.contentContainer.paddingHorizontal / 2

    return (
      <>
        <Header>{t('name', { count: 2 })}</Header>
        <View style={styles.albums} onLayout={albumsLayout.onLayout}>
          {sortedAlbums.map(a => (
            <AlbumItem key={a.id} album={a} height={albumSize} width={albumSize} />
          ))}
        </View>
      </>
    )
  },
  null,
  equal,
)

const ArtistViewFallback = React.memo(() => (
  <GradientBackground style={styles.fallback}>
    <ActivityIndicator size="large" color={colors.accent} />
  </GradientBackground>
))

const ArtistView = React.memo<{ id: string; title: string }>(({ id, title }) => {
  const { data: artistData } = useQueryArtist(id)
  const { data: topSongs, isError } = useQueryArtistTopSongs(artistData?.artist?.name)

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

  if (!artistData) {
    return <ArtistViewFallback />
  }

  const { artist, albums } = artistData

  return (
    <View style={styles.container}>
      <HeaderBar title={title} headerStyle={[styles.header, animatedOpacity]} />
      <GradientScrollView
        onLayout={coverLayout.onLayout}
        offset={artistCoverHeight}
        style={styles.scroll}
        contentContainerStyle={styles.scrollContent}
        onScroll={onScroll}>
        <CoverArt type="artist" size="original" artistId={artist.id} style={styles.artistCover} resizeMode="cover" />
        <View style={styles.titleContainer}>
          <Text style={styles.title}>{artist.name}</Text>
        </View>
        <View style={styles.contentContainer}>
          {(topSongs || isError) && artist ? (
            topSongs && topSongs.length > 0 ? (
              <>
                <TopSongs songs={topSongs} name={artist.name} />
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
