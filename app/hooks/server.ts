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

export const useActiveListRefresh = (list: unknown[], update: () => void) => {
  const activeServer = useStore(selectSettings.activeServer)

  useEffect(() => {
    if (list.length === 0) {
      update()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeServer])
}

export const useActiveListRefresh2 = (update: () => void) => {
  const activeServer = useStore(selectSettings.activeServer)

  useEffect(() => {
    update()
  }, [activeServer, update])
}

export const useActiveServerRefresh = (update: () => void) => {
  const activeServer = useStore(selectSettings.activeServer)

  useEffect(() => {
    if (activeServer) {
      update()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeServer])
}
