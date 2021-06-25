import React from 'react';
import { Text, View, Image, FlatList } from 'react-native';
import { Artist } from '../../models/music';
import { useRecoilValue } from 'recoil';
import textStyles from '../../styles/text';
import TopTabContainer from '../common/TopTabContainer';
import { artistsState } from '../../state/music';

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

const ArtistsTab = () => (
  <TopTabContainer>
    <ArtistsList />
  </TopTabContainer>
);

export default ArtistsTab;
