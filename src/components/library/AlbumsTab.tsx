import { useNavigation } from '@react-navigation/native';
import { useAtomValue } from 'jotai/utils';
import React, { useEffect } from 'react';
import { Pressable, Text, View } from 'react-native';
import { Album } from '../../models/music';
import { albumsAtom, albumsUpdatingAtom, useUpdateAlbums } from '../../state/music';
import textStyles from '../../styles/text';
import AlbumArt from '../common/AlbumArt';
import GradientFlatList from '../common/GradientFlatList';

const AlbumItem: React.FC<{
  id: string;
  name: string;
  artist?: string;
}> = ({ id, name, artist }) => {
  const navigation = useNavigation();

  const size = 125;

  return (
    <Pressable
      style={{
        alignItems: 'center',
        marginVertical: 8,
        flex: 1 / 3,
      }}
      onPress={() => navigation.navigate('AlbumView', { id, title: name })}>
      <AlbumArt id={id} height={size} width={size} />
      <View
        style={{
          flex: 1,
          width: size,
        }}>
        <Text
          style={{
            ...textStyles.itemTitle,
            marginTop: 4,
          }}
          numberOfLines={2}>
          {name}
        </Text>
        <Text style={{ ...textStyles.itemSubtitle }} numberOfLines={1}>
          {artist}
        </Text>
      </View>
    </Pressable>
  );
};
const MemoAlbumItem = React.memo(AlbumItem);

const AlbumListRenderItem: React.FC<{ item: Album }> = ({ item }) => (
  <MemoAlbumItem id={item.id} name={item.name} artist={item.artist} />
);

const AlbumsList = () => {
  const albums = useAtomValue(albumsAtom);
  const updating = useAtomValue(albumsUpdatingAtom);
  const updateAlbums = useUpdateAlbums();

  const albumsList = Object.values(albums);

  useEffect(() => {
    if (albumsList.length === 0) {
      updateAlbums();
    }
  });

  return (
    <View style={{ flex: 1 }}>
      <GradientFlatList
        data={albumsList}
        renderItem={AlbumListRenderItem}
        keyExtractor={item => item.id}
        numColumns={3}
        removeClippedSubviews={true}
        refreshing={updating}
        onRefresh={updateAlbums}
        overScrollMode="never"
      />
    </View>
  );
};

const AlbumsTab = () => (
  <React.Suspense fallback={<Text>Loading...</Text>}>
    <AlbumsList />
  </React.Suspense>
);

export default React.memo(AlbumsTab);
