import RNFS from 'react-native-fs'
import path from 'path'
import { CacheItemTypeKey } from '@app/models/cache'

const serversCacheDir = path.join(RNFS.ExternalDirectoryPath, 's')

function cacheDir(serverId?: string, itemType?: CacheItemTypeKey, itemId?: string): string {
  const segments: string[] = []

  serverId && segments.push(serverId)
  serverId && itemType && segments.push(itemType)
  serverId && itemType && itemId && segments.push(itemId)

  return path.join(serversCacheDir, ...segments)
}

export default cacheDir
