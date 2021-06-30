import React from 'react';
import { useWindowDimensions } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import colors from '../../styles/colors';

const GradientBackground: React.FC<{
  height?: number | string;
  width?: number | string;
  position?: 'relative' | 'absolute';
}> = ({ height, width, position }) => {
  const layout = useWindowDimensions();

  return (
    <LinearGradient
      colors={[colors.gradient.high, colors.gradient.low]}
      locations={[0.01,0.7]}
      style={{
        width: width || '100%',
        height: height || layout.height,
        position: position || 'absolute',
      }}
    />
  );
};

export default GradientBackground;
