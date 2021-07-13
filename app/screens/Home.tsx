import CoverArt from '@app/components/CoverArt'
import GradientScrollView from '@app/components/GradientScrollView'
import PressableOpacity from '@app/components/PressableOpacity'
import { AlbumListItem } from '@app/models/music'
import { albumLists } from '@app/state/music'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React from 'react'
import { ScrollView, StatusBar, StyleSheet, Text, View } from 'react-native'

const AlbumItem = React.memo<{
  album: AlbumListItem
}>(({ album }) => {
  const navigation = useNavigation()

  return (
    <PressableOpacity
      onPress={() => navigation.navigate('AlbumView', { id: album.id, title: album.name })}
      key={album.id}
      style={styles.item}>
      <CoverArt
        PlaceholderComponent={() => <></>}
        coverArtUri={album.coverArtThumbUri}
        height={styles.item.width}
        width={styles.item.width}
      />
      <Text style={styles.title} numberOfLines={1}>
        {album.name}
      </Text>
      <Text style={styles.subtitle} numberOfLines={1}>
        {album.artist}
      </Text>
    </PressableOpacity>
  )
})

const Category = React.memo<{
  name: string
  type: string
}>(({ name, type }) => {
  const state = albumLists[type]
  const list = useAtomValue(state.listAtom)
  const updating = useAtomValue(state.updatingAtom)
  const updateList = state.useUpdateList()

  return (
    <View style={[styles.category, { backgroundColor: updating ? 'transparent' : 'transparent' }]}>
      <PressableOpacity onPress={updateList} style={{ alignItems: 'flex-start' }}>
        <Text style={styles.header}>{name}</Text>
      </PressableOpacity>
      <ScrollView
        horizontal={true}
        showsHorizontalScrollIndicator={false}
        overScrollMode={'never'}
        style={styles.artScroll}
        contentContainerStyle={styles.artScrollContent}>
        {list.map(album => (
          <AlbumItem key={album.id} album={album} />
        ))}
      </ScrollView>
    </View>
  )
})

const Home = () => (
  <GradientScrollView style={styles.scroll} contentContainerStyle={styles.scrollContentContainer}>
    <View style={styles.content}>
      <Category name="Random Albums" type="random" />
      <Category name="Newest Albums" type="newest" />
      <Category name="Recent Albums" type="recent" />
      <Category name="Frequent Albums" type="frequent" />
      <Category name="Starred Albums" type="starred" />
    </View>
  </GradientScrollView>
)

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
  category: {
    marginTop: 12,
  },
  header: {
    fontFamily: font.bold,
    fontSize: 24,
    color: colors.text.primary,
    paddingHorizontal: 20,
    marginTop: 4,
  },
  artScroll: {
    marginTop: 10,
    height: 190,
  },
  artScrollContent: {
    paddingLeft: 20,
  },
  item: {
    marginRight: 10,
    width: 150,
    alignItems: 'flex-start',
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
