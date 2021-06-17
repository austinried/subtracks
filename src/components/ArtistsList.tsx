import React from 'react';
import { Button, FlatList, Text, View } from 'react-native';
import { useRecoilValue, useResetRecoilState } from 'recoil';
import { artistsState, ArtistState, useUpdateArtists } from '../state/artists';

const ArtistItem: React.FC<{ item: ArtistState } > = ({ item }) => (
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

  const renderItem: React.FC<{ item: ArtistState }> = ({ item }) => (
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

const ArtistsList = () => {
  const resetArtists = useResetRecoilState(artistsState);
  const updateArtists = useUpdateArtists();

  return (
    <View>
        <Button 
          title='Reset to default'
          onPress={resetArtists}
        />
        <Button 
          title='Update from API'
          onPress={updateArtists}
        />
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <List />
    </React.Suspense>
    </View>
  );
}

export default ArtistsList;
