import { useAtomValue } from 'jotai/utils'
import { activeServerAtom } from '../state/settings'
import { SubsonicApiClient } from '../subsonic/api'

export const useSubsonicApi = () => {
  const activeServer = useAtomValue(activeServerAtom)

  return () => {
    if (!activeServer) {
      return undefined
    }
    return new SubsonicApiClient(activeServer)
  }
}
