import React from 'react';
import FastImage from 'react-native-fast-image';
import LinearGradient from 'react-native-linear-gradient';
import colors from '../../styles/colors';
import CoverArt from './CoverArt';

const AlbumArt: React.FC<{
  height: number,
  width: number,
  coverArtUri?: string
}> = ({ height, width, coverArtUri }) => {
  const Placeholder = () => (
    <LinearGradient
      colors={[colors.accent, colors.accentLow]}
    >
      <FastImage
        source={require('../../../res/record.png')}
        style={{ height, width }}
        resizeMode={FastImage.resizeMode.contain}
      />
    </LinearGradient>
  );

  return (
    <CoverArt
      PlaceholderComponent={Placeholder}
      height={height}
      width={width}
      coverArtUri={coverArtUri}
    />
  );
}

export default React.memo(AlbumArt);
