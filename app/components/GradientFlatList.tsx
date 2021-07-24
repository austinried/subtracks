import React from 'react'
import { FlatList, FlatListProps, StyleSheet, useWindowDimensions } from 'react-native'
import colors from '@app/styles/colors'
import GradientBackground from '@app/components/GradientBackground'

function GradientFlatList<ItemT>(props: FlatListProps<ItemT>) {
  const layout = useWindowDimensions()

  const contentContainerStyle = StyleSheet.flatten(props.contentContainerStyle)

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
        marginHorizontal: -(contentContainerStyle.paddingHorizontal || 0),
        top: -(contentContainerStyle.paddingTop || 0),
      }}
    />
  )
}

export default GradientFlatList
