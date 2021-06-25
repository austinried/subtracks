import React, { memo, useEffect, useState } from 'react';
import { View, Image, Text, FlatList, Button, ListRenderItem } from 'react-native';
import { useRecoilState, useRecoilValue } from 'recoil';
import { Album } from '../../models/music';
import { albumsState, albumState, useUpdateAlbums, albumIdsState, useCoverArtUri } from '../../state/albums';
import TopTabContainer from '../common/TopTabContainer';
import textStyles from '../../styles/text';
import { ScrollView } from 'react-native-gesture-handler';
import colors from '../../styles/colors';
import LinearGradient from 'react-native-linear-gradient';

const AlbumArt: React.FC<{ height: number, width: number, id?: string }> = ({ height, width, id }) => {
  const coverArtSource = useCoverArtUri(id);

  // useEffect(() => {
  //   console.log(id);
  // });

  const Placeholder = (
    <LinearGradient
      colors={[colors.accent, colors.accentLow]}
      style={{
        height, width,
      }}
    >
      <Image
        source={require('../../../res/record-m.png')}
        style={{
          height, width,
          resizeMode: 'contain',
        }}
      />
    </LinearGradient>
  );

  const CoverArt = (
    <View style={{
      height, width,
    }}>
      <Image
        source={{ uri: coverArtSource }}
        style={{
          height, width,
          resizeMode: 'contain',
        }}
      />
    </View>
  );

  return coverArtSource ? CoverArt : Placeholder;
}

const AlbumItem: React.FC<{ id: string } > = ({ id }) => {
  const album = useRecoilValue(albumState(id));
  
  // useEffect(() => {
  //   console.log(album.name);
  // });

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
      <AlbumArt
        width={size}
        height={size}
        id={album.coverArt}
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
          {album.name}
        </Text>
        <Text
          style={{
            ...textStyles.itemSubtitle,
            // marginTop: 2,
          }}
          numberOfLines={1}
        >
          {album.name}
        </Text>
      </View>
    </View>
  );
}

const MemoAlbumItem = memo(AlbumItem, (prev, next) => {
  // console.log('prev: ' + JSON.stringify(prev) + ' next: ' + JSON.stringify(next))
  return prev.id == next.id;
});

const AlbumsList = () => {
  const albumIds = useRecoilValue(albumIdsState);
  const updateAlbums = useUpdateAlbums();

  const [refreshing, setRefreshing] = useState(false);

  const renderItem: React.FC<{ item: string }> = ({ item }) => (
    <MemoAlbumItem id={item} />
  );

  const refresh = async () => {
    setRefreshing(true);
    await updateAlbums();
    setRefreshing(false);
  }

  useEffect(() => {
    if (!refreshing && albumIds.length === 0) {
      refresh();
    }
  });

  return (
    <View style={{ flex: 1 }}>
      <FlatList
        data={albumIds}
        renderItem={renderItem}
        keyExtractor={item => item}
        onRefresh={refresh}
        refreshing={refreshing}
        numColumns={3}
        removeClippedSubviews={false}
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

export default AlbumsTab;
