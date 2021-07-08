import React from 'react'
import { FlatList, FlatListProps, useWindowDimensions } from 'react-native'
import colors from '@app/styles/colors'
import GradientBackground from '@app/components/GradientBackground'

function GradientFlatList<ItemT>(props: FlatListProps<ItemT>) {
  const layout = useWindowDimensions()

  return (
    <FlatList
      {...props}
      style={{
        ...(props.style as any),
        backgroundColor: colors.gradient.low,
      }}
      ListHeaderComponent={() => <GradientBackground position="relative" />}
      ListHeaderComponentStyle={{
        marginBottom: -layout.height,
      }}
    />
  )
}

export default GradientFlatList
