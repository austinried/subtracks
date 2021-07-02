import React from 'react';
import { useWindowDimensions, ViewStyle } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import colorStyles from '../../styles/colors';

const GradientBackground: React.FC<{
  height?: number | string;
  width?: number | string;
  position?: 'relative' | 'absolute';
  style?: ViewStyle;
  colors?: string[];
  locations?: number[];
}> = ({ height, width, position, style, colors, locations, children }) => {
  const layout = useWindowDimensions();

  return (
    <LinearGradient
      colors={colors || [colorStyles.gradient.high, colorStyles.gradient.low]}
      locations={locations || [0.01, 0.7]}
      style={{
        ...style,
        width: width || '100%',
        height: height || layout.height,
        position: position || 'absolute',
      }}>
      {children}
    </LinearGradient>
  );
};

export default GradientBackground;
