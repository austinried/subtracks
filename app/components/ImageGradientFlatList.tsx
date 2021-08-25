import React from 'react'
import BackgroundHeaderFlatList, { BackgroundHeaderFlatListPropsBase } from './BackgroundHeaderFlatList'
import ImageGradientBackground, { ImageGradientBackgroundProps } from './ImageGradientBackground'

export type ImageGradientFlatListProps<ItemT> = BackgroundHeaderFlatListPropsBase<ItemT> & {
  backgroundProps?: ImageGradientBackgroundProps
}

function ImageGradientFlatList<ItemT>(props: ImageGradientFlatListProps<ItemT>) {
  return <BackgroundHeaderFlatList BackgroundComponent={ImageGradientBackground} {...props} />
}

export default ImageGradientFlatList
