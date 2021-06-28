import { TextStyle } from "react-native";
import colors from './colors';

const fontRegular = 'Metropolis-Regular';
const fontSemiBold = 'Metropolis-SemiBold';

const paragraph: TextStyle = {
  fontFamily: fontRegular,
  fontSize: 16,
  color: colors.text.primary,
};

const header: TextStyle = {
  ...paragraph,
  fontSize: 19,
  fontFamily: fontSemiBold,
};

const itemTitle: TextStyle = {
  ...paragraph,
  fontSize: 13,
  fontFamily: fontSemiBold,
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
