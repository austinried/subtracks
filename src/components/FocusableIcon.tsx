import React from 'react';
import { Image, ImageSourcePropType } from 'react-native';
import colors from '../styles/colors';

export type FocusableIconProps = {
  focused: boolean;
  source: ImageSourcePropType;
  focusedSource?: ImageSourcePropType;
  width?: number;
  height?: number;
};

const FocusableIcon: React.FC<FocusableIconProps> = props => {
  props.focusedSource = props.focusedSource || props.source;
  props.width = props.width || 26;
  props.height = props.height || 26;

  return (
    <Image
      style={{
        height: props.height,
        width: props.width,
        tintColor: props.focused ? colors.text.primary : colors.text.secondary,
      }}
      source={props.focused ? props.focusedSource : props.source}
    />
  );
};

export default FocusableIcon;
