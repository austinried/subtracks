import { Song } from '@app/models/music'
import CoverArt from '@app/components/CoverArt'
import React from 'react'
import { StyleSheet, ImageResizeMode, FlatList } from 'react-native'

export function genPlaylistCover2x2Component(_songs: Song[]) {
  const component: React.FC<{style: any, resizeMode: ImageResizeMode}> = ({style}) => {
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
  return React.memo(component);
}

const styles = StyleSheet.create({
  imgGrid: {
    paddingRight: 10
  },
})