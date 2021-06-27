import AsyncStorage from '@react-native-async-storage/async-storage';

export async function getItem(key: string): Promise<string | null> {
  try {
    return await AsyncStorage.getItem(key);
  } catch (e) {
    console.error(`getItem error (key: ${key})`, e);
    return null;
  }
}

export async function multiGet(keys: string[]): Promise<[string, string | null][]> {
  try {
    return await AsyncStorage.multiGet(keys);
  } catch (e) {
    console.error(`multiGet error`, e);
    return [];
  }
}

export async function setItem(key: string, item: string): Promise<void> {
  try {
    await AsyncStorage.setItem(key, item);
  } catch (e) {
    console.error(`setItem error (key: ${key})`, e);
  }
}

export async function multiSet(items: string[][]): Promise<void> {
  try {
    await AsyncStorage.multiSet(items);
  } catch (e) {
    console.error(`multiSet error`, e);
  }
}
