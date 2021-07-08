import { TextStyle } from 'react-native'
import colors from '@app/styles/colors'

export enum Font {
  regular = 'Metropolis-Regular',
  semiBold = 'Metropolis-SemiBold',
  bold = 'Metropolis-Bold',
}

const paragraph: TextStyle = {
  fontFamily: Font.regular,
  fontSize: 16,
  color: colors.text.primary,
}

const header: TextStyle = {
  ...paragraph,
  fontSize: 18,
  fontFamily: Font.semiBold,
}

const title: TextStyle = {
  ...paragraph,
  fontSize: 24,
  fontFamily: Font.bold,
}

const itemTitle: TextStyle = {
  ...paragraph,
  fontSize: 13,
  fontFamily: Font.semiBold,
}

const itemSubtitle: TextStyle = {
  ...paragraph,
  fontSize: 12,
  color: colors.text.secondary,
}

const songListTitle: TextStyle = {
  ...paragraph,
  fontSize: 16,
  fontFamily: Font.semiBold,
}

const songListSubtitle: TextStyle = {
  ...paragraph,
  fontSize: 14,
  color: colors.text.secondary,
}

const xsmall: TextStyle = {
  ...paragraph,
  fontSize: 10,
}

const button: TextStyle = {
  ...paragraph,
  fontSize: 15,
  fontFamily: Font.bold,
}

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
}
