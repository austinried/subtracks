import { useAtomValue } from 'jotai/utils'
import { activeServerAtom } from '@app/state/settings'
import { SubsonicApiClient } from '@app/subsonic/api'

export const useSubsonicApi = () => {
  const activeServer = useAtomValue(activeServerAtom)

  return () => {
    if (!activeServer) {
      return undefined
    }
    return new SubsonicApiClient(activeServer)
  }
}
