import { useQueryAlbumCoverArtPath, useQueryArtistArtPath, useQueryCoverArtPath } from '@app/hooks/query'
import { CacheImageSize } from '@app/models/cache'
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
  size: CacheImageSize
  fadeDuration?: number
}

type ArtistCoverArtProps = BaseProps & {
  type: 'artist'
  artistId: string
}

type CoverArtProps = BaseProps & {
  type: 'cover'
  coverArt?: string
}

type AlbumIdProps = BaseProps & {
  type: 'album'
  albumId?: string
}

type ImageSourceProps = BaseProps & {
  data?: string
  isFetching: boolean
  isExistingFetching: boolean
}

const ImageSource = React.memo<ImageSourceProps>(
  ({ style, imageStyle, resizeMode, data, isFetching, isExistingFetching, fadeDuration }) => {
    const [error, setError] = useState(false)

    let source: ImageSourcePropType
    if (!error && data) {
      source = { uri: `file://${data}` }
    } else {
      source = require('@res/fallback.png')
    }

    return (
      <>
        {isExistingFetching ? (
          <View style={{ height: style?.height, width: style?.width }} />
        ) : (
          <Image
            source={source}
            fadeDuration={fadeDuration === undefined ? 250 : fadeDuration}
            resizeMode={resizeMode || 'contain'}
            style={[{ height: style?.height, width: style?.width }, imageStyle]}
            onError={() => setError(true)}
          />
        )}
        {isFetching && (
          <ActivityIndicator animating={true} size="large" color={colors.accent} style={styles.indicator} />
        )}
      </>
    )
  },
)

const ArtistImage = React.memo<ArtistCoverArtProps>(props => {
  const { data, isFetching, isExistingFetching } = useQueryArtistArtPath(props.artistId, props.size)

  return <ImageSource data={data} isFetching={isFetching} isExistingFetching={isExistingFetching} {...props} />
})

const CoverArtImage = React.memo<CoverArtProps>(props => {
  const { data, isFetching, isExistingFetching } = useQueryCoverArtPath(props.coverArt, props.size)

  return <ImageSource data={data} isFetching={isFetching} isExistingFetching={isExistingFetching} {...props} />
})

const AlbumIdIamge = React.memo<AlbumIdProps>(props => {
  const { data, isFetching, isExistingFetching } = useQueryAlbumCoverArtPath(props.albumId, props.size)

  return <ImageSource data={data} isFetching={isFetching} isExistingFetching={isExistingFetching} {...props} />
})

const CoverArt = React.memo<CoverArtProps | ArtistCoverArtProps | AlbumIdProps>(props => {
  const viewStyles = [props.style]
  if (props.round) {
    viewStyles.push(styles.round)
  }

  let imageComponent
  switch (props.type) {
    case 'artist':
      imageComponent = <ArtistImage {...(props as ArtistCoverArtProps)} />
      break
    case 'album':
      imageComponent = <AlbumIdIamge {...(props as AlbumIdProps)} />
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
