import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import NothingHere from '@app/components/NothingHere'
import { useActiveListRefresh2 } from '@app/hooks/server'
import { AlbumListItem } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { selectSettings } from '@app/state/settings'
import { useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { GetAlbumListType } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback } from 'react'
import { RefreshControl, ScrollView, StatusBar, StyleSheet, Text, View } from 'react-native'
import FastImage from 'react-native-fast-image'

const titles: { [key in GetAlbumListType]?: string } = {
  recent: 'Recent Albums',
  random: 'Random Albums',
  frequent: 'Frequent Albums',
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
        resizeMode={FastImage.resizeMode.cover}
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
  name?: string
  data: AlbumListItem[]
}>(({ name, data }) => {
  const Albums = () => (
    <ScrollView
      horizontal={true}
      showsHorizontalScrollIndicator={false}
      overScrollMode={'never'}
      style={styles.artScroll}
      contentContainerStyle={styles.artScrollContent}>
      {data.map(album => (
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
      <Header style={styles.header}>{name}</Header>
      {data.length > 0 ? <Albums /> : <Nothing />}
    </View>
  )
})

const Home = () => {
  const types = useStore(selectSettings.homeLists)
  const lists = useStore(selectMusic.homeLists)
  const updating = useStore(selectMusic.homeListsUpdating)
  const update = useStore(selectMusic.fetchHomeLists)
  const clear = useStore(selectMusic.clearHomeLists)

  useActiveListRefresh2(
    useCallback(() => {
      clear()
      update()
    }, [clear, update]),
  )

  return (
    <GradientScrollView
      style={styles.scroll}
      contentContainerStyle={styles.scrollContentContainer}
      refreshControl={
        <RefreshControl
          refreshing={updating}
          onRefresh={update}
          colors={[colors.accent, colors.accentLow]}
          progressViewOffset={StatusBar.currentHeight}
        />
      }>
      <View style={styles.content}>
        {types.map(type => (
          <Category key={type} name={titles[type as GetAlbumListType]} data={type in lists ? lists[type] : []} />
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
