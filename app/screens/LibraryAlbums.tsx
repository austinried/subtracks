import CoverArt from '@app/components/CoverArt'
import GradientFlatList from '@app/components/GradientFlatList'
import PressableOpacity from '@app/components/PressableOpacity'
import { Album } from '@app/models/music'
import { albumLists } from '@app/state/music'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { StyleSheet, Text, useWindowDimensions, View } from 'react-native'

const AlbumItem = React.memo<{
  id: string
  name: string
  size: number
  height: number
  artist?: string
  coverArtUri?: string
}>(({ id, name, artist, size, height, coverArtUri }) => {
  const navigation = useNavigation()

  return (
    <PressableOpacity
      style={[styles.item, { maxWidth: size, height }]}
      onPress={() => navigation.navigate('AlbumView', { id, title: name })}>
      <CoverArt coverArtUri={coverArtUri} height={size} width={size} />
      <View style={styles.itemDetails}>
        <Text style={styles.title} numberOfLines={1}>
          {name}
        </Text>
        <Text style={styles.subtitle} numberOfLines={1}>
          {artist}
        </Text>
      </View>
    </PressableOpacity>
  )
})

const AlbumListRenderItem: React.FC<{
  item: { album: Album; size: number; height: number }
}> = ({ item }) => (
  <AlbumItem
    id={item.album.id}
    coverArtUri={item.album.coverArtThumbUri}
    name={item.album.name}
    artist={item.album.artist}
    size={item.size}
    height={item.height}
  />
)

const AlbumsList = () => {
  const state = albumLists.alphabeticalByArtist
  const list = useAtomValue(state.listAtom)
  const updating = useAtomValue(state.updatingAtom)
  const updateList = state.useUpdateList()

  const layout = useWindowDimensions()

  const size = layout.width / 3 - styles.item.marginHorizontal * 2
  const height = size + 44

  const albumsList = list.map(album => ({ album, size, height }))

  useEffect(() => {
    if (albumsList.length === 0) {
      updateList()
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
        onRefresh={updateList}
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
