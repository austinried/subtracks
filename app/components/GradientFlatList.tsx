import GradientBackground, { GradientBackgroundProps } from '@app/components/GradientBackground'
import React from 'react'
import BackgroundHeaderFlatList, { BackgroundHeaderFlatListPropsBase } from './BackgroundHeaderFlatList'

export type GradientFlatListProps<ItemT> = BackgroundHeaderFlatListPropsBase<ItemT> & {
  backgroundProps?: GradientBackgroundProps
}

function GradientFlatList<ItemT>(props: GradientFlatListProps<ItemT>) {
  return <BackgroundHeaderFlatList BackgroundComponent={GradientBackground} {...props} />
}

export default GradientFlatList
