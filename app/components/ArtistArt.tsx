import CoverArt from '@app/components/CoverArt'
import { artistArtAtomFamily } from '@app/state/music'
import colors from '@app/styles/colors'
import { useLayout } from '@react-native-community/hooks'
import { useAtomValue } from 'jotai/utils'
import React from 'react'
import { ActivityIndicator, StyleSheet, View } from 'react-native'
import FastImage from 'react-native-fast-image'
import LinearGradient from 'react-native-linear-gradient'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'

interface ArtistArtSizeProps {
  height: number
  width: number
}

interface ArtistArtXUpProps extends ArtistArtSizeProps {
  albumCoverUris: string[]
}

interface ArtistArtProps extends ArtistArtSizeProps {
  id: string
  round?: boolean
}

const PlaceholderContainer: React.FC<ArtistArtSizeProps> = ({ height, width, children }) => {
  const layout = useLayout()

  return (
    <LinearGradient
      onLayout={layout.onLayout}
      colors={[colors.accent, colors.accentLow]}
      style={[styles.placeholderContainer, { height, width }]}>
      <IconFA5 name="microphone" color="black" size={layout.width / 1.8} style={styles.placeholderIcon} />
      {children}
    </LinearGradient>
  )
}

const FourUp = React.memo<ArtistArtXUpProps>(({ height, width, albumCoverUris }) => {
  const halfHeight = height / 2
  const halfWidth = width / 2

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: albumCoverUris[0] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: albumCoverUris[1] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: albumCoverUris[2] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: albumCoverUris[3] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  )
})

const ThreeUp = React.memo<ArtistArtXUpProps>(({ height, width, albumCoverUris }) => {
  const halfHeight = height / 2
  const halfWidth = width / 2

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: albumCoverUris[0] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: albumCoverUris[1] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: albumCoverUris[2] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  )
})

const TwoUp = React.memo<ArtistArtXUpProps>(({ height, width, albumCoverUris }) => {
  const halfHeight = height / 2

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: albumCoverUris[0] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={[styles.artRow, { width, height: halfHeight }]}>
        <FastImage
          source={{ uri: albumCoverUris[1] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  )
})

const OneUp = React.memo<ArtistArtXUpProps>(({ height, width, albumCoverUris }) => (
  <PlaceholderContainer height={height} width={width}>
    <FastImage source={{ uri: albumCoverUris[0] }} style={{ height, width }} resizeMode={FastImage.resizeMode.cover} />
  </PlaceholderContainer>
))

const NoneUp = React.memo<ArtistArtSizeProps>(({ height, width }) => (
  <PlaceholderContainer height={height} width={width} />
))

const ArtistArt = React.memo<ArtistArtProps>(({ id, height, width, round }) => {
  const artistArt = useAtomValue(artistArtAtomFamily(id))

  round = round === undefined ? true : round

  const Placeholder = () => {
    if (!artistArt) {
      return <NoneUp height={height} width={width} />
    }
    const { albumCoverUris } = artistArt

    if (albumCoverUris.length >= 4) {
      return <FourUp height={height} width={width} albumCoverUris={albumCoverUris} />
    }
    if (albumCoverUris.length === 3) {
      return <ThreeUp height={height} width={width} albumCoverUris={albumCoverUris} />
    }
    if (albumCoverUris.length === 2) {
      return <TwoUp height={height} width={width} albumCoverUris={albumCoverUris} />
    }
    if (albumCoverUris.length === 1) {
      return <OneUp height={height} width={width} albumCoverUris={albumCoverUris} />
    }

    return <NoneUp height={height} width={width} />
  }

  return (
    <View style={[styles.container, round ? { borderRadius: height / 2 } : {}]}>
      <CoverArt
        FallbackComponent={Placeholder}
        style={{ height, width }}
        coverArtUri={artistArt?.uri}
        resizeMode={FastImage.resizeMode.cover}
      />
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
