import React, { useEffect, useState } from 'react'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import FastImage from 'react-native-fast-image'
import colors from '@app/styles/colors'

const CoverArt: React.FC<{
  PlaceholderComponent: () => JSX.Element
  height?: string | number
  width?: string | number
  coverArtUri?: string
}> = ({ PlaceholderComponent, height, width, coverArtUri }) => {
  const [placeholderVisible, setPlaceholderVisible] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!coverArtUri) {
      setLoading(false)
    }
  }, [coverArtUri, setLoading])

  const Image = () => (
    <FastImage
      source={{ uri: coverArtUri, priority: 'high' }}
      style={{ ...styles.image, opacity: placeholderVisible ? 0 : 1 }}
      resizeMode={FastImage.resizeMode.contain}
      onError={() => {
        setLoading(false)
        setPlaceholderVisible(true)
      }}
      onLoadEnd={() => setLoading(false)}
    />
  )

  return (
    <View style={{ ...styles.container, height, width }}>
      {coverArtUri ? <Image /> : <></>}
      <View style={{ ...styles.placeholderContainer, opacity: placeholderVisible ? 1 : 0 }}>
        <PlaceholderComponent />
      </View>
      <ActivityIndicator style={styles.indicator} animating={loading} size={'large'} color={colors.accent} />
    </View>
  )
}

const styles = StyleSheet.create({
  container: {},
  image: {
    height: '100%',
    width: '100%',
  },
  placeholderContainer: {
    height: '100%',
    width: '100%',
    position: 'absolute',
  },
  indicator: {
    height: '100%',
    width: '100%',
    position: 'absolute',
  },
})

export default React.memo(CoverArt)
