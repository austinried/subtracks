import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { genPlaylistCover2x2Component } from '@app/components/PlaylistCover'
import { useFetchList } from '@app/hooks/list'
import { PlaylistListItem, PlaylistWithSongs } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import React, { useMemo } from 'react'
import { ImageResizeMode, StyleSheet } from 'react-native'

const PlaylistRenderItem: React.FC<{ item: PlaylistListItem, getPlaylistData: (id: string) => PlaylistWithSongs | null }> = ({ item, getPlaylistData }) => {
  let CustomArt: React.FC<{style: any, resizeMode: ImageResizeMode}> | null = null
  if (item.coverArt == null) {
    // some server types don't support cover art for playlists
    // in this case we manually generate a 2x2 grid of album covers
    // we only do so if the playlist data already exists because
    // fetching it here will bottleneck the entire app
    const playlist = getPlaylistData(item.id)
    // this use of hooks is technically invalid because it is in a branch
    // but the branch is constant for the entire lifetime of the app
    CustomArt = useMemo(() => playlist != null ? genPlaylistCover2x2Component(playlist.songs): null, [playlist])
  }
  return <ListItem item={item} showArt={true} showStar={false} listStyle="big" style={styles.listItem} CustomArt={CustomArt} />
}

const PlaylistsList = () => {
  const fetchPlaylists = useStore(selectMusic.fetchPlaylists)
  const { list, refreshing, refresh } = useFetchList(fetchPlaylists)

  const playlistFetch = useStore(store => (id: string) => id in store.playlistsWithSongs ? store.playlistsWithSongs[id] : null)

  return (
    <GradientFlatList
      data={list}
      renderItem={item => <PlaylistRenderItem item={item.item} getPlaylistData={playlistFetch} />}
      keyExtractor={item => item.id}
      onRefresh={refresh}
      refreshing={refreshing}
      overScrollMode="never"
      windowSize={5}
      contentMarginTop={6}
    />
  )
}

const styles = StyleSheet.create({
  listItem: {
    paddingHorizontal: 10,
  },
})

export default PlaylistsList
