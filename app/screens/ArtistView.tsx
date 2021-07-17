import CoverArt from '@app/components/CoverArt'
import GradientScrollView from '@app/components/GradientScrollView'
import PressableOpacity from '@app/components/PressableOpacity'
import SongItem from '@app/components/SongItem'
import { Album } from '@app/models/music'
import { artistInfoAtomFamily } from '@app/state/music'
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
      <CoverArt coverArtUri={album.coverArtThumbUri} height={height} width={width} />
      <Text style={styles.albumTitle}>{album.name}</Text>
      <Text style={styles.albumYear}> {album.year ? album.year : ''}</Text>
    </PressableOpacity>
  )
})

const ArtistDetails: React.FC<{ id: string }> = ({ id }) => {
  const artist = useAtomValue(artistInfoAtomFamily(id))
  const layout = useLayout()

  const size = layout.width / 2 - styles.container.paddingHorizontal / 2

  if (!artist) {
    return <></>
  }

  return (
    <GradientScrollView offset={artistImageHeight} style={styles.scroll} contentContainerStyle={styles.scrollContent}>
      <FastImage
        style={[styles.artistImage]}
        source={{ uri: artist.largeImageUrl }}
        resizeMode={FastImage.resizeMode.cover}
      />
      <View style={styles.titleContainer}>
        <Text style={styles.title}>{artist.name}</Text>
      </View>
      <View style={styles.container}>
        <Text style={styles.header}>Top Songs</Text>
        {artist.topSongs.map(s => (
          <SongItem key={s.id} song={s} showArt={true} subtitle="album" />
        ))}
        <Text style={styles.header}>Albums</Text>
        <View style={styles.albums} onLayout={layout.onLayout}>
          {artist.albums.map(a => (
            <AlbumItem key={a.id} album={a} height={size} width={size} />
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

const artistImageHeight = 280

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
    height: artistImageHeight,
    justifyContent: 'flex-end',
  },
  title: {
    fontFamily: font.bold,
    fontSize: 44,
    color: colors.text.primary,
    textAlign: 'left',
    textShadowColor: 'black',
    textShadowOffset: { width: 0, height: 0 },
    textShadowRadius: 16,
    paddingHorizontal: 10,
  },
  header: {
    fontFamily: font.bold,
    fontSize: 24,
    color: colors.text.primary,
    marginTop: 20,
    marginBottom: 14,
  },
  artistImage: {
    position: 'absolute',
    width: '100%',
    height: artistImageHeight,
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
