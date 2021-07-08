module.exports = {
  presets: ['module:metro-react-native-babel-preset'],
  plugins: [
    [
      require.resolve('babel-plugin-module-resolver'),
      {
        cwd: 'babelrc',
        extensions: ['.ts', '.tsx', '.js', '.ios.js', '.android.js'],
        alias: {
          '@app': './app',
        },
      },
    ],
    // reanimated has to be listed last in plugins
    'react-native-reanimated/plugin',
  ],
}
