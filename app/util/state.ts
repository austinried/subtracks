import { ById } from '@app/models/state'
import merge from 'lodash.merge'

export function reduceById<T extends { id: string }>(collection: T[]): ById<T> {
  return collection.reduce((acc, value) => {
    acc[value.id] = value
    return acc
  }, {} as ById<T>)
}

export function mergeById<T extends { [id: string]: unknown }>(object: T, source: T): void {
  merge(object, source)
}

export function mapById<T>(object: ById<T>, ids: string[]): T[] {
  return ids.map(id => object[id]).filter(a => a !== undefined)
}

export function mapId(entities: { id: string }[]): string[] {
  return entities.map(e => e.id)
}
