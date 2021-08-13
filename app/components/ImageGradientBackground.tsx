import { useNavigation } from '@react-navigation/native'
import React, { useEffect, useState } from 'react'
import { ViewStyle } from 'react-native'
import ImageColors from 'react-native-image-colors'
import { AndroidImageColors } from 'react-native-image-colors/lib/typescript/types'
import colors from '@app/styles/colors'
import GradientBackground from '@app/components/GradientBackground'

const ImageGradientBackground: React.FC<{
  height?: number | string
  width?: number | string
  position?: 'relative' | 'absolute'
  style?: ViewStyle
  imagePath?: string
}> = ({ height, width, position, style, imagePath, children }) => {
  const [highColor, setHighColor] = useState<string>(colors.gradient.high)
  const navigation = useNavigation()

  useEffect(() => {
    async function getColors() {
      if (imagePath === undefined) {
        return
      }

      const cachedResult = ImageColors.cache.getItem(imagePath)

      let res: AndroidImageColors
      if (cachedResult) {
        res = cachedResult as AndroidImageColors
      } else {
        const path = `file://${imagePath}`
        res = (await ImageColors.getColors(path, {
          cache: true,
          key: imagePath,
        })) as AndroidImageColors
      }

      if (res.muted && res.muted !== '#000000') {
        setHighColor(res.muted)
      } else if (res.darkMuted && res.darkMuted !== '#000000') {
        setHighColor(res.darkMuted)
      }
    }
    getColors()
  }, [imagePath])

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
