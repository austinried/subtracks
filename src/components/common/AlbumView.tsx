import { useNavigation } from '@react-navigation/native';
import { useAtomValue } from 'jotai/utils';
import React, { useEffect } from 'react';
import { View, Text } from 'react-native';
import { albumAtomFamily } from '../../state/music';
import TopTabContainer from './TopTabContainer';

const AlbumDetails: React.FC<{
  id: string,
}> = ({ id }) => {
  const navigation = useNavigation();
  const album = useAtomValue(albumAtomFamily(id));

  useEffect(() => {
    if (!album) {
      return;
    }
    navigation.setOptions({ title: album.name });
  });

  return (
    <>
      <Text>Name: {album?.name}</Text>
      <Text>Artist: {album?.artist}</Text>
    </>
  );
}

const AlbumView: React.FC<{
  id: string,
}> = ({ id }) => (
  <TopTabContainer>
    <Text>{id}</Text>
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <AlbumDetails id={id} />
    </React.Suspense>
  </TopTabContainer>
);

export default React.memo(AlbumView);
