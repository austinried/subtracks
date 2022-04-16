import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import FilterButton from '@app/components/FilterButton'
import GradientFlatList from '@app/components/GradientFlatList'
import { withSuspenseMemo } from '@app/components/withSuspense'
import { useQueryAlbumList } from '@app/hooks/query'
import { Album } from '@app/models/library'
import { useStore, useStoreDeep } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { GetAlbumList2TypeBase } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import equal from 'fast-deep-equal/es6/react'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { StyleSheet, Text, useWindowDimensions, View } from 'react-native'

const AlbumItem = React.memo<{
  album: Album
  size: number
  height: number
}>(({ album, size, height }) => {
  const navigation = useNavigation()

  if (!album) {
    return <></>
  }

  return (
    <AlbumContextPressable
      album={album}
      menuStyle={[styles.itemMenu, { width: size }]}
      triggerWrapperStyle={[styles.itemWrapper, { height }]}
      onPress={() => navigation.navigate('album', { id: album.id, title: album.name, album })}>
      <CoverArt
        type="cover"
        coverArt={album.coverArt}
        style={{ height: size, width: size }}
        resizeMode="cover"
        size="thumbnail"
      />
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
}, equal)

const AlbumListRenderItem: React.FC<{
  item: { album: Album; size: number; height: number }
}> = ({ item }) => <AlbumItem album={item.album} size={item.size} height={item.height} />

const filterValues: GetAlbumList2TypeBase[] = [
  'alphabeticalByName', //
  'alphabeticalByArtist',
  'newest',
  'frequent',
  'recent',
  'starred',
  'random',
]

const AlbumFilterButton = withSuspenseMemo(() => {
  const { t } = useTranslation()
  const filterType = useStoreDeep(store => store.settings.screens.library.albumsFilter.type)
  const setFilterType = useStore(store => store.setLibraryAlbumFilterType)

  return (
    <FilterButton
      data={filterValues.map(value => ({ value, text: t(`resources.album.lists.${value}`) }))}
      value={filterType}
      onSelect={selection => {
        setFilterType(selection as GetAlbumList2TypeBase)
      }}
      title={t('resources.album.lists.sort')}
    />
  )
})

const AlbumsList = () => {
  const filterType = useStoreDeep(store => store.settings.screens.library.albumsFilter.type)
  const { isLoading, data, fetchNextPage, refetch } = useQueryAlbumList(filterType as GetAlbumList2TypeBase, 300)

  const layout = useWindowDimensions()

  const size = layout.width / 3 - styles.itemWrapper.marginHorizontal * 2
  const height = size + 36

  return (
    <View style={styles.container}>
      <GradientFlatList
        data={data ? data.pages.flatMap(albums => albums.map(album => ({ album, size, height }))) : []}
        renderItem={AlbumListRenderItem}
        keyExtractor={item => item.album.id}
        numColumns={3}
        removeClippedSubviews={true}
        refreshing={isLoading}
        onRefresh={refetch}
        overScrollMode="never"
        onEndReached={() => fetchNextPage()}
        onEndReachedThreshold={6}
        windowSize={5}
      />
      <AlbumFilterButton />
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
