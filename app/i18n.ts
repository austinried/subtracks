import { BackendModule, LanguageDetectorAsyncModule } from 'i18next'
import path from 'path'
import RNFS from 'react-native-fs'
import * as RNLocalize from 'react-native-localize'
import _ from 'lodash'

const I18N_ASSETS_DIR = path.join('custom', 'i18n')

const cache: {
  [language: string]: {
    [key: string]: any
  }
} = {}

async function loadTranslation(language: string) {
  const text = await RNFS.readFileAssets(path.join(I18N_ASSETS_DIR, `${language}.json`), 'utf8')
  return JSON.parse(text)
}

async function readTranslation(language: string, namespace: string) {
  if (!cache[language]) {
    cache[language] = await loadTranslation(language)
  }

  return namespace === 'translation' ? cache[language] : _.get(cache[language], namespace)
}

export const backend = {
  type: 'backend',
  init: () => {},
  read: async (language, namespace, callback) => {
    try {
      callback(null, await readTranslation(language, namespace))
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
