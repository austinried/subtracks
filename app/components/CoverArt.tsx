import { useArtistArtFile, useCoverArtFile } from '@app/hooks/music'
import { CacheFile, CacheRequest } from '@app/models/music'
import colors from '@app/styles/colors'
import React, { useState } from 'react'
import { ActivityIndicator, StyleSheet, View, ViewStyle } from 'react-native'
import FastImage, { ImageStyle } from 'react-native-fast-image'

type BaseProps = {
  style?: ViewStyle
  imageStyle?: ImageStyle
  resizeMode?: keyof typeof FastImage.resizeMode
  round?: boolean
}

type ArtistCoverArtProps = BaseProps & {
  type: 'artist'
  artistId: string
}

type CoverArtProps = BaseProps & {
  type: 'cover'
  coverArt?: string
}

const Image = React.memo<{ cache?: { file?: CacheFile; request?: CacheRequest } } & BaseProps>(
  ({ cache, style, imageStyle, resizeMode }) => {
    const [error, setError] = useState(false)

    let source
    if (!error && cache?.file && !cache?.request?.promise) {
      source = { uri: `file://${cache.file.path}` }
    } else {
      source = require('@res/fallback.png')
    }

    return (
      <>
        <FastImage
          source={source}
          resizeMode={resizeMode || FastImage.resizeMode.contain}
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
  const cache = useArtistArtFile(props.artistId)

  return <Image cache={cache} {...props} />
})

const CoverArtImage = React.memo<CoverArtProps>(props => {
  const cache = useCoverArtFile(props.coverArt)

  return <Image cache={cache} {...props} />
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
})

export default CoverArt
