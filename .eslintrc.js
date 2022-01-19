module.exports = {
  root: true,
  ignorePatterns: ['submodules/**', 'ndk/**'],
  extends: '@react-native-community',
  rules: {
    '@typescript-eslint/no-unused-vars': ['warn'],
    semi: 0,
    'no-spaced-func': 0,
  },
}
