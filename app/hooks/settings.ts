import { useReset } from '@app/hooks/trackplayer'
import { CacheItemTypeKey } from '@app/models/cache'
import queryClient from '@app/queryClient'
import { useStore, useStoreDeep } from '@app/state/store'
import { cacheDir } from '@app/util/fs'
import { useEffect } from 'react'
import RNFS from 'react-native-fs'
import qk from './queryKeys'

export const useSwitchActiveServer = () => {
  const activeServerId = useStore(store => store.settings.activeServerId)
  const setActiveServer = useStore(store => store.setActiveServer)
  const resetPlayer = useReset()

  return async (id: string) => {
    if (id === activeServerId) {
      return
    }

    await resetPlayer()
    setActiveServer(id)
  }
}

export const useActiveServerRefresh = (refresh: () => void) => {
  const activeServerId = useStore(store => store.settings.activeServerId)

  useEffect(() => {
    if (activeServerId) {
      refresh()
    }
  }, [activeServerId, refresh])
}

export const useFirstRun = () => {
  return useStore(store => Object.keys(store.settings.servers).length === 0)
}

export const useResetImageCache = () => {
  const serverIds = useStoreDeep(store => Object.keys(store.settings.servers))
  const changeCacheBuster = useStore(store => store.changeCacheBuster)

  return async () => {
    // disable/invalidate queries
    await Promise.all([
      queryClient.cancelQueries(qk.artistArt()),
      queryClient.cancelQueries(qk.coverArt()),
      queryClient.invalidateQueries(qk.artistArt(), { refetchActive: false }),
      queryClient.invalidateQueries(qk.coverArt(), { refetchActive: false }),
    ])

    // delete all images
    const itemTypes: CacheItemTypeKey[] = ['artistArt', 'artistArtThumb', 'coverArt', 'coverArtThumb']
    await Promise.all(
      serverIds.flatMap(id =>
        itemTypes.map(async type => {
          const dir = cacheDir(id, type)
          try {
            await RNFS.unlink(dir)
          } catch {}
        }),
      ),
    )

    // change cacheBuster
    changeCacheBuster()

    // enable queries
    await Promise.all([
      queryClient.refetchQueries(qk.artistArt(), { active: true }),
      queryClient.refetchQueries(qk.coverArt(), { active: true }),
    ])
  }
}
