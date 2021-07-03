import { useNavigation } from '@react-navigation/native';
import React, { useEffect, useState } from 'react';
import { ViewStyle } from 'react-native';
import ImageColors from 'react-native-image-colors';
import { AndroidImageColors } from 'react-native-image-colors/lib/typescript/types';
import colors from '../../styles/colors';
import GradientBackground from './GradientBackground';

const ImageGradientBackground: React.FC<{
  height?: number | string;
  width?: number | string;
  position?: 'relative' | 'absolute';
  style?: ViewStyle;
  imageUri?: string;
}> = ({ height, width, position, style, imageUri, children }) => {
  const [highColor, setHighColor] = useState<string>(colors.gradient.high);
  const navigation = useNavigation();

  useEffect(() => {
    async function getColors() {
      if (imageUri === undefined) {
        return;
      }

      let res: AndroidImageColors;
      const cachedResult = ImageColors.cache.getItem(imageUri);
      if (cachedResult) {
        res = cachedResult as AndroidImageColors;
      } else {
        res = (await ImageColors.getColors(imageUri, {
          cache: true,
        })) as AndroidImageColors;
      }

      if (res.muted && res.muted !== '#000000') {
        setHighColor(res.muted);
      } else if (res.darkMuted && res.darkMuted !== '#000000') {
        setHighColor(res.darkMuted);
      }
    }
    getColors();
  }, [imageUri]);

  useEffect(() => {
    navigation.setOptions({
      headerStyle: {
        backgroundColor: highColor,
      },
    });
  }, [navigation, highColor]);

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
  );
};

export default ImageGradientBackground;
