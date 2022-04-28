import React from 'react'
import GradientBackgroundHeaderFlatList, {
  GradientBackgroundHeaderFlatListPropsBase,
} from './GradientBackgroundHeaderFlatList'
import GradientImageBackground, { GradientImageBackgroundProps } from './GradientImageBackground'

export type GradientImageFlatListProps<ItemT> = GradientBackgroundHeaderFlatListPropsBase<ItemT> & {
  backgroundProps?: GradientImageBackgroundProps
}

function GradientImageFlatList<ItemT>(props: GradientImageFlatListProps<ItemT>) {
  return <GradientBackgroundHeaderFlatList BackgroundComponent={GradientImageBackground} {...props} />
}

export default GradientImageFlatList
