import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import FilterButton, { OptionData } from '@app/components/FilterButton'
import GradientFlatList from '@app/components/GradientFlatList'
import { useFetchPaginatedList } from '@app/hooks/list'
import { useStore, useStoreDeep } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { GetAlbumList2Params, GetAlbumList2Type } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback } from 'react'
import { StyleSheet, Text, useWindowDimensions, View } from 'react-native'

const AlbumItem = React.memo<{
  id: string
  size: number
  height: number
}>(({ id, size, height }) => {
  const album = useStoreDeep(useCallback(store => store.library.albums[id], [id]))
  const navigation = useNavigation()

  if (!album) {
    return <></>
  }

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
  item: { id: string; size: number; height: number }
}> = ({ item }) => <AlbumItem id={item.id} size={item.size} height={item.height} />

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

const AlbumsList = () => {
  const filter = useStoreDeep(store => store.settings.screens.library.albumsFilter)
  const setFilter = useStore(store => store.setLibraryAlbumFilter)

  const fetchAlbumList = useStore(store => store.fetchAlbumList)
  const fetchPage = useCallback(
    (size: number, offset: number) => {
      let params: GetAlbumList2Params
      switch (filter.type) {
        case 'byYear':
          params = {
            size,
            offset,
            type: filter.type,
            fromYear: filter.fromYear,
            toYear: filter.toYear,
          }
          break
        case 'byGenre':
          params = {
            size,
            offset,
            type: filter.type,
            genre: filter.genre,
          }
          break
        default:
          params = {
            size,
            offset,
            type: filter.type,
          }
          break
      }
      return fetchAlbumList(params)
    },
    [fetchAlbumList, filter.fromYear, filter.genre, filter.toYear, filter.type],
  )

  const { list, refreshing, refresh, fetchNextPage } = useFetchPaginatedList(fetchPage, 300)

  const layout = useWindowDimensions()

  const size = layout.width / 3 - styles.itemWrapper.marginHorizontal * 2
  const height = size + 36

  return (
    <View style={styles.container}>
      <GradientFlatList
        data={list.map(id => ({ id, size, height }))}
        renderItem={AlbumListRenderItem}
        keyExtractor={item => item.id}
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
