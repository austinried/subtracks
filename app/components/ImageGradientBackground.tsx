import { useNavigation } from '@react-navigation/native'
import React, { useEffect, useState } from 'react'
import ImageColors from 'react-native-image-colors'
import { AndroidImageColors } from 'react-native-image-colors/lib/typescript/types'
import colors from '@app/styles/colors'
import GradientBackground, { GradientBackgroundPropsBase } from '@app/components/GradientBackground'

export type ImageGradientBackgroundProps = GradientBackgroundPropsBase & {
  imagePath?: string
  onGetColor?: (color: string) => void
}

const ImageGradientBackground: React.FC<ImageGradientBackgroundProps> = ({
  height,
  width,
  position,
  style,
  imagePath,
  children,
  onGetColor,
}) => {
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
      } else if (res.vibrant && res.vibrant !== '#000000') {
        setHighColor(res.vibrant)
      } else if (res.darkVibrant && res.darkVibrant !== '#000000') {
        setHighColor(res.darkVibrant)
      } else if (res.lightVibrant && res.lightVibrant !== '#000000') {
        setHighColor(res.lightVibrant)
      } else if (res.lightMuted && res.lightMuted !== '#000000') {
        setHighColor(res.lightMuted)
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

  useEffect(() => {
    onGetColor && onGetColor(highColor)
  }, [onGetColor, highColor])

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
