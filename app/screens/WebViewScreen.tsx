import { useNavigation } from '@react-navigation/native'
import React, { useEffect } from 'react'
import { WebView } from 'react-native-webview'

const WebViewScreen: React.FC<{
  uri: string
  title?: string
}> = ({ uri, title }) => {
  const navigation = useNavigation()

  useEffect(() => {
    navigation.setOptions({ title })
  }, [navigation, title])

  return <WebView source={{ uri }} />
}

export default WebViewScreen
