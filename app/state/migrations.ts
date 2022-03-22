import { Store } from './store'
import RNFS from 'react-native-fs'
import { rmdir } from '@app/util/fs'

const migrations: Array<(state: any) => Promise<any>> = [
  async state => {
    for (const server of state.settings.servers) {
      server.usePlainPassword = false
    }

    return state
  },
  async state => {
    const store = state as Store
    const keysToDelete = ['coverArtThumb', 'artistArtThumb']

    for (const serverId in store.cacheDirs) {
      for (const key in keysToDelete) {
        await rmdir(`${RNFS.DocumentDirectoryPath}/servers/${serverId}/${key}`)
        delete state.cacheFiles[serverId][key]
      }
    }

    return state
  },
]

export default migrations
