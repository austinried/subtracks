import { useStore } from '@app/state/store'
import { StarParams } from '@app/subsonic/params'
import { useCallback, useEffect } from 'react'

type StarrableItem = 'album' | 'artist' | 'song'

function starParams(id: string, type: StarrableItem): StarParams {
  const params: StarParams = {}
  if (type === 'album') {
    params.albumId = id
  } else if (type === 'artist') {
    params.artistId = id
  } else {
    params.id = id
  }

  return params
}

export const useStar = (id: string, type: StarrableItem) => {
  const fetchAlbum = useStore(store => store.fetchAlbum)
  const fetchArtist = useStore(store => store.fetchArtist)
  const fetchSong = useStore(store => store.fetchSong)

  const _starred = useStore(
    useCallback(
      store => {
        if (type === 'album') {
          return store.library.albums[id] ? !!store.library.albums[id].starred : null
        } else if (type === 'artist') {
          return store.library.artists[id] ? !!store.library.artists[id].starred : null
        } else {
          return store.library.songs[id] ? !!store.library.songs[id].starred : null
        }
      },
      [id, type],
    ),
  )

  useEffect(() => {
    if (_starred === null) {
      if (type === 'album') {
        fetchAlbum(id)
      } else if (type === 'artist') {
        fetchArtist(id)
      } else {
        fetchSong(id)
      }
    }
  }, [fetchAlbum, fetchArtist, fetchSong, id, _starred, type])

  const starred = !!_starred

  const _star = useStore(store => store.star)
  const _unstar = useStore(store => store.unstar)

  const star = useCallback(() => _star(starParams(id, type)), [_star, id, type])
  const unstar = useCallback(() => _unstar(starParams(id, type)), [_unstar, id, type])

  const toggleStar = useCallback(() => (starred ? unstar() : star()), [star, starred, unstar])

  return { star, unstar, toggleStar, starred }
}
