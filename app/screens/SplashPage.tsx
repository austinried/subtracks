import React, { useEffect, useState } from 'react'
import { Text, View } from 'react-native'
import { Store, useStore } from '@app/state/store'

const selectHydrated = (store: Store) => store.hydrated

const SplashPage: React.FC<{}> = ({ children }) => {
  const [ready, setReady] = useState(false)
  const hydrated = useStore(selectHydrated)

  const minSplashTime = new Promise(resolve => setTimeout(resolve, 1))

  const prepare = async () => {
    return
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
