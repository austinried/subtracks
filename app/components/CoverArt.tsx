import { useArtistArtFile, useCoverArtFile } from '@app/hooks/cache'
import { CacheFile, CacheImageSize, CacheRequest } from '@app/models/cache'
import colors from '@app/styles/colors'
import React, { useState } from 'react'
import {
  ActivityIndicator,
  Image,
  ImageResizeMode,
  ImageSourcePropType,
  ImageStyle,
  StyleSheet,
  View,
  ViewStyle,
} from 'react-native'

type BaseProps = {
  style?: ViewStyle
  imageStyle?: ImageStyle
  resizeMode?: ImageResizeMode
  round?: boolean
  size?: CacheImageSize
}

type ArtistCoverArtProps = BaseProps & {
  type: 'artist'
  artistId: string
}

type CoverArtProps = BaseProps & {
  type: 'cover'
  coverArt?: string
}

const ImageSource = React.memo<{ cache?: { file?: CacheFile; request?: CacheRequest } } & BaseProps>(
  ({ cache, style, imageStyle, resizeMode }) => {
    const [error, setError] = useState(false)

    let source: ImageSourcePropType
    if (!error && cache?.file && !cache?.request?.promise) {
      source = { uri: `file://${cache.file.path}`, cache: 'reload' }
    } else {
      source = require('@res/fallback.png')
    }

    return (
      <>
        <Image
          source={source}
          fadeDuration={150}
          resizeMode={resizeMode || 'contain'}
          style={[{ height: style?.height, width: style?.width }, imageStyle]}
          onError={() => setError(true)}
        />
        <ActivityIndicator
          animating={!!cache?.request?.promise}
          size="large"
          color={colors.accent}
          style={styles.indicator}
        />
      </>
    )
  },
)

const ArtistImage = React.memo<ArtistCoverArtProps>(props => {
  const cache = useArtistArtFile(props.artistId, props.size)

  return <ImageSource cache={cache} {...props} imageStyle={{ ...styles.artistImage, ...props.imageStyle }} />
})

const CoverArtImage = React.memo<CoverArtProps>(props => {
  const cache = useCoverArtFile(props.coverArt, props.size)

  return <ImageSource cache={cache} {...props} />
})

const CoverArt = React.memo<CoverArtProps | ArtistCoverArtProps>(props => {
  const viewStyles = [props.style]
  if (props.round) {
    viewStyles.push(styles.round)
  }

  let imageComponent
  switch (props.type) {
    case 'artist':
      imageComponent = <ArtistImage {...(props as ArtistCoverArtProps)} />
      break
    default:
      imageComponent = <CoverArtImage {...(props as CoverArtProps)} />
      break
  }

  return <View style={viewStyles}>{imageComponent}</View>
})

const styles = StyleSheet.create({
  round: {
    overflow: 'hidden',
    borderRadius: 1000,
  },
  indicator: {
    height: '100%',
    width: '100%',
    position: 'absolute',
  },
  artistImage: {
    backgroundColor: 'rgba(81, 28, 99, 0.4)',
  },
})

export default CoverArt
