export type OutlineFillIcon = {
  outline: number
  fill: number
}

export const bottomTabIcons: Record<string, OutlineFillIcon> = {
  home: {
    outline: require('@res/icons/home.png'),
    fill: require('@res/icons/home-fill.png'),
  },
  library: {
    outline: require('@res/icons/library.png'),
    fill: require('@res/icons/library-fill.png'),
  },
  search: {
    outline: require('@res/icons/search.png'),
    fill: require('@res/icons/search-fill.png'),
  },
  settings: {
    outline: require('@res/icons/settings.png'),
    fill: require('@res/icons/settings-fill.png'),
  },
}
