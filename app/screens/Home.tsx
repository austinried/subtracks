import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import NothingHere from '@app/components/NothingHere'
import { useFetchPaginatedList } from '@app/hooks/list'
import { useActiveServerRefresh } from '@app/hooks/server'
import { AlbumListItem } from '@app/models/music'
import { Album, mapById } from '@app/state/library'
import { selectMusic } from '@app/state/music'
import { selectSettings } from '@app/state/settings'
import { Store, useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { GetAlbumList2Params, GetAlbumList2TypeBase, GetAlbumListType } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import produce from 'immer'
import React, { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { RefreshControl, ScrollView, StatusBar, StyleSheet, Text, View } from 'react-native'
import create from 'zustand'

const titles: { [key in GetAlbumListType]?: string } = {
  recent: 'Recently Played',
  random: 'Random Albums',
  frequent: 'Frequently Played',
  starred: 'Starred Albums',
}

const AlbumItem = React.memo<{
  album: AlbumListItem
}>(({ album }) => {
  const navigation = useNavigation()

  return (
    <AlbumContextPressable
      album={album}
      triggerWrapperStyle={styles.item}
      onPress={() => navigation.navigate('album', { id: album.id, title: album.name })}>
      <CoverArt
        type="cover"
        coverArt={album.coverArt}
        style={{ height: styles.item.width, width: styles.item.width }}
        resizeMode={'cover'}
      />
      <Text style={styles.title} numberOfLines={1}>
        {album.name}
      </Text>
      <Text style={styles.subtitle} numberOfLines={1}>
        {album.artist}
      </Text>
    </AlbumContextPressable>
  )
})

const Category = React.memo<{
  type: string
}>(({ type }) => {
  const list = useHomeStore(useCallback(store => store.lists[type] || [], [type]))
  const albums = useStore(useCallback(store => mapById(store.entities.albums, list), [list]))

  const Albums = () => (
    <ScrollView
      horizontal={true}
      showsHorizontalScrollIndicator={false}
      overScrollMode={'never'}
      style={styles.artScroll}
      contentContainerStyle={styles.artScrollContent}>
      {albums.map(album => (
        <AlbumItem key={album.id} album={album} />
      ))}
    </ScrollView>
  )

  const Nothing = () => (
    <View style={styles.nothingHereContent}>
      <NothingHere height={160} width={160} />
    </View>
  )

  return (
    <View style={styles.category}>
      <Header style={styles.header}>{titles[type as GetAlbumListType] || ''}</Header>
      {albums.length > 0 ? <Albums /> : <Nothing />}
    </View>
  )
})

interface HomeState {
  lists: { [type: string]: string[] }
  setList: (type: string, list: string[]) => void
}

const useHomeStore = create<HomeState>((set, get) => ({
  lists: {},

  setList: (type, list) => {
    set(
      produce<HomeState>(state => {
        state.lists[type] = list
      }),
    )
  },
}))

const Home = () => {
  const [refreshing, setRefreshing] = useState(false)
  const types = useStore(selectSettings.homeLists)
  const fetchAlbumList = useStore(store => store.fetchLibraryAlbumList)
  const setList = useHomeStore(store => store.setList)

  const refresh = useCallback(async () => {
    setRefreshing(true)

    await Promise.all(
      types.map(async type => {
        console.log('fetch', type)
        const ids = await fetchAlbumList({ type: type as GetAlbumList2TypeBase, size: 20, offset: 0 })
        console.log('set', type)
        setList(type, ids)
      }),
    )

    setRefreshing(false)
  }, [fetchAlbumList, setList, types])

  useActiveServerRefresh(
    useCallback(() => {
      types.forEach(type => setList(type, []))
      refresh()
    }, [refresh, setList, types]),
  )

  return (
    <GradientScrollView
      style={styles.scroll}
      contentContainerStyle={styles.scrollContentContainer}
      refreshControl={
        <RefreshControl
          refreshing={refreshing}
          onRefresh={refresh}
          colors={[colors.accent, colors.accentLow]}
          progressViewOffset={StatusBar.currentHeight}
        />
      }>
      <View style={styles.content}>
        {types.map(type => (
          <Category key={type} type={type} />
        ))}
      </View>
    </GradientScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
  },
  scrollContentContainer: {
    paddingTop: StatusBar.currentHeight,
  },
  content: {
    paddingBottom: 20,
  },
  header: {
    paddingHorizontal: 20,
  },
  category: {
    // marginTop: 12,
  },
  nothingHereContent: {
    width: '100%',
    height: 190,
    justifyContent: 'center',
    alignItems: 'center',
  },
  artScroll: {
    height: 190,
  },
  artScrollContent: {
    paddingLeft: 20,
  },
  item: {
    flex: 1,
    marginRight: 10,
    width: 150,
  },
  title: {
    fontFamily: font.semiBold,
    fontSize: 13,
    color: colors.text.primary,
    marginTop: 4,
  },
  subtitle: {
    fontFamily: font.regular,
    fontSize: 12,
    color: colors.text.secondary,
  },
})

export default Home
