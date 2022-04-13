import { BackendModule, LanguageDetectorAsyncModule } from 'i18next'
import path from 'path'
import RNFS from 'react-native-fs'
import * as RNLocalize from 'react-native-localize'

const I18N_ASSETS_DIR = path.join('custom', 'i18n')

let cache: {
  language: string
  translation: {
    [key: string]: any
  }
}

export const backend = {
  type: 'backend',
  init: () => {},
  read: async (language, namespace, callback) => {
    try {
      if (cache && cache.language === language) {
        callback(null, cache.translation[namespace])
      }

      const text = await RNFS.readFileAssets(path.join(I18N_ASSETS_DIR, `${language}.json`), 'utf8')
      cache = {
        language,
        translation: JSON.parse(text),
      }

      callback(null, cache.translation[namespace])
    } catch (err) {
      callback(err as any, null)
    }
  },
} as BackendModule

export const languageDetector = {
  type: 'languageDetector',
  async: true,
  detect: async callback => {
    try {
      const languageTags = (await RNFS.readDirAssets(I18N_ASSETS_DIR))
        .map(f => f.name)
        .filter(n => n.endsWith('.json'))
        .map(n => n.slice(0, -5))

      console.log('translations available:', languageTags)
      console.log(
        'locales list:',
        RNLocalize.getLocales().map(l => l.languageTag),
      )
      console.log('best language:', RNLocalize.findBestAvailableLanguage(languageTags)?.languageTag)

      callback(RNLocalize.findBestAvailableLanguage(languageTags)?.languageTag)
    } catch {
      callback(undefined)
    }
  },
  init: () => {},
  cacheUserLanguage: () => {},
} as LanguageDetectorAsyncModule
