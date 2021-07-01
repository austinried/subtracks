import AsyncStorage from '@react-native-async-storage/async-storage';

export async function getItem(key: string): Promise<any | null> {
  try {
    const item = await AsyncStorage.getItem(key);
    return item ? JSON.parse(item) : null;
  } catch (e) {
    console.error(`getItem error (key: ${key})`, e);
    return null;
  }
}

export async function multiGet(keys: string[]): Promise<[string, any | null][]> {
  try {
    const items = await AsyncStorage.multiGet(keys);
    return items.map(x => [x[0], x[1] ? JSON.parse(x[1]) : null]);
  } catch (e) {
    console.error('multiGet error', e);
    return [];
  }
}

export async function setItem(key: string, item: any): Promise<void> {
  try {
    await AsyncStorage.setItem(key, JSON.stringify(item));
  } catch (e) {
    console.error(`setItem error (key: ${key})`, e);
  }
}

export async function multiSet(items: string[][]): Promise<void> {
  try {
    await AsyncStorage.multiSet(items.map(x => [x[0], JSON.stringify(x[1])]));
  } catch (e) {
    console.error('multiSet error', e);
  }
}

export async function getAllKeys(): Promise<string[]> {
  try {
    return await AsyncStorage.getAllKeys();
  } catch (e) {
    console.error('getAllKeys error', e);
    return [];
  }
}

export async function multiRemove(keys: string[]): Promise<void> {
  try {
    await AsyncStorage.multiRemove(keys);
  } catch (e) {
    console.error('multiRemove error', e);
  }
}
