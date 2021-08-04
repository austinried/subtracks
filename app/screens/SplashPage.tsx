import React, { useEffect, useState } from 'react'
import { Text, View } from 'react-native'
import RNFS from 'react-native-fs'
import paths from '@app/util/paths'
import { Store, useStore } from '@app/state/store'

async function mkdir(path: string): Promise<void> {
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

const selectHydrated = (store: Store) => store.hydrated

const SplashPage: React.FC<{}> = ({ children }) => {
  const [ready, setReady] = useState(false)
  const hydrated = useStore(selectHydrated)

  const minSplashTime = new Promise(resolve => setTimeout(resolve, 1))

  const prepare = async () => {
    await mkdir(paths.imageCache)
    await mkdir(paths.songCache)
    await mkdir(paths.songs)
  }

  const promise = Promise.all([prepare(), minSplashTime])

  useEffect(() => {
    promise.then(() => {
      setReady(true)
    })
  })

  if (ready && hydrated) {
    return <View style={{ flex: 1 }}>{children}</View>
  } else {
    return <Text>Loading THE GOOD SHIT...</Text>
  }
}

export default SplashPage
