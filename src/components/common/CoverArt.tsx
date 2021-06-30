import React, { useState } from 'react';
import { ActivityIndicator, View } from 'react-native';
import FastImage from 'react-native-fast-image';
import colors from '../../styles/colors';

const CoverArt: React.FC<{
  PlaceholderComponent: () => JSX.Element,
  height: number,
  width: number,
  coverArtUri?: string
}> = ({ PlaceholderComponent, height, width, coverArtUri }) => {
  const [placeholderVisible, setPlaceholderVisible] = useState(false);
  const [loading, setLoading] = useState(true);

  const indicatorSize = height > 130 ? 'large' : 'small';
  const halfIndicatorHeight = indicatorSize === 'large' ? 18 : 10;

  const Placeholder: React.FC<{ visible: boolean }> = ({ visible }) => (
    <View style={{
      opacity: visible ? 100 : 0,
    }}>
      <PlaceholderComponent />
    </View>
  );

  const CoverArt = () => (
    <>
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
        onError={() => {
          setLoading(false);
          setPlaceholderVisible(true);
        }}
        onLoadEnd={() => setLoading(false)}
      />
    </>
  );

  return (
    <View style={{ height, width }}>
      {!coverArtUri ? <Placeholder visible={true} /> : <CoverArt />}
    </View>
  );
}

export default React.memo(CoverArt);
