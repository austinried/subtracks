import { useReset } from '@app/hooks/trackplayer'
import { useStore } from '@app/state/store'
import { useEffect } from 'react'

export const useSwitchActiveServer = () => {
  const activeServer = useStore(store => store.settings.activeServer)
  const setActiveServer = useStore(store => store.setActiveServer)
  const resetPlayer = useReset()

  return async (id: string) => {
    if (id === activeServer) {
      return
    }

    await resetPlayer()
    setActiveServer(id)
  }
}

export const useActiveServerRefresh = (refresh: () => void) => {
  const activeServer = useStore(store => store.settings.activeServer)

  useEffect(() => {
    if (activeServer) {
      refresh()
    }
  }, [activeServer, refresh])
}

export const useFirstRun = () => {
  return useStore(store => Object.keys(store.settings.servers).length === 0)
}
