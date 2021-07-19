import CoverArt from '@app/components/CoverArt'
import GradientFlatList from '@app/components/GradientFlatList'
import PressableOpacity from '@app/components/PressableOpacity'
import { PlaylistListItem } from '@app/models/music'
import { playlistsAtom, playlistsUpdatingAtom, useUpdatePlaylists } from '@app/state/music'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { StyleSheet, Text, View } from 'react-native'

const PlaylistItem = React.memo<{ item: PlaylistListItem }>(({ item }) => {
  const navigation = useNavigation()

  return (
    <PressableOpacity
      style={styles.item}
      onPress={() => navigation.navigate('PlaylistView', { id: item.id, title: item.name })}>
      <CoverArt coverArtUri={item.coverArtThumbUri} style={styles.art} />
      <View style={styles.text}>
        <Text style={styles.title} numberOfLines={1}>
          {item.name}
        </Text>
        {item.comment ? (
          <Text style={styles.subtitle} numberOfLines={1}>
            {item.comment}
          </Text>
        ) : (
          <></>
        )}
      </View>
    </PressableOpacity>
  )
})

const PlaylistRenderItem: React.FC<{ item: PlaylistListItem }> = ({ item }) => <PlaylistItem item={item} />

const PlaylistsList = () => {
  const playlists = useAtomValue(playlistsAtom)
  const updating = useAtomValue(playlistsUpdatingAtom)
  const updatePlaylists = useUpdatePlaylists()

  useEffect(() => {
    if (playlists.length === 0) {
      updatePlaylists()
    }
  })

  return (
    <GradientFlatList
      contentContainerStyle={styles.listContent}
      data={playlists}
      renderItem={PlaylistRenderItem}
      keyExtractor={item => item.id}
      onRefresh={updatePlaylists}
      refreshing={updating}
      overScrollMode="never"
    />
  )
}

const styles = StyleSheet.create({
  listContent: {
    minHeight: '100%',
  },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
    marginVertical: 6,
    marginLeft: 10,
  },
  text: {
    marginLeft: 10,
  },
  title: {
    fontFamily: font.semiBold,
    fontSize: 16,
    color: colors.text.primary,
  },
  subtitle: {
    fontFamily: font.regular,
    fontSize: 14,
    color: colors.text.secondary,
  },
  art: {
    height: 70,
    width: 70,
  },
})

export default PlaylistsList
