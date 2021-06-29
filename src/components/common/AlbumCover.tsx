import React, { useState } from 'react';
import { ActivityIndicator, View } from 'react-native';
import FastImage from 'react-native-fast-image';
import LinearGradient from 'react-native-linear-gradient';
import colors from '../../styles/colors';

const AlbumCover: React.FC<{
  height: number,
  width: number,
  coverArtUri?: string
}> = ({ height, width, coverArtUri }) => {
  const [placeholderVisible, setPlaceholderVisible] = useState(false);
  const [loading, setLoading] = useState(true);

  const indicatorSize = height > 130 ? 'large' : 'small';
  const halfIndicatorHeight = indicatorSize === 'large' ? 18 : 10;

  const Placeholder: React.FC<{ visible: boolean }> = ({ visible }) => (
    <LinearGradient
      colors={[colors.accent, colors.accentLow]}
      style={{
        height, width,
        opacity: visible ? 100 : 0,
      }}
    >
      <FastImage
        source={require('../../../res/record.png')}
        style={{ height, width }}
        resizeMode={FastImage.resizeMode.contain}
      />
    </LinearGradient>
  );

  const CoverArt = () => (
    <View>
      <Placeholder visible={placeholderVisible} />
      <ActivityIndicator
        animating={loading}
        size={indicatorSize}
        color={colors.accent}
        style={{
          top: -height / 2 - halfIndicatorHeight,
        }}
      />
      <FastImage
        source={{ uri: coverArtUri, priority: 'high' }}
        style={{
          height, width,
          marginTop: -height - halfIndicatorHeight * 2,
        }}
        resizeMode={FastImage.resizeMode.contain}
        onError={() => setPlaceholderVisible(true)}
        onLoadEnd={() => setLoading(false)}
      />
    </View>
  );

  return (
    <View style={{ height, width }}>
      {!coverArtUri ? <Placeholder visible={placeholderVisible} /> : <CoverArt />}
    </View>
  );
}

export default React.memo(AlbumCover);
