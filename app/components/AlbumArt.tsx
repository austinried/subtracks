import { useAtomValue } from 'jotai/utils'
import React from 'react'
import { ActivityIndicator, View } from 'react-native'
import FastImage from 'react-native-fast-image'
import LinearGradient from 'react-native-linear-gradient'
import { albumArtAtomFamily } from '@app/state/music'
import colors from '@app/styles/colors'
import CoverArt from '@app/components/CoverArt'

interface AlbumArtProps {
  id: string
  height: number
  width: number
}

const AlbumArt: React.FC<AlbumArtProps> = ({ id, height, width }) => {
  const albumArt = useAtomValue(albumArtAtomFamily(id))

  const Placeholder = () => (
    <LinearGradient colors={[colors.accent, colors.accentLow]}>
      <FastImage
        source={require('@res/icons/record.png')}
        style={{ height, width }}
        resizeMode={FastImage.resizeMode.contain}
      />
    </LinearGradient>
  )

  return (
    <CoverArt
      PlaceholderComponent={Placeholder}
      height={height}
      width={width}
      coverArtUri={width > 128 ? albumArt?.uri : albumArt?.thumbUri}
    />
  )
}

const AlbumArtFallback: React.FC<AlbumArtProps> = ({ height, width }) => (
  <View
    style={{
      height,
      width,
      alignItems: 'center',
      justifyContent: 'center',
    }}>
    <ActivityIndicator size="small" color={colors.accent} />
  </View>
)

const AlbumArtLoader: React.FC<AlbumArtProps> = props => (
  <React.Suspense fallback={<AlbumArtFallback {...props} />}>
    <AlbumArt {...props} />
  </React.Suspense>
)

export default React.memo(AlbumArtLoader)
