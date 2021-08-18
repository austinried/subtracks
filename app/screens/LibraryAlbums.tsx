import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import GradientFlatList from '@app/components/GradientFlatList'
import { useFetchPaginatedList } from '@app/hooks/list'
import { Album, AlbumListItem } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import React from 'react'
import { StyleSheet, Text, useWindowDimensions, View } from 'react-native'
import FastImage from 'react-native-fast-image'

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
      <CoverArt
        type="cover"
        coverArt={album.coverArt}
        style={{ height: size, width: size }}
        resizeMode={FastImage.resizeMode.cover}
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
})

const AlbumListRenderItem: React.FC<{
  item: { album: Album; size: number; height: number }
}> = ({ item }) => <AlbumItem album={item.album} size={item.size} height={item.height} />

const AlbumsList = () => {
  const fetchAlbums = useStore(selectMusic.fetchAlbums)
  const { list, refreshing, refresh, fetchNextPage } = useFetchPaginatedList(fetchAlbums, 60)

  const layout = useWindowDimensions()

  const size = layout.width / 3 - styles.itemWrapper.marginHorizontal * 2
  const height = size + 36

  return (
    <View style={styles.container}>
      <GradientFlatList
        contentContainerStyle={styles.listContent}
        data={list.map(album => ({ album, size, height }))}
        renderItem={AlbumListRenderItem}
        keyExtractor={item => item.album.id}
        numColumns={3}
        removeClippedSubviews={true}
        refreshing={refreshing}
        onRefresh={refresh}
        overScrollMode="never"
        onEndReached={fetchNextPage}
        onEndReachedThreshold={1}
        getItemLayout={(_data, index) => ({
          length: height,
          offset: height * Math.floor(index / 3),
          index,
        })}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  listContent: {
    minHeight: '100%',
  },
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
