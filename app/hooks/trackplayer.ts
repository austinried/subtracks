import { Song } from '@app/models/library'
import { QueueContextType, TrackExt } from '@app/models/trackplayer'
import { useStore, useStoreDeep } from '@app/state/store'
import { getQueue, trackPlayerCommands } from '@app/state/trackplayer'
import userAgent from '@app/util/userAgent'
import uniq from 'lodash.uniq'
import TrackPlayer from 'react-native-track-player'
import { useQueries } from 'react-query'
import { useFetchFile } from './fetch'
import qk from './queryKeys'
import zipObject from 'lodash.zipobject'

export const usePlay = () => {
  return () => trackPlayerCommands.enqueue(() => TrackPlayer.play())
}

export const usePause = () => {
  return () => trackPlayerCommands.enqueue(() => TrackPlayer.pause())
}

export const usePrevious = () => {
  return () =>
    trackPlayerCommands.enqueue(async () => {
      const [current] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current > 0) {
        await TrackPlayer.skipToPrevious()
      } else {
        await TrackPlayer.seekTo(0)
      }
      await TrackPlayer.play()
    })
}

export const useNext = () => {
  return () =>
    trackPlayerCommands.enqueue(async () => {
      const [current, queue] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current >= queue.length - 1) {
        await TrackPlayer.skip(0)
        await TrackPlayer.pause()
      } else {
        await TrackPlayer.skipToNext()
        await TrackPlayer.play()
      }
    })
}

export const useSkipTo = () => {
  return (track: number) =>
    trackPlayerCommands.enqueue(async () => {
      const queue = await getQueue()
      if (track < 0 || track >= queue.length) {
        return
      }
      await TrackPlayer.skip(track)
      await TrackPlayer.play()
    })
}

export const useSeekTo = () => {
  return (position: number) =>
    trackPlayerCommands.enqueue(async () => {
      await TrackPlayer.seekTo(position)
    })
}

export const useReset = (enqueue = true) => {
  const resetStore = useStore(store => store.resetTrackPlayerState)

  const reset = async () => {
    await TrackPlayer.reset()
    resetStore()
  }

  return enqueue ? () => trackPlayerCommands.enqueue(reset) : reset
}

export const useIsPlaying = (contextId: string | undefined, track: number) => {
  const queueContextId = useStore(store => store.queueContextId)
  const currentTrackIdx = useStore(store => store.currentTrackIdx)
  const shuffleOrder = useStoreDeep(store => store.shuffleOrder)

  if (contextId === undefined) {
    return track === currentTrackIdx
  }

  if (shuffleOrder) {
    const shuffledTrack = shuffleOrder.findIndex(i => i === track)
    track = shuffledTrack !== undefined ? shuffledTrack : -1
  }

  return contextId === queueContextId && track === currentTrackIdx
}

export const useSetQueue = (songs: Song[]) => {
  const _setQueue = useStore(store => store.setQueue)
  const serverId = useStore(store => store.settings.activeServerId)
  const client = useStore(store => store.client)
  const buildStreamUri = useStore(store => store.buildStreamUri)
  const fetchFile = useFetchFile()

  const songCoverArt = uniq(songs.map(s => s.coverArt)).filter((c): c is string => c !== undefined)
  const coverArtPaths = useQueries(
    songCoverArt.map(coverArt => ({
      queryKey: qk.coverArt(coverArt, 'thumbnail'),
      queryFn: async () => {
        if (!serverId || !client) {
          return
        }

        const url = client.getCoverArtUri({ id: coverArt, size: '256' })
        return await fetchFile(serverId, 'coverArtThumb', coverArt, url)
      },
      enabled: !!serverId && !!client,
      staleTime: Infinity,
      cacheTime: Infinity,
      notifyOnChangeProps: ['data', 'isFetching'] as any,
    })),
  )

  const songCoverArtToPath = zipObject(
    songCoverArt,
    coverArtPaths.map(c => c.data),
  )

  const mapSongToTrackExt = (s: Song): TrackExt => {
    let artwork = require('@res/fallback.png')
    if (s.coverArt) {
      const filePath = songCoverArtToPath[s.coverArt]
      if (filePath) {
        artwork = `file://${filePath}`
      }
    }

    return {
      id: s.id,
      title: s.title,
      artist: s.artist || 'Unknown Artist',
      album: s.album || 'Unknown Album',
      url: buildStreamUri(s.id),
      userAgent,
      artwork,
      coverArt: s.coverArt,
      duration: s.duration,
      artistId: s.artistId,
      albumId: s.albumId,
      track: s.track,
      discNumber: s.discNumber,
    }
  }

  const setQueue = async (
    name: string,
    contextType: QueueContextType,
    contextId: string,
    playTrack?: number,
    shuffle?: boolean,
  ) => {
    const tracks = songs.map(mapSongToTrackExt)
    return await _setQueue(tracks, name, contextType, contextId, playTrack, shuffle)
  }

  return { setQueue, isReady: coverArtPaths.every(c => !c.isFetching) }
}
