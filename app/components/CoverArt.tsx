import colors from '@app/styles/colors'
import React, { useState } from 'react'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import FastImage, { ImageStyle } from 'react-native-fast-image'
import LinearGradient from 'react-native-linear-gradient'

type CoverImageProps = {
  uri?: string
  style?: ImageStyle
  resizeMode?: keyof typeof FastImage.resizeMode
  onProgress?: () => void
  onLoadEnd?: () => void
  onError?: () => void
}

const CoverImage = React.memo<CoverImageProps>(({ uri, style, resizeMode, onProgress, onLoadEnd, onError }) => (
  <FastImage
    source={{ uri }}
    style={style}
    resizeMode={resizeMode || FastImage.resizeMode.contain}
    onProgress={onProgress}
    onLoadEnd={onLoadEnd}
    onError={onError}
  />
))

const Fallback = React.memo<{}>(({}) => {
  return <LinearGradient colors={[colors.accent, colors.accentLow]} style={styles.fallback} />
})

const CoverArt: React.FC<{
  FallbackComponent?: () => JSX.Element
  placeholderIcon?: string
  height?: string | number
  width?: string | number
  coverArtUri?: string
  resizeMode?: keyof typeof FastImage.resizeMode
  style?: ImageStyle
}> = ({ FallbackComponent, coverArtUri, resizeMode, style }) => {
  const [loading, setLoading] = useState(false)
  const [fallbackVisible, setFallbackVisible] = useState(false)

  const enableLoading = React.useCallback(() => setLoading(true), [])
  const disableLoading = React.useCallback(() => setLoading(false), [])
  const enableFallback = React.useCallback(() => setFallbackVisible(true), [])

  return (
    <View style={style}>
      <CoverImage
        uri={coverArtUri}
        style={style}
        resizeMode={resizeMode}
        onProgress={enableLoading}
        onLoadEnd={disableLoading}
        onError={enableFallback}
      />
      {fallbackVisible ? (
        FallbackComponent ? (
          <View style={styles.fallback}>
            <FallbackComponent />
          </View>
        ) : (
          <Fallback />
        )
      ) : (
        <></>
      )}
      <ActivityIndicator animating={loading} size="large" color={colors.accent} style={styles.indicator} />
    </View>
  )
}

const styles = StyleSheet.create({
  image: {
    height: '100%',
    width: '100%',
  },
  fallback: {
    height: '100%',
    width: '100%',
    position: 'absolute',
    justifyContent: 'center',
    alignItems: 'center',
  },
  indicator: {
    height: '100%',
    width: '100%',
    position: 'absolute',
  },
})

export default React.memo(CoverArt)
