import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { Pressable, StyleSheet, Text, useWindowDimensions, View } from 'react-native'
import { Album } from '@app/models/music'
import { albumsAtom, albumsUpdatingAtom, useUpdateAlbums } from '@app/state/music'
import font from '@app/styles/font'
import AlbumArt from '@app/components/AlbumArt'
import GradientFlatList from '@app/components/GradientFlatList'
import colors from '@app/styles/colors'

const AlbumItem: React.FC<{
  id: string
  name: string
  size: number
  height: number
  artist?: string
}> = ({ id, name, artist, size, height }) => {
  const navigation = useNavigation()

  return (
    <Pressable
      style={[styles.item, { maxWidth: size, height }]}
      onPress={() => navigation.navigate('AlbumView', { id, title: name })}>
      <AlbumArt id={id} height={size} width={size} />
      <View style={styles.itemDetails}>
        <Text style={styles.title} numberOfLines={1}>
          {name}
        </Text>
        <Text style={styles.subtitle} numberOfLines={1}>
          {artist}
        </Text>
      </View>
    </Pressable>
  )
}
const MemoAlbumItem = React.memo(AlbumItem)

const AlbumListRenderItem: React.FC<{
  item: { album: Album; size: number; height: number }
}> = ({ item }) => (
  <MemoAlbumItem
    id={item.album.id}
    name={item.album.name}
    artist={item.album.artist}
    size={item.size}
    height={item.height}
  />
)

const AlbumsList = () => {
  const albums = useAtomValue(albumsAtom)
  const updating = useAtomValue(albumsUpdatingAtom)
  const updateAlbums = useUpdateAlbums()
  const layout = useWindowDimensions()

  const size = layout.width / 3 - styles.item.marginHorizontal * 2
  const height = size + 44

  const albumsList = Object.values(albums).map(album => ({ album, size, height }))

  useEffect(() => {
    if (albumsList.length === 0) {
      updateAlbums()
    }
  })

  return (
    <View style={styles.container}>
      <GradientFlatList
        data={albumsList}
        renderItem={AlbumListRenderItem}
        keyExtractor={item => item.album.id}
        numColumns={3}
        removeClippedSubviews={true}
        refreshing={updating}
        onRefresh={updateAlbums}
        overScrollMode="never"
        getItemLayout={(_data, index) => ({
          length: height,
          offset: height * Math.floor(index / 3),
          index,
        })}
      />
    </View>
  )
}

const AlbumsTab = () => (
  <React.Suspense fallback={<Text>Loading...</Text>}>
    <AlbumsList />
  </React.Suspense>
)

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  item: {
    alignItems: 'center',
    marginVertical: 4,
    marginHorizontal: 2,
    flex: 1 / 3,
    // backgroundColor: 'green',
  },
  art: {
    // height: 125,
  },
  itemDetails: {
    flex: 1,
    width: '100%',
    // width: 125,
  },
  title: {
    fontSize: 12,
    fontFamily: font.semiBold,
    color: colors.text.primary,
    marginTop: 4,
  },
  subtitle: {
    fontSize: 11,
    fontFamily: font.regular,
    color: colors.text.secondary,
  },
})

export default React.memo(AlbumsTab)
