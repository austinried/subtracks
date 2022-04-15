import { AlbumContextPressable } from '@app/components/ContextMenu'
import CoverArt from '@app/components/CoverArt'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import NothingHere from '@app/components/NothingHere'
import { withSuspenseMemo } from '@app/components/withSuspense'
import { useQueryHomeLists } from '@app/hooks/query'
import { Album } from '@app/models/library'
import { useStoreDeep } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { GetAlbumList2TypeBase } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import equal from 'fast-deep-equal/es6/react'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

const AlbumItem = React.memo<{
  album: Album
}>(({ album }) => {
  const navigation = useNavigation()

  return (
    <AlbumContextPressable
      album={album}
      triggerWrapperStyle={styles.item}
      onPress={() => navigation.navigate('album', { id: album.id, title: album.name, album })}>
      <CoverArt
        type="cover"
        coverArt={album.coverArt}
        style={{ height: styles.item.width, width: styles.item.width }}
        resizeMode="cover"
        size="thumbnail"
      />
      <Text style={styles.title} numberOfLines={1}>
        {album.name}
      </Text>
      <Text style={styles.subtitle} numberOfLines={1}>
        {album.artist}
      </Text>
    </AlbumContextPressable>
  )
}, equal)

const CategoryHeader = withSuspenseMemo<{ type: string }>(({ type }) => {
  const { t } = useTranslation('resources.album.lists')
  console.log('type', type, t(type))
  return <Header style={styles.header}>{t(type)}</Header>
})

const Category = React.memo<{
  type: string
  albums: Album[]
}>(({ type, albums }) => {
  const Albums = () => (
    <ScrollView
      horizontal={true}
      showsHorizontalScrollIndicator={false}
      overScrollMode={'never'}
      style={styles.artScroll}
      contentContainerStyle={styles.artScrollContent}>
      {albums.map(a => (
        <AlbumItem key={a.id} album={a} />
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
      <CategoryHeader type={type} />
      {albums.length > 0 ? <Albums /> : <Nothing />}
    </View>
  )
}, equal)

const Home = () => {
  const types = useStoreDeep(store => store.settings.screens.home.listTypes)
  const listQueries = useQueryHomeLists(types as GetAlbumList2TypeBase[], 20)
  const paddingTop = useSafeAreaInsets().top

  return (
    <GradientScrollView
      style={styles.scroll}
      contentContainerStyle={{ paddingTop }}
      refreshControl={
        <RefreshControl
          refreshing={listQueries.some(q => q.isLoading)}
          onRefresh={() => listQueries.forEach(q => q.refetch())}
          colors={[colors.accent, colors.accentLow]}
          progressViewOffset={paddingTop}
        />
      }>
      <View style={styles.content}>
        {types.map(type => {
          const query = listQueries.find(list => list.data?.type === type)
          return <Category key={type} type={type} albums={query?.data?.albums || []} />
        })}
      </View>
    </GradientScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
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
