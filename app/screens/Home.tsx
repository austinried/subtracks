import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import NothingHere from '@app/components/NothingHere'
import { useActiveServerRefresh } from '@app/hooks/settings'
import { useStore, useStoreDeep } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { GetAlbumList2TypeBase, GetAlbumListType } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import equal from 'fast-deep-equal/es6/react'
import produce from 'immer'
import React, { useCallback, useState } from 'react'
import { RefreshControl, ScrollView, StatusBar, StyleSheet, Text, View } from 'react-native'
import create, { StateSelector } from 'zustand'

const titles: { [key in GetAlbumListType]?: string } = {
  recent: 'Recently Played',
  random: 'Random Albums',
  frequent: 'Frequently Played',
  starred: 'Starred Albums',
}

const AlbumItem = React.memo<{
  id: string
}>(({ id }) => {
  const navigation = useNavigation()
  const album = useStoreDeep(useCallback(store => store.library.albums[id], [id]))

  if (!album) {
    return <></>
  }

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
  const list = useHomeStoreDeep(useCallback(store => store.lists[type] || [], [type]))

  const Albums = () => (
    <ScrollView
      horizontal={true}
      showsHorizontalScrollIndicator={false}
      overScrollMode={'never'}
      style={styles.artScroll}
      contentContainerStyle={styles.artScrollContent}>
      {list.map(id => (
        <AlbumItem key={id} id={id} />
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
      {list.length > 0 ? <Albums /> : <Nothing />}
    </View>
  )
})

interface HomeState {
  lists: { [type: string]: string[] }
  setList: (type: string, list: string[]) => void
}

const useHomeStore = create<HomeState>(set => ({
  lists: {},

  setList: (type, list) => {
    set(
      produce<HomeState>(state => {
        state.lists[type] = list
      }),
    )
  },
}))

function useHomeStoreDeep<U>(stateSelector: StateSelector<HomeState, U>) {
  return useHomeStore(stateSelector, equal)
}

const Home = () => {
  const [refreshing, setRefreshing] = useState(false)
  const types = useStoreDeep(store => store.settings.screens.home.listTypes)
  const fetchAlbumList = useStore(store => store.fetchAlbumList)
  const setList = useHomeStore(store => store.setList)

  const refresh = useCallback(async () => {
    setRefreshing(true)

    await Promise.all(
      types.map(async type => {
        const ids = await fetchAlbumList({ type: type as GetAlbumList2TypeBase, size: 20, offset: 0 })
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
