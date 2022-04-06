import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import HeaderBar from '@app/components/HeaderBar'
import ImageGradientFlatList from '@app/components/ImageGradientFlatList'
import ListItem from '@app/components/ListItem'
import ListPlayerControls from '@app/components/ListPlayerControls'
import NothingHere from '@app/components/NothingHere'
import { useQueryAlbum, useQueryCoverArtPath, useQueryPlaylist } from '@app/hooks/query'
import { useSetQueue } from '@app/hooks/trackplayer'
import { Album, Playlist, Song } from '@app/models/library'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import equal from 'fast-deep-equal/es6/react'
import React, { useState } from 'react'
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native'

type SongListType = 'album' | 'playlist'

const SongListDetailsFallback = React.memo(() => (
  <GradientBackground style={styles.fallback}>
    <ActivityIndicator size="large" color={colors.accent} />
  </GradientBackground>
))

const SongRenderItem: React.FC<{
  item: {
    song: Song
    contextId?: string
    queueId?: number
    subtitle?: string
    onPress?: () => void
    showArt?: boolean
  }
}> = ({ item }) => (
  <ListItem
    item={item.song}
    contextId={item.contextId}
    queueId={item.queueId}
    subtitle={item.subtitle}
    onPress={item.onPress}
    showArt={item.showArt}
    style={styles.listItem}
  />
)

const SongListDetails = React.memo<{
  title: string
  type: SongListType
  songList?: Album | Playlist
  songs?: Song[]
  subtitle?: string
}>(({ title, songList, songs, subtitle, type }) => {
  const { data: coverArtPath } = useQueryCoverArtPath(songList?.coverArt, 'thumbnail')
  const [headerColor, setHeaderColor] = useState<string | undefined>(undefined)

  const _songs = [...(songs || [])]
  let typeName = ''

  if (type === 'album') {
    typeName = 'Album'
    if (_songs.some(s => s.track === undefined)) {
      _songs.sort((a, b) => a.title.localeCompare(b.title))
    } else {
      _songs.sort((a, b) => {
        const aVal = (a.track as number) + (a.discNumber !== undefined ? a.discNumber * 10000 : 0)
        const bVal = (b.track as number) + (b.discNumber !== undefined ? b.discNumber * 10000 : 0)
        return aVal - bVal
      })
    }
  } else {
    typeName = 'Playlist'
  }

  const { setQueue, isReady } = useSetQueue(_songs)

  if (!songList) {
    return <SongListDetailsFallback />
  }

  return (
    <View style={styles.container}>
      <HeaderBar
        headerStyle={{ backgroundColor: headerColor }}
        title={title}
        contextItem={songList.itemType === 'album' ? songList : undefined}
      />
      <ImageGradientFlatList
        data={_songs.map((s, i) => ({
          song: s,
          contextId: songList.id,
          queueId: i,
          subtitle: s.artist,
          onPress: () => setQueue(songList.name, type, songList.id, i),
          showArt: songList.itemType === 'playlist',
        }))}
        renderItem={SongRenderItem}
        keyExtractor={(item, i) => i.toString()}
        backgroundProps={{
          imagePath: coverArtPath,
          style: styles.container,
          onGetColor: setHeaderColor,
        }}
        overScrollMode="never"
        windowSize={7}
        contentMarginTop={26}
        ListEmptyComponent={
          songs ? (
            <NothingHere style={styles.nothing} />
          ) : (
            <ActivityIndicator size="large" color={colors.accent} style={styles.listLoading} />
          )
        }
        ListHeaderComponent={
          <View style={styles.content}>
            <CoverArt type="cover" size="original" coverArt={songList.coverArt} style={styles.cover} />
            <Text style={styles.title}>{songList.name}</Text>
            {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : <></>}
            <ListPlayerControls
              style={styles.controls}
              songs={_songs}
              typeName={typeName}
              queueName={songList.name}
              queueContextId={songList.id}
              queueContextType={type}
            />
          </View>
        }
      />
    </View>
  )
}, equal)

const PlaylistView = React.memo<{
  id: string
  title: string
  playlist?: Playlist
}>(({ id, title, playlist }) => {
  const query = useQueryPlaylist(id, playlist)

  return (
    <SongListDetails
      title={title}
      songList={query.data?.playlist}
      songs={query.data?.songs}
      subtitle={query.data?.playlist?.comment}
      type="playlist"
    />
  )
}, equal)

const AlbumView = React.memo<{
  id: string
  title: string
  album?: Album
}>(({ id, title, album }) => {
  const query = useQueryAlbum(id, album)

  return (
    <SongListDetails
      title={title}
      songList={query.data?.album}
      songs={query.data?.songs}
      subtitle={(query.data?.album?.artist || '') + (query.data?.album?.year ? ' • ' + query.data?.album?.year : '')}
      type="album"
    />
  )
}, equal)

const SongListView = React.memo<{
  id: string
  title: string
  type: SongListType
  album?: Album
  playlist?: Playlist
}>(({ id, title, type, album, playlist }) => {
  return type === 'album' ? (
    <AlbumView id={id} title={title} album={album} />
  ) : (
    <PlaylistView id={id} title={title} playlist={playlist} />
  )
}, equal)

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    alignItems: 'center',
    paddingTop: 10,
    paddingHorizontal: 20,
  },
  controls: {
    marginTop: 20,
  },
  title: {
    fontSize: 24,
    fontFamily: font.bold,
    color: colors.text.primary,
    marginTop: 20,
    textAlign: 'center',
  },
  subtitle: {
    fontFamily: font.regular,
    color: colors.text.secondary,
    fontSize: 14,
    marginTop: 4,
    textAlign: 'center',
  },
  cover: {
    height: 240,
    width: 240,
  },
  songs: {
    marginTop: 26,
    marginBottom: 30,
    width: '100%',
  },
  fallback: {
    alignItems: 'center',
    paddingTop: 100,
  },
  listItem: {
    paddingHorizontal: 20,
  },
  nothing: {
    width: '100%',
  },
  listLoading: {
    marginTop: 10,
  },
})

export default SongListView
