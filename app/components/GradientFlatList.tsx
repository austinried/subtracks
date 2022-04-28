import GradientBackground, { GradientBackgroundProps } from '@app/components/GradientBackground'
import React from 'react'
import GradientBackgroundHeaderFlatList, {
  GradientBackgroundHeaderFlatListPropsBase,
} from './GradientBackgroundHeaderFlatList'

export type GradientFlatListProps<ItemT> = GradientBackgroundHeaderFlatListPropsBase<ItemT> & {
  backgroundProps?: GradientBackgroundProps
}

function GradientFlatList<ItemT>(props: GradientFlatListProps<ItemT>) {
  return <GradientBackgroundHeaderFlatList BackgroundComponent={GradientBackground} {...props} />
}

export default GradientFlatList
