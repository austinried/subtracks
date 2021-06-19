import { StyleSheet, TextStyle } from "react-native";

const regular: TextStyle = {
  fontFamily: 'Ubuntu-Light',
  fontSize: 18,
  color: '#fff',
};

const header: TextStyle = {
  ...regular,
  fontSize: 22,
};

const small: TextStyle = {
  ...regular,
  fontSize: 11,
};

export type TextStyles = {
  [key: string]: TextStyle,
}

export default {
  regular,
  header,
  small,
};
