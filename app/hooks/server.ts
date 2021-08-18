import { useReset } from '@app/hooks/trackplayer'
import { selectSettings } from '@app/state/settings'
import { useStore } from '@app/state/store'
import { useEffect } from 'react'

export const useSwitchActiveServer = () => {
  const activeServer = useStore(selectSettings.activeServer)
  const setActiveServer = useStore(selectSettings.setActiveServer)
  const resetPlayer = useReset()

  return async (id: string) => {
    if (id === activeServer?.id) {
      return
    }

    await resetPlayer()
    setActiveServer(id)
  }
}

export const useActiveServerRefresh = (refresh: () => void) => {
  const activeServer = useStore(selectSettings.activeServer)

  useEffect(() => {
    if (activeServer) {
      refresh()
    }
  }, [activeServer, refresh])
}
