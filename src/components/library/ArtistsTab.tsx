import { useAtomValue } from 'jotai/utils';
import React, { useEffect } from 'react';
import { Image, Text, View } from 'react-native';
import { Artist } from '../../models/music';
import { artistsAtom, artistsUpdatingAtom, useUpdateArtists } from '../../state/music';
import textStyles from '../../styles/text';
import GradientFlatList from '../common/GradientFlatList';

const ArtistItem: React.FC<{ item: Artist } > = ({ item }) => (
  <View style={{
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 6,
    marginLeft: 6,
  }}>
    <Image
      source={item.coverArt ? { uri: 'https://reactnative.dev/img/tiny_logo.png'  } : require('../../../res/mic_on-fill.png')}
      style={{
        width: 56,
        height: 56,
      }}
    />
    <Text style={{
      ...textStyles.paragraph,
      marginLeft: 12,
    }}>{item.name}</Text>
  </View>
);

const ArtistsList = () => {
  const artists = useAtomValue(artistsAtom);
  const updating = useAtomValue(artistsUpdatingAtom);
  const updateArtists = useUpdateArtists();

  useEffect(() => {
    if (artists.length === 0) {
      updateArtists();
    }
  });

  const renderItem: React.FC<{ item: Artist }> = ({ item }) => (
    <ArtistItem item={item} />
  );

  return (
    <GradientFlatList
      data={artists}
      renderItem={renderItem}
      keyExtractor={item => item.id}
      onRefresh={updateArtists}
      refreshing={updating}
      overScrollMode='never'
    />
  );
}

const ArtistsTab = () => (
  <ArtistsList />
);

export default ArtistsTab;
