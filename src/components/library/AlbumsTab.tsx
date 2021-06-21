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

  return (
    <LinearGradient
      colors={[colors.accent, colors.accentLow]}
      style={{
        height, width,
        backgroundColor: 'white',
      }}
    >
      <Image
        source={coverArtSource ? { uri: coverArtSource } : require('../../../res/record-m.png')}
        style={{
          height, width,
        }}
      />
    </LinearGradient>
  )
}

const AlbumItem: React.FC<{ id: string } > = ({ id }) => {
  const album = useRecoilValue(albumState(id));
  
  // useEffect(() => {
  //   console.log(album.name);
  // });

  return (
    <View style={{
      flexDirection: 'row',
      alignItems: 'center',
      marginVertical: 6,
      marginLeft: 6,
      // height: 200,
    }}>
      <AlbumArt
        width={56}
        height={56}
        id={album.coverArt}
      />
      <Text style={{
        ...textStyles.paragraph,
        marginLeft: 12,
      }}>{album.name}</Text>
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
  })

  return (
    <View style={{ flex: 1 }}>
      {/* <Button
        title='Update'
        onPress={updateAlbums}
      /> */}
      <FlatList
        data={albumIds}
        renderItem={renderItem}
        keyExtractor={item => item}
        onRefresh={refresh}
        refreshing={refreshing}
      />
      {/* <ScrollView>
        {Object.values(albums).map(item => (
          <AlbumItem item={item} key={item.id} />
        ))}
      </ScrollView> */}
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
