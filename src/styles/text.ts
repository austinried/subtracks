import { TextStyle } from "react-native";
import colors from './colors';

const paragraph: TextStyle = {
  fontFamily: 'Metropolis-Regular',
  fontSize: 16,
  color: colors.text.primary,
};

const header: TextStyle = {
  ...paragraph,
  fontSize: 20,
  fontFamily: 'Metropolis-SemiBold',
};

const itemTitle: TextStyle = {
  ...paragraph,
  fontSize: 13,
  fontFamily: 'Metropolis-SemiBold',
};

const itemSubtitle: TextStyle = {
  ...paragraph,
  fontSize: 12,
  color: colors.text.secondary,
};

const xsmall: TextStyle = {
  ...paragraph,
  fontSize: 10,
};

export default {
  paragraph,
  header,
  itemTitle,
  itemSubtitle,
  xsmall
};
