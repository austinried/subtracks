import React from 'react';
import { Button, FlatList, Text, View } from 'react-native';
import { useRecoilValue, useResetRecoilState, useSetRecoilState } from 'recoil';
import { Artist } from '../models/music';
import { artistsState, isLibraryRefreshingState, libraryRefreshState } from '../state/music';

const ArtistItem: React.FC<{ item: Artist } > = ({ item }) => (
  <View>
    <Text>{item.id}</Text>
    <Text style={{
      fontSize: 60,
      paddingBottom: 400,
    }}>{item.name}</Text>
  </View>
);

const List = () => {
  const artists = useRecoilValue(artistsState);

  const renderItem: React.FC<{ item: Artist }> = ({ item }) => (
    <ArtistItem item={item} />
  );

  return (
    <FlatList
      data={artists}
      renderItem={renderItem}
      keyExtractor={item => item.id}
    />
  );
}

const ListPlusControls = () => {
  const resetArtists = useResetRecoilState(artistsState);
  const setLibraryRefresh = useSetRecoilState(libraryRefreshState);
  const isLibraryRefreshing = useRecoilValue(isLibraryRefreshingState);

  return (
    <View>
        <Button 
          title='Reset to default'
          onPress={resetArtists}
        />
        <Button 
          title='Refresh Library'
          onPress={() => setLibraryRefresh(true)}
          disabled={isLibraryRefreshing}
        />
      <List />
    </View>
  );
}

const ArtistsList = () => (
  <View>
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <ListPlusControls />
    </React.Suspense>
  </View>
)

export default ArtistsList;
