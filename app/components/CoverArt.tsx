import { useArtistCoverArtFile, useCoverArtFile } from '@app/hooks/music'
import { DownloadFile } from '@app/state/music'
import colors from '@app/styles/colors'
import React, { useCallback, useEffect, useState } from 'react'
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

const Image: React.FC<{ file?: DownloadFile } & BaseProps> = ({ file, style, imageStyle, resizeMode }) => {
  const [source, setSource] = useState<number | { uri: string }>(
    file && file.progress === 1 ? { uri: `file://${file.path}` } : require('@res/fallback.png'),
  )

  useEffect(() => {
    if (file && file.progress === 1) {
      setSource({ uri: `file://${file.path}` })
    }
  }, [file])

  return (
    <>
      <FastImage
        source={source}
        resizeMode={resizeMode || FastImage.resizeMode.contain}
        style={[{ height: style?.height, width: style?.width }, imageStyle]}
        onError={() => {
          setSource(require('@res/fallback.png'))
        }}
      />
      <ActivityIndicator
        animating={file && file.progress < 1}
        size="large"
        color={colors.accent}
        style={styles.indicator}
      />
    </>
  )
}

const ArtistImage = React.memo<ArtistCoverArtProps>(props => {
  const file = useArtistCoverArtFile(props.artistId)

  return <Image file={file} {...props} />
})

const CoverArtImage = React.memo<CoverArtProps>(props => {
  const file = useCoverArtFile(props.coverArt)

  return <Image file={file} {...props} />
})

const CoverArt: React.FC<CoverArtProps | ArtistCoverArtProps> = props => {
  const viewStyles = [props.style]
  if (props.round) {
    viewStyles.push(styles.round)
  }

  const coverArtImage = useCallback(() => <CoverArtImage {...(props as CoverArtProps)} />, [props])
  const artistImage = useCallback(() => <ArtistImage {...(props as ArtistCoverArtProps)} />, [props])

  let ImageComponent
  switch (props.type) {
    case 'artist':
      ImageComponent = artistImage
      break
    default:
      ImageComponent = coverArtImage
      break
  }

  return (
    <View style={viewStyles}>
      <ImageComponent />
    </View>
  )
}

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

export default React.memo(CoverArt)
