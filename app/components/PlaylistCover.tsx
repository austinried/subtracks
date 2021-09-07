import { usePlaylistWithSongs } from '@app/hooks/music'
import { PlaylistListItem, Song } from '@app/models/music'
import CoverArt from '@app/components/CoverArt'
import React, { useMemo } from 'react'
import { StyleSheet, ImageResizeMode, FlatList } from 'react-native'

export function usePlaylistCover2x2(_songs: Song[]) {
  const generator: React.FC<{style: any, resizeMode: ImageResizeMode}> = ({style}) => {
    const nestedStyle = { ...style, ...{ width: style.width / 2, height: style.height / 2, marginRight: 0 } }
    // we need to wrap the songs in a custom object because the list will ignore null values
    const songs = [...Array(4)].map((_, i) => ({s: i < _songs.length ? _songs[i] : null}))
    return (
      <FlatList
        data={songs}
        numColumns={2}
        style={styles.imgGrid}
        keyExtractor={(_, idx) => idx.toString()}
        renderItem={(item) => <CoverArt type="cover" coverArt={item.item.s?.id} style={nestedStyle} resizeMode='stretch' />}
      />
    )
  }
  return generator;
}

export function usePlaylistCover2x2Fetch(item: PlaylistListItem) {
  const playlist = usePlaylistWithSongs(item.id)
  return useMemo(() => playlist == null ? null : usePlaylistCover2x2(playlist.songs), [playlist]);
}
const styles = StyleSheet.create({
  imgGrid: {
    paddingRight: 10
  },
})