import RNFS from 'react-native-fs'
import path from 'path'
import { CacheItemTypeKey } from '@app/models/cache'

const serversCacheDir = path.join(RNFS.ExternalDirectoryPath, 's')

export function cacheDir(serverId?: string, itemType?: CacheItemTypeKey, itemId?: string): string {
  const segments: string[] = []

  serverId && segments.push(serverId)
  serverId && itemType && segments.push(itemType)
  serverId && itemType && itemId && segments.push(itemId)

  return path.join(serversCacheDir, ...segments)
}
