import React from 'react'
import { WebView } from 'react-native-webview'

const WebViewScreen: React.FC<{
  uri: string
}> = ({ uri }) => {
  return <WebView source={{ uri }} />
}

export default WebViewScreen
