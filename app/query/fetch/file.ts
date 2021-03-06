import { CacheItemTypeKey } from '@app/models/cache'
import queryClient from '@app/query/queryClient'
import qk from '@app/query/queryKeys'
import { useStore } from '@app/state/store'
import cacheDir from '@app/util/cacheDir'
import userAgent from '@app/util/userAgent'
import cd from 'content-disposition'
import mime from 'mime-types'
import path from 'path'
import ReactNativeBlobUtil, { FetchBlobResponse } from 'react-native-blob-util'
import RNFS from 'react-native-fs'

export type FetchExisingFileOptions = {
  itemType: CacheItemTypeKey
  itemId: string
}

export async function fetchExistingFile(
  options: FetchExisingFileOptions,
  serverId: string | undefined,
): Promise<string | undefined> {
  const { itemType, itemId } = options
  const fileDir = cacheDir(serverId, itemType, itemId)

  try {
    const dir = await RNFS.readDir(fileDir)
    console.log('existing file:', dir[0].path)
    return dir[0].path
  } catch {}
}

export const useFetchExistingFile = () => {
  const serverId = useStore(store => store.settings.activeServerId)
  return async (options: FetchExisingFileOptions) => fetchExistingFile(options, serverId)
}

function assertMimeType(expected?: string, actual?: string) {
  expected = expected?.toLowerCase()
  actual = actual?.toLowerCase()

  if (!expected || expected === actual) {
    return
  }

  if (!expected.includes(';')) {
    actual = actual?.split(';')[0]
  }

  if (!expected.includes('/')) {
    actual = actual?.split('/')[0]
  }

  if (expected !== actual) {
    throw new Error(`Request does not satisfy expected content type. Expected: ${expected} Actual: ${actual}`)
  }
}

export type FetchFileOptions = FetchExisingFileOptions & {
  fromUrl: string
  useCacheBuster?: boolean
  expectedContentType?: string
  progress?: (received: number, total: number) => void
}

export async function fetchFile(options: FetchFileOptions, serverId: string | undefined): Promise<string> {
  let { itemType, itemId, fromUrl, useCacheBuster, expectedContentType, progress } = options
  useCacheBuster = useCacheBuster === undefined ? true : useCacheBuster

  const fileDir = cacheDir(serverId, itemType, itemId)
  const filePathNoExt = path.join(fileDir, useCacheBuster ? useStore.getState().settings.cacheBuster : itemType)

  try {
    await RNFS.unlink(fileDir)
  } catch {}

  const headers = { 'User-Agent': userAgent }

  // we send a HEAD first for two reasons:
  // 1. to follow any redirects and get the actual URL (DownloadManager does not support redirects)
  // 2. to obtain the mime-type up front so we can use it for the file extension/validation
  const headRes = await fetch(fromUrl, { method: 'HEAD', headers })

  if (headRes.status > 399) {
    throw new Error(`HTTP status error ${headRes.status}. File: ${itemType} ID: ${itemId}`)
  }

  const contentType = headRes.headers.get('content-type') || undefined
  assertMimeType(expectedContentType, contentType)

  const contentDisposition = headRes.headers.get('content-disposition') || undefined
  const filename = contentDisposition ? cd.parse(contentDisposition).parameters.filename : undefined

  let extension: string | undefined
  if (filename) {
    extension = path.extname(filename) || undefined
    if (extension) {
      extension = extension.substring(1)
    }
  } else if (contentType) {
    extension = mime.extension(contentType) || undefined
  }

  const config = ReactNativeBlobUtil.config({
    addAndroidDownloads: {
      useDownloadManager: true,
      notification: false,
      mime: contentType,
      description: 'subtracks',
      path: extension ? `${filePathNoExt}.${extension}` : filePathNoExt,
    },
  })

  const fetchParams: Parameters<typeof config['fetch']> = ['GET', headRes.url, headers]

  let res: FetchBlobResponse
  if (progress) {
    res = await config.fetch(...fetchParams).progress(progress)
  } else {
    res = await config.fetch(...fetchParams)
  }

  const downloadPath = res.path()
  queryClient.setQueryData<string>(qk.existingFiles(itemType, itemId), downloadPath)

  console.log('downloaded file:', downloadPath)
  return downloadPath
}

export const useFetchFile = () => {
  const serverId = useStore(store => store.settings.activeServerId)
  return async (options: FetchFileOptions) => fetchFile(options, serverId)
}
