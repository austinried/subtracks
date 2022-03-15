import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import FilterButton, { OptionData } from '@app/components/FilterButton'
import GradientFlatList from '@app/components/GradientFlatList'
import { useFetchPaginatedList2 } from '@app/hooks/list'
import { Album, AlbumListItem } from '@app/models/music'
import { selectSettings } from '@app/state/settings'
import { Store, useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { GetAlbumList2Type } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import React, { useEffect } from 'react'
import { StyleSheet, Text, useWindowDimensions, View } from 'react-native'

const AlbumItem = React.memo<{
  album: AlbumListItem
  size: number
  height: number
}>(({ album, size, height }) => {
  const navigation = useNavigation()

  return (
    <AlbumContextPressable
      album={album}
      menuStyle={[styles.itemMenu, { width: size }]}
      triggerWrapperStyle={[styles.itemWrapper, { height }]}
      onPress={() => navigation.navigate('album', { id: album.id, title: album.name })}>
      <CoverArt type="cover" coverArt={album.coverArt} style={{ height: size, width: size }} resizeMode={'cover'} />
      <View style={styles.itemDetails}>
        <Text style={styles.title} numberOfLines={1}>
          {album.name}
        </Text>
        <Text style={styles.subtitle} numberOfLines={1}>
          {album.artist}
        </Text>
      </View>
    </AlbumContextPressable>
  )
})

const AlbumListRenderItem: React.FC<{
  item: { album: Album; size: number; height: number }
}> = ({ item }) => <AlbumItem album={item.album} size={item.size} height={item.height} />

const filterOptions: OptionData[] = [
  { text: 'By Name', value: 'alphabeticalByName' },
  { text: 'By Artist', value: 'alphabeticalByArtist' },
  { text: 'Newest', value: 'newest' },
  { text: 'Frequent', value: 'frequent' },
  { text: 'Recent', value: 'recent' },
  { text: 'Starred', value: 'starred' },
  { text: 'Random', value: 'random' },
  // { text: 'By Year...', value: 'byYear' },
  // { text: 'By Genre...', value: 'byGenre' },
]

const selectAlbumList = (store: Store) => {
  return Object.values(store.entities.albumsList)
    .flat()
    .map(id => store.entities.albums[id])
}

const AlbumsList = () => {
  const list = useStore(selectAlbumList)

  const fetchAlbumsNextPage = useStore(store => store.fetchLibraryAlbumsNextPage)
  const resetAlbumsList = useStore(store => store.resetLibraryAlbumsList)
  const { refreshing, refresh, fetchNextPage } = useFetchPaginatedList2(fetchAlbumsNextPage, resetAlbumsList)

  const filter = useStore(selectSettings.libraryAlbumFilter)
  const setFilter = useStore(selectSettings.setLibraryAlbumFilter)

  const layout = useWindowDimensions()

  const size = layout.width / 3 - styles.itemWrapper.marginHorizontal * 2
  const height = size + 36

  useEffect(() => {
    refresh()
  }, [refresh, filter])

  return (
    <View style={styles.container}>
      <GradientFlatList
        data={list.map(album => ({ album, size, height }))}
        renderItem={AlbumListRenderItem}
        keyExtractor={item => item.album.id}
        numColumns={3}
        removeClippedSubviews={true}
        refreshing={refreshing}
        onRefresh={refresh}
        overScrollMode="never"
        onEndReached={fetchNextPage}
        onEndReachedThreshold={6}
        windowSize={5}
      />
      <FilterButton
        data={filterOptions}
        value={filter.type}
        onSelect={selection => {
          setFilter({
            ...filter,
            type: selection as GetAlbumList2Type,
          })
        }}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  itemMenu: {
    flex: 1 / 3,
  },
  itemWrapper: {
    marginVertical: 4,
    marginHorizontal: 2,
  },
  itemDetails: {
    flex: 1,
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

export default React.memo(AlbumsList)
