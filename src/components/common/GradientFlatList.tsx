import React from 'react';
import { FlatList, FlatListProps, useWindowDimensions } from 'react-native';
import GradientBackground from './GradientBackground';

function GradientFlatList<ItemT>(props: FlatListProps<ItemT>) {
  const layout = useWindowDimensions();

  return (
    <FlatList
      { ...props }
      ListHeaderComponent={() => <GradientBackground position='relative' />}
      ListHeaderComponentStyle={{
        marginBottom: -layout.height,
      }}
    />
  );
};

export default GradientFlatList;
