import { useAtomValue } from 'jotai/utils'
import React, { useState } from 'react'
import { ActivityIndicator, LayoutChangeEvent, StyleSheet, View } from 'react-native'
import FastImage from 'react-native-fast-image'
import LinearGradient from 'react-native-linear-gradient'
import { artistArtAtomFamily } from '@app/state/music'
import colors from '@app/styles/colors'
import CoverArt from '@app/components/CoverArt'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'

interface ArtistArtSizeProps {
  height: number
  width: number
}

interface ArtistArtXUpProps extends ArtistArtSizeProps {
  coverArtUris: string[]
}

interface ArtistArtProps extends ArtistArtSizeProps {
  id: string
}

const PlaceholderContainer: React.FC<ArtistArtSizeProps> = ({ height, width, children }) => {
  const [layout, setLayout] = useState({ x: 0, y: 0, width: 0, height: 0 })

  const onLayout = (event: LayoutChangeEvent) => {
    setLayout(event.nativeEvent.layout)
  }

  return (
    <LinearGradient
      onLayout={onLayout}
      colors={[colors.accent, colors.accentLow]}
      style={[styles.placeholderContainer, { height, width }]}>
      <IconFA5 name="microphone" color="black" size={layout.width / 1.8} style={styles.placeholderIcon} />
      {children}
    </LinearGradient>
  )
}

const FourUp = React.memo<ArtistArtXUpProps>(({ height, width, coverArtUris }) => {
  const halfHeight = height / 2
  const halfWidth = width / 2

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: coverArtUris[0] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: coverArtUris[1] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: coverArtUris[2] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: coverArtUris[3] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  )
})

const ThreeUp = React.memo<ArtistArtXUpProps>(({ height, width, coverArtUris }) => {
  const halfHeight = height / 2
  const halfWidth = width / 2

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: coverArtUris[0] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: coverArtUris[1] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: coverArtUris[2] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  )
})

const TwoUp = React.memo<ArtistArtXUpProps>(({ height, width, coverArtUris }) => {
  const halfHeight = height / 2

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: coverArtUris[0] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: coverArtUris[1] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  )
})

const OneUp = React.memo<ArtistArtXUpProps>(({ height, width, coverArtUris }) => (
  <PlaceholderContainer height={height} width={width}>
    <FastImage source={{ uri: coverArtUris[0] }} style={{ height, width }} resizeMode={FastImage.resizeMode.cover} />
  </PlaceholderContainer>
))

const NoneUp = React.memo<ArtistArtSizeProps>(({ height, width }) => (
  <PlaceholderContainer height={height} width={width} />
))

const ArtistArt = React.memo<ArtistArtProps>(({ id, height, width }) => {
  const artistArt = useAtomValue(artistArtAtomFamily(id))

  const Placeholder = () => {
    const none = <NoneUp height={height} width={width} />

    if (!artistArt || !artistArt.coverArtUris) {
      return none
    }
    const { coverArtUris } = artistArt

    if (coverArtUris.length >= 4) {
      return <FourUp height={height} width={width} coverArtUris={coverArtUris} />
    }
    if (coverArtUris.length === 3) {
      return <ThreeUp height={height} width={width} coverArtUris={coverArtUris} />
    }
    if (coverArtUris.length === 2) {
      return <TwoUp height={height} width={width} coverArtUris={coverArtUris} />
    }
    if (coverArtUris.length === 1) {
      return <OneUp height={height} width={width} coverArtUris={coverArtUris} />
    }

    return none
  }

  return (
    <View style={[styles.container, { borderRadius: height / 2 }]}>
      <CoverArt PlaceholderComponent={Placeholder} height={height} width={width} coverArtUri={artistArt?.uri} />
    </View>
  )
})

const ArtistArtFallback = React.memo<ArtistArtProps>(({ height, width }) => (
  <View style={[styles.fallback, { height, width }]}>
    <ActivityIndicator size="large" color={colors.accent} />
  </View>
))

const ArtistArtLoader: React.FC<ArtistArtProps> = props => (
  <React.Suspense fallback={<ArtistArtFallback {...props} />}>
    <ArtistArt {...props} />
  </React.Suspense>
)

const styles = StyleSheet.create({
  placeholderContainer: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  placeholderIcon: {
    position: 'absolute',
  },
  artRow: {
    flexDirection: 'row',
  },
  container: {
    overflow: 'hidden',
  },
  fallback: {
    alignItems: 'center',
    justifyContent: 'center',
  },
})

export default React.memo(ArtistArtLoader)
