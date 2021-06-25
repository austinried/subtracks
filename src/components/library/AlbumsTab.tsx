import React, { useEffect, useState } from 'react';
import { FlatList, Text, View } from 'react-native';
import FastImage from 'react-native-fast-image';
import LinearGradient from 'react-native-linear-gradient';
import { useRecoilValue } from 'recoil';
import { Album } from '../../models/music';
import { albumsState, useCoverArtUri, useUpdateAlbums } from '../../state/albums';
import colors from '../../styles/colors';
import textStyles from '../../styles/text';
import TopTabContainer from '../common/TopTabContainer';

const AlbumArt: React.FC<{ height: number, width: number, id?: string }> = ({ height, width, id }) => {
  const coverArtUri = useCoverArtUri(id);

  const Placeholder = (
    <LinearGradient
      colors={[colors.accent, colors.accentLow]}
      style={{ height, width }}
    >
      <FastImage
        source={require('../../../res/record-m.png')}
        style={{ height, width }}
        resizeMode={FastImage.resizeMode.contain}
      />
    </LinearGradient>
  );

  const CoverArt = (
    <View style={{ height, width }}>
      <FastImage
        source={{ uri: coverArtUri }}
        style={{ height, width }}
        resizeMode={FastImage.resizeMode.contain}
      />
    </View>
  );

  return coverArtUri ? CoverArt : Placeholder;
}
const MemoAlbumArt = React.memo(AlbumArt);

const AlbumItem: React.FC<{ name: string, coverArt?: string } > = ({ name, coverArt }) => {
  const size = 125;

  return (
    <View style={{
      // flexDirection: 'row',
      alignItems: 'center',
      marginVertical: 8,
      // marginLeft: 6,
      // width: size,
      flex: 1/3,
    }}>
      <MemoAlbumArt
        width={size}
        height={size}
        id={coverArt}
      />
      <View style={{
        flex: 1,
        width: size,
        // alignItems: 'baseline',
      }}>
        <Text
          style={{
            ...textStyles.itemTitle,
            marginTop: 4,
          }}
          numberOfLines={2}
        >
          {name}
        </Text>
        <Text
          style={{
            ...textStyles.itemSubtitle,
            // marginTop: 2,
          }}
          numberOfLines={1}
        >
          {name}
        </Text>
      </View>
    </View>
  );
}
const MemoAlbumItem = React.memo(AlbumItem);

const AlbumListRenderItem: React.FC<{ item: Album }> = ({ item }) => (
  <MemoAlbumItem name={item.name} coverArt={item.coverArt} />
);

const AlbumsList = () => {
  const albums = useRecoilValue(albumsState);
  const updateAlbums = useUpdateAlbums();

  const [refreshing, setRefreshing] = useState(false);

  const refresh = async () => {
    setRefreshing(true);
    await updateAlbums();
    setRefreshing(false);
  }

  useEffect(() => {
    if (!refreshing && Object.keys(albums).length === 0) {
      refresh();
    }
  });

  return (
    <View style={{ flex: 1 }}>
      <FlatList
        data={Object.values(albums)}
        renderItem={AlbumListRenderItem}
        keyExtractor={item => item.id}
        onRefresh={refresh}
        refreshing={refreshing}
        numColumns={3}
        removeClippedSubviews={true}
      />
    </View>
  );
}

const AlbumsTab = () => (
  <TopTabContainer>
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <AlbumsList />
    </React.Suspense>
  </TopTabContainer>
);

export default React.memo(AlbumsTab);
