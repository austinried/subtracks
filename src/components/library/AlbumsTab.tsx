import React, { memo, useEffect, useState } from 'react';
import { View, Image, Text, FlatList, Button, ListRenderItem, ScrollView } from 'react-native';
import { useRecoilState, useRecoilValue } from 'recoil';
import { Album } from '../../models/music';
import { albumsState, albumState, useUpdateAlbums, albumIdsState, useCoverArtUri } from '../../state/albums';
import TopTabContainer from '../common/TopTabContainer';
import textStyles from '../../styles/text';
import colors from '../../styles/colors';
import LinearGradient from 'react-native-linear-gradient';
import FastImage from 'react-native-fast-image';
import RNFS from 'react-native-fs';

const AlbumArt: React.FC<{ height: number, width: number, id?: string }> = ({ height, width, id }) => {
  // useEffect(() => {
  //   console.log(id);
  // });

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
    <View style={{
      height, width,
    }}>
      <FastImage
        source={{ uri: `file://${RNFS.DocumentDirectoryPath}/image_cache/${id}` }}
        style={{ height, width }}
        resizeMode={FastImage.resizeMode.contain}
      />
    </View>
  );

  return id ? CoverArt : Placeholder;
}

const AlbumItem: React.FC<{ 
  name: string,
  coverArt?: string,
} > = ({ name, coverArt }) => {
  // useEffect(() => {
  //   console.log(name);
  // });

  const size = 125;

  return (
    <View style={{
      // flexDirection: 'row',
      alignItems: 'center',
      marginVertical: 8,
      // marginLeft: 6,
      // width: size,
      height: 180,
      flex: 1/3,
    }}>
      <AlbumArt
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

function renderItem(props: { item: Album }) {
  return <AlbumItem name={props.item.name} coverArt={props.item.coverArt} />;
}

const AlbumsList = () => {
  const albums = useRecoilValue(albumsState);
  const updateAlbums = useUpdateAlbums();

  const [refreshing, setRefreshing] = useState(false);

  const refresh = async () => {
    setRefreshing(true);
    await updateAlbums();
    setRefreshing(false);
  }

  console.log('rendering albums');

  useEffect(() => {
    console.log('mounting albums');
    if (!refreshing && Object.keys(albums).length === 0) {
      refresh();
    }
  });

  return (
    <View style={{ flex: 1 }}>
      <FlatList
        data={Object.values(albums)}
        renderItem={renderItem}
        keyExtractor={item => item.id}
        onRefresh={refresh}
        refreshing={refreshing}
        numColumns={3}
        removeClippedSubviews={true}
        // initialNumToRender={3}
        // maxToRenderPerBatch={2}
        // updateCellsBatchingPeriod={1000}
        getItemLayout={(data, index) => ({
            length: 180,
            offset: 180 * Math.floor(index / 3),
            index
        })}
      />
      {/* <ScrollView>
        {Object.values(albums).map(album => (
          <AlbumItem name={album.name} coverArt={album.coverArt} key={album.id} />
        ))}
      </ScrollView> */}
    </View>
  );
}

const MemoAlbumsList = React.memo(AlbumsList);

const AlbumsTab = () => (
  <TopTabContainer>
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <MemoAlbumsList />
    </React.Suspense>
  </TopTabContainer>
);

export default AlbumsTab;
