import React, { useState } from 'react';
import { GestureResponderEvent, Pressable, Text } from 'react-native';
import colors from '../../styles/colors';
import text from '../../styles/text';

const Button: React.FC<{
  title: string;
  onPress: (event: GestureResponderEvent) => void;
}> = ({ title, onPress }) => {
  const [opacity, setOpacity] = useState(1);

  return (
    <Pressable
      onPress={onPress}
      onPressIn={() => setOpacity(0.6)}
      onPressOut={() => setOpacity(1)}
      onLongPress={() => setOpacity(1)}
      style={{
        backgroundColor: colors.accent,
        paddingHorizontal: 24,
        minHeight: 42,
        justifyContent: 'center',
        borderRadius: 1000,
        opacity,
      }}
    >
      <Text style={{ ...text.button }}>{title}</Text>
    </Pressable>
  );
}

export default Button;
