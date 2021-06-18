import React from 'react';
import { Image, ImageSourcePropType } from 'react-native';

export type FocusableIconProps = {
  focused: boolean,
  source: ImageSourcePropType;
  focusedSource?: ImageSourcePropType;
  width?: number;
  height?: number;
};

const FocusableIcon: React.FC<FocusableIconProps> = (props) => {
  props.focusedSource = props.focusedSource || props.source;
  props.width = props.width || 32;
  props.height = props.height || 32;

  return (
    <Image
      style={{ height: props.height, width: props.width }}
      source={props.focused ? props.focusedSource : props.source}
    />
  );
}

export default FocusableIcon;
