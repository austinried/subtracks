import React, { useEffect, useState } from 'react'
import { ActivityIndicator, LayoutChangeEvent, StyleSheet, View } from 'react-native'
import FastImage from 'react-native-fast-image'
import colors from '@app/styles/colors'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'
import LinearGradient from 'react-native-linear-gradient'

const CoverArt: React.FC<{
  PlaceholderComponent?: () => JSX.Element
  placeholderIcon?: string
  height?: string | number
  width?: string | number
  coverArtUri?: string
  resizeMode?: keyof typeof FastImage.resizeMode
}> = ({ PlaceholderComponent, placeholderIcon, height, width, coverArtUri, resizeMode }) => {
  const [placeholderVisible, setPlaceholderVisible] = useState(false)
  const [loading, setLoading] = useState(true)
  const [layout, setLayout] = useState({ x: 0, y: 0, width: 0, height: 0 })

  useEffect(() => {
    if (!coverArtUri) {
      setLoading(false)
    }
  }, [coverArtUri, setLoading])

  const Image = () => (
    <FastImage
      source={{ uri: coverArtUri, priority: 'high' }}
      style={{ ...styles.image, opacity: placeholderVisible ? 0 : 1 }}
      resizeMode={resizeMode || FastImage.resizeMode.contain}
      onError={() => {
        setLoading(false)
        setPlaceholderVisible(true)
      }}
      onLoadEnd={() => setLoading(false)}
    />
  )

  const Placeholder = () => (
    <LinearGradient colors={[colors.accent, colors.accentLow]} style={styles.placeholder}>
      <IconFA5 name={placeholderIcon || 'record-vinyl'} color="black" size={layout.width / 1.5} />
    </LinearGradient>
  )

  const onLayout = (event: LayoutChangeEvent) => {
    setLayout(event.nativeEvent.layout)
  }

  return (
    <View style={{ ...styles.container, height, width }} onLayout={onLayout}>
      {coverArtUri ? <Image /> : <></>}
      <View style={{ ...styles.placeholderContainer, opacity: placeholderVisible ? 1 : 0 }}>
        {PlaceholderComponent ? <PlaceholderComponent /> : <Placeholder />}
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
  placeholder: {
    height: '100%',
    width: '100%',
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
