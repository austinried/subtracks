import { StatusBar } from 'react-native'

const header = 56
const tabBar = 54

const top = () => header + (StatusBar.currentHeight || 0)
const bottom = () => tabBar

export default {
  header,
  tabBar,
  top,
  bottom,
}
