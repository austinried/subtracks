import { atomWithStorage } from 'jotai/utils';
import { getItem, setItem } from './asyncstorage';

export default <T>(key: string, defaultValue: T) => {
  return atomWithStorage<T>(key, defaultValue, {
    getItem: async () => (await getItem(key)) || defaultValue,
    setItem: setItem,
    delayInit: true,
  });
};
