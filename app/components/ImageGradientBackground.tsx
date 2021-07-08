import { useNavigation } from '@react-navigation/native'
import React, { useEffect, useState } from 'react'
import { ViewStyle } from 'react-native'
import FastImage from 'react-native-fast-image'
import ImageColors from 'react-native-image-colors'
import { AndroidImageColors } from 'react-native-image-colors/lib/typescript/types'
import colors from '@app/styles/colors'
import GradientBackground from '@app/components/GradientBackground'

const ImageGradientBackground: React.FC<{
  height?: number | string
  width?: number | string
  position?: 'relative' | 'absolute'
  style?: ViewStyle
  imageUri?: string
  imageKey?: string
}> = ({ height, width, position, style, imageUri, imageKey, children }) => {
  const [highColor, setHighColor] = useState<string>(colors.gradient.high)
  const navigation = useNavigation()

  useEffect(() => {
    async function getColors() {
      if (imageUri === undefined) {
        return
      }

      const cachedResult = ImageColors.cache.getItem(imageKey ? imageKey : imageUri)

      let res: AndroidImageColors
      if (cachedResult) {
        res = cachedResult as AndroidImageColors
      } else {
        const path = await FastImage.getCachePath({ uri: imageUri })
        res = (await ImageColors.getColors(path ? `file://${path}` : imageUri, {
          cache: true,
          key: imageKey ? imageKey : imageUri,
        })) as AndroidImageColors
      }

      if (res.muted && res.muted !== '#000000') {
        setHighColor(res.muted)
      } else if (res.darkMuted && res.darkMuted !== '#000000') {
        setHighColor(res.darkMuted)
      }
    }
    getColors()
  }, [imageUri, imageKey])

  useEffect(() => {
    navigation.setOptions({
      headerStyle: {
        backgroundColor: highColor,
      },
    })
  }, [navigation, highColor])

  return (
    <GradientBackground
      height={height}
      width={width}
      position={position}
      style={style}
      colors={[highColor, colors.gradient.low]}
      locations={[0.1, 1.0]}>
      {children}
    </GradientBackground>
  )
}

export default ImageGradientBackground
