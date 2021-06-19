import { TextStyle } from "react-native";
import colors from './colors';

const paragraph: TextStyle = {
  fontFamily: 'Ubuntu-Light',
  fontSize: 16,
  color: colors.text.primary,
};

const header: TextStyle = {
  ...paragraph,
  fontSize: 18,
};

const small: TextStyle = {
  ...paragraph,
  fontSize: 11,
};

export default {
  paragraph,
  header,
  small,
};
