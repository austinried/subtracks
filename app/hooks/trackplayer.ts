import { Song } from '@app/models/library'
import { QueueContextType, TrackExt } from '@app/models/trackplayer'
import queryClient from '@app/queryClient'
import { useStore, useStoreDeep } from '@app/state/store'
import { getQueue, SetQueueOptions, trackPlayerCommands } from '@app/state/trackplayer'
import userAgent from '@app/util/userAgent'
import _ from 'lodash'
import { useCallback } from 'react'
import TrackPlayer from 'react-native-track-player'
import qk from './queryKeys'

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

export const useSetQueue = (type: QueueContextType, songs?: Song[]) => {
  const _setQueue = useStore(store => store.setQueue)
  const buildStreamUri = useStore(store => store.buildStreamUri)

  const mapSongToTrackExt = useCallback(
    (song: Song, artwork: string | number): TrackExt => ({
      id: song.id,
      title: song.title,
      artist: song.artist || 'Unknown Artist',
      album: song.album || 'Unknown Album',
      url: buildStreamUri(song.id),
      userAgent,
      artwork,
      duration: song.duration,
      artistId: song.artistId,
      albumId: song.albumId,
      track: song.track,
      discNumber: song.discNumber,
    }),
    [buildStreamUri],
  )

  const mapSongs = useCallback((): TrackExt[] => {
    const fallbackArtwork = require('@res/fallback.png')
    const albumIds = _.uniq((songs || []).map(s => s.albumId)).filter((id): id is string => id !== undefined)

    const albumCoverArts = albumIds.reduce((acc, id) => {
      acc[id] = queryClient.getQueryData<string>(qk.albumCoverArt(id))
      return acc
    }, {} as { [albumId: string]: string | undefined })

    const filePaths = albumIds.reduce((acc, id) => {
      const coverArt = albumCoverArts[id]

      acc[id] = queryClient.getQueryData<string>(qk.existingFiles('coverArtThumb', coverArt))

      if (!acc[id]) {
        acc[id] = queryClient.getQueryData<string>(qk.coverArt(coverArt, 'thumbnail'))
      }

      return acc
    }, {} as { [albumId: string]: string | undefined })

    return (songs || []).map(s => {
      if (s.albumId && filePaths[s.albumId]) {
        return mapSongToTrackExt(s, `file://${filePaths[s.albumId]}`)
      } else {
        return mapSongToTrackExt(s, fallbackArtwork)
      }
    })
  }, [mapSongToTrackExt, songs])

  const contextId = `${type}-${songs?.map(s => s.id).join('-')}`

  const setQueue = async (options: SetQueueOptions) => {
    const queue = mapSongs()
    return await _setQueue({ queue, type, contextId, ...options })
  }

  return { setQueue, contextId }
}
