import { useReset } from '@app/hooks/trackplayer'
import { CacheItemTypeKey } from '@app/models/cache'
import queryClient from '@app/query/queryClient'
import { useStore, useStoreDeep } from '@app/state/store'
import cacheDir from '@app/util/cacheDir'
import RNFS from 'react-native-fs'
import qk from '@app/query/queryKeys'

export const useSwitchActiveServer = () => {
  const activeServerId = useStore(store => store.settings.activeServerId)
  const setActiveServer = useStore(store => store.setActiveServer)
  const resetPlayer = useReset()

  return async (id: string) => {
    if (id === activeServerId) {
      return
    }

    await queryClient.cancelQueries(undefined, { active: true })
    await resetPlayer()
    queryClient.removeQueries()
    setActiveServer(id)
  }
}

export const useFirstRun = () => {
  return useStore(store => Object.keys(store.settings.servers).length === 0)
}

export const useResetImageCache = () => {
  const serverIds = useStoreDeep(store => Object.keys(store.settings.servers))
  const changeCacheBuster = useStore(store => store.changeCacheBuster)
  const setDisableMusicTabs = useStore(store => store.setDisableMusicTabs)

  return async () => {
    setDisableMusicTabs(true)

    try {
      // disable/invalidate queries
      await Promise.all([
        queryClient.cancelQueries(qk.artistArt(), { active: true }),
        queryClient.cancelQueries(qk.coverArt(), { active: true }),
        queryClient.cancelQueries(qk.existingFiles(), { active: true }),
        queryClient.invalidateQueries(qk.artistArt(), { refetchActive: false }),
        queryClient.invalidateQueries(qk.coverArt(), { refetchActive: false }),
        queryClient.invalidateQueries(qk.existingFiles(), { refetchActive: false }),
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
    } finally {
      setDisableMusicTabs(false)

      // enable queries
      await Promise.all([
        queryClient.refetchQueries(qk.existingFiles(), { active: true }),
        queryClient.refetchQueries(qk.artistArt(), { active: true }),
        queryClient.refetchQueries(qk.coverArt(), { active: true }),
      ])
    }
  }
}
