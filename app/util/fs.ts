import RNFS from 'react-native-fs'

export async function mkdir(path: string): Promise<void> {
  const exists = await RNFS.exists(path)
  if (exists) {
    const isDir = (await RNFS.stat(path)).isDirectory()
    if (!isDir) {
      throw new Error(`path exists and is not a directory: ${path}`)
    } else {
      return
    }
  }

  return await RNFS.mkdir(path)
}

export async function rmdir(path: string): Promise<void> {
  return RNFS.unlink(path)
}
