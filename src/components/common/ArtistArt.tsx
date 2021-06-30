import { useAtom } from 'jotai';
import { useAtomValue } from 'jotai/utils';
import React from 'react';
import { ActivityIndicator, View } from 'react-native';
import FastImage from 'react-native-fast-image';
import LinearGradient from 'react-native-linear-gradient';
import { artistArtAtomFamily } from '../../state/music';
import colors from '../../styles/colors';
import CoverArt from './CoverArt';

interface ArtistArtSizeProps {
  height: number;
  width: number;
};

interface ArtistArtXUpProps extends ArtistArtSizeProps {
  coverArtUris: string[];
}

interface ArtistArtProps extends ArtistArtSizeProps {
  id: string;
}

const PlaceholderContainer: React.FC<ArtistArtSizeProps> = ({ height, width, children }) => (
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

const FourUp: React.FC<ArtistArtXUpProps> = ({ height, width, coverArtUris }) => {
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

const ThreeUp: React.FC<ArtistArtXUpProps> = ({ height, width, coverArtUris }) => {
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

const TwoUp: React.FC<ArtistArtXUpProps> = ({ height, width, coverArtUris }) => {
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

const OneUp: React.FC<ArtistArtXUpProps> = ({ height, width, coverArtUris }) => {
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

const NoneUp: React.FC<ArtistArtSizeProps> = ({ height, width }) => {
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

const ArtistArt: React.FC<ArtistArtProps> = ({ id, height, width }) => {
  const artistArt = useAtomValue(artistArtAtomFamily(id));

  const Placeholder = () => {
    const none = <NoneUp height={height} width={width} />;

    if (!artistArt || !artistArt.coverArtUris) {
      return none;
    }
    const { coverArtUris } = artistArt;

    if (coverArtUris.length >= 4) {
      return <FourUp height={height} width={width} coverArtUris={coverArtUris} />;
    }
    if (coverArtUris.length === 3) {
      return <ThreeUp height={height} width={width} coverArtUris={coverArtUris} />;
    }
    if (coverArtUris.length === 2) {
      return <TwoUp height={height} width={width} coverArtUris={coverArtUris} />;
    }
    if (coverArtUris.length === 1) {
      return <OneUp height={height} width={width} coverArtUris={coverArtUris} />;
    }

    return none;
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
        coverArtUri={artistArt?.uri}
      />
    </View>
  );
}

const ArtistArtFallback: React.FC<ArtistArtProps> = ({ height, width }) => (
  <View style={{ 
    height, width,
    alignItems: 'center',
    justifyContent: 'center',
  }}>
    <ActivityIndicator size='small' color={colors.accent} />
  </View>
);

const ArtistArtLoader: React.FC<ArtistArtProps> = (props) => (
  <React.Suspense fallback={<ArtistArtFallback { ...props } />}>
    <ArtistArt { ...props } />
  </React.Suspense>
);

export default React.memo(ArtistArtLoader);
