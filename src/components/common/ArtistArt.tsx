import React from 'react';
import { View } from 'react-native';
import FastImage from 'react-native-fast-image';
import LinearGradient from 'react-native-linear-gradient';
import colors from '../../styles/colors';
import CoverArt from './CoverArt';

const PlaceholderContainer: React.FC<{
  height: number,
  width: number,
}> = ({ height, width, children}) => (
  <LinearGradient
    colors={[colors.accent, colors.accentLow]}
    style={{
      height, width,
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    {children}
  </LinearGradient>
);

const FourUp: React.FC<{
  height: number,
  width: number,
  coverArtUris: string[];
}> = ({ height, width, coverArtUris }) => {
  const halfHeight = height / 2;
  const halfWidth = width / 2;

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={{ width, height: halfHeight, flexDirection: 'row' }}>
        <FastImage
          source={{ uri: coverArtUris[0] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: coverArtUris[1] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={{ width, height: halfHeight, flexDirection: 'row' }}>
        <FastImage
          source={{ uri: coverArtUris[2] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: coverArtUris[3] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  );
};

const ThreeUp: React.FC<{
  height: number,
  width: number,
  coverArtUris: string[];
}> = ({ height, width, coverArtUris }) => {
  const halfHeight = height / 2;
  const halfWidth = width / 2;

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={{ width, height: halfHeight, flexDirection: 'row' }}>
        <FastImage
          source={{ uri: coverArtUris[0] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={{ width, height: halfHeight, flexDirection: 'row' }}>
        <FastImage
          source={{ uri: coverArtUris[1] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
        <FastImage
          source={{ uri: coverArtUris[2] }}
          style={{ height: halfHeight, width: halfWidth }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  );
};

const TwoUp: React.FC<{
  height: number,
  width: number,
  coverArtUris: string[];
}> = ({ height, width, coverArtUris }) => {
  const halfHeight = height / 2;

  return (
    <PlaceholderContainer height={height} width={width}>
      <View style={{ width, height: halfHeight, flexDirection: 'row' }}>
        <FastImage
          source={{ uri: coverArtUris[0] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
      <View style={{ width, height: halfHeight, flexDirection: 'row' }}>
        <FastImage
          source={{ uri: coverArtUris[1] }}
          style={{ height: halfHeight, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
      </View>
    </PlaceholderContainer>
  );
};

const OneUp: React.FC<{
  height: number,
  width: number,
  coverArtUris: string[];
}> = ({ height, width, coverArtUris }) => {
  return (
    <PlaceholderContainer height={height} width={width}>
        <FastImage
          source={{ uri: coverArtUris[0] }}
          style={{ height, width }}
          resizeMode={FastImage.resizeMode.cover}
        />
    </PlaceholderContainer>
  );
};

const NoneUp: React.FC<{
  height: number,
  width: number,
}> = ({ height, width }) => {
  return (
    <PlaceholderContainer height={height} width={width}>
        <FastImage
          source={require('../../../res/mic_on-fill.png')}
          style={{
            height: height - height / 4,
            width: width - width / 4,
          }}
          resizeMode={FastImage.resizeMode.cover}
        />
    </PlaceholderContainer>
  );
};

const ArtistArt: React.FC<{
  height: number,
  width: number,
  mediumImageUrl?: string;
  coverArtUris?: string[]
}> = ({ height, width, mediumImageUrl, coverArtUris }) => {
  const Placeholder = () => {
    if (coverArtUris && coverArtUris.length >= 4) {
      return <FourUp height={height} width={width} coverArtUris={coverArtUris} />;
    }
    if (coverArtUris && coverArtUris.length === 3) {
      return <ThreeUp height={height} width={width} coverArtUris={coverArtUris} />;
    }
    if (coverArtUris && coverArtUris.length === 2) {
      return <TwoUp height={height} width={width} coverArtUris={coverArtUris} />;
    }
    if (coverArtUris && coverArtUris.length === 1) {
      return <OneUp height={height} width={width} coverArtUris={coverArtUris} />;
    }
    return <NoneUp height={height} width={width} />;
  }

  return (
    <View style={{
      borderRadius: height / 2,
      overflow: 'hidden',
    }}>
      <CoverArt
        PlaceholderComponent={Placeholder}
        height={height}
        width={width}
        coverArtUri={mediumImageUrl}
      />
    </View>
  );
}

export default React.memo(ArtistArt);
