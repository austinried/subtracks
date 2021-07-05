module.exports = {
  presets: ['module:metro-react-native-babel-preset'],
  plugins: [
    // reanimated has to be listed last in plugins
    'react-native-reanimated/plugin',
  ],
}
