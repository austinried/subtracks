import CoverArt from '@app/components/CoverArt'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import PressableOpacity from '@app/components/PressableOpacity'
import ListItem from '@app/components/ListItem'
import { Album } from '@app/models/music'
import { artistInfoAtomFamily } from '@app/state/music'
import { useSetQueue } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useLayout } from '@react-native-community/hooks'
import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { StyleSheet, Text, View } from 'react-native'
import FastImage from 'react-native-fast-image'

const AlbumItem = React.memo<{
  album: Album
  height: number
  width: number
}>(({ album, height, width }) => {
  const navigation = useNavigation()

  return (
    <PressableOpacity
      onPress={() => navigation.navigate('AlbumView', { id: album.id, title: album.name })}
      style={[styles.albumItem, { width }]}>
      <CoverArt coverArt={album.coverArt} style={{ height, width }} />
      <Text style={styles.albumTitle}>{album.name}</Text>
      <Text style={styles.albumYear}> {album.year ? album.year : ''}</Text>
    </PressableOpacity>
  )
})

const ArtistDetails: React.FC<{ id: string }> = ({ id }) => {
  const artist = useAtomValue(artistInfoAtomFamily(id))
  const setQueue = useSetQueue()
  const albumsLayout = useLayout()
  const coverLayout = useLayout()

  const albumSize = albumsLayout.width / 2 - styles.container.paddingHorizontal / 2

  if (!artist) {
    return <></>
  }

  const TopSongs = () => (
    <>
      <Header>Top Songs</Header>
      {artist.topSongs.map((s, i) => (
        <ListItem
          key={i}
          item={s}
          showArt={true}
          subtitle={s.album}
          onPress={() => setQueue(artist.topSongs, `Top Songs: ${artist.name}`, i)}
        />
      ))}
    </>
  )

  return (
    <GradientScrollView
      onLayout={coverLayout.onLayout}
      offset={artistCoverHeight}
      style={styles.scroll}
      contentContainerStyle={styles.scrollContent}>
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
        {artist.topSongs.length > 0 ? <TopSongs /> : <></>}
        <Header>Albums</Header>
        <View style={styles.albums} onLayout={albumsLayout.onLayout}>
          {artist.albums.map(a => (
            <AlbumItem key={a.id} album={a} height={albumSize} width={albumSize} />
          ))}
        </View>
      </View>
    </GradientScrollView>
  )
}

const ArtistView: React.FC<{
  id: string
  title: string
}> = ({ id, title }) => {
  const navigation = useNavigation()

  useEffect(() => {
    navigation.setOptions({ title })
  })

  return (
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <ArtistDetails id={id} />
    </React.Suspense>
  )
}

const artistCoverHeight = 280

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
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

export default React.memo(ArtistView)
