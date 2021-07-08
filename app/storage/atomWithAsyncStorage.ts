import { atomWithStorage } from 'jotai/utils'
import { getItem, setItem } from '@app/storage/asyncstorage'

export default <T>(key: string, defaultValue: T) => {
  return atomWithStorage<T>(key, defaultValue, {
    getItem: async () => (await getItem(key)) || defaultValue,
    setItem: setItem,
    delayInit: true,
  })
}
