import { TextStyle } from 'react-native';
import colors from './colors';

const fontRegular = 'Metropolis-Regular';
const fontSemiBold = 'Metropolis-SemiBold';
const fontBold = 'Metropolis-Bold';

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

const title: TextStyle = {
  ...paragraph,
  fontSize: 24,
  fontFamily: fontBold,
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

const songListTitle: TextStyle = {
  ...paragraph,
  fontSize: 16,
  fontFamily: fontSemiBold,
};

const songListSubtitle: TextStyle = {
  ...paragraph,
  fontSize: 14,
  color: colors.text.secondary,
};

const xsmall: TextStyle = {
  ...paragraph,
  fontSize: 10,
};

const button: TextStyle = {
  ...paragraph,
  fontSize: 15,
  fontFamily: fontBold,
};

export default {
  paragraph,
  header,
  title,
  itemTitle,
  itemSubtitle,
  songListTitle,
  songListSubtitle,
  xsmall,
  button,
};
