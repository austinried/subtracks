import { useReset } from '@app/hooks/trackplayer'
import { useStore } from '@app/state/store'
import { useEffect } from 'react'

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
