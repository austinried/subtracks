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

const itemTitle: TextStyle = {
  ...paragraph,
  fontSize: 14,
  // fontFamily: 'Ubuntu-Light',
};

const itemSubtitle: TextStyle = {
  ...paragraph,
  fontSize: 12,
  color: colors.text.secondary,
};

const xsmall: TextStyle = {
  ...paragraph,
  fontSize: 11,
};

export default {
  paragraph,
  header,
  itemTitle,
  itemSubtitle,
  xsmall
};
