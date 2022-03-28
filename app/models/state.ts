export interface ById<T> {
  [id: string]: T
}

export type OneToMany = ById<string[]>

export interface OrderedById<T> {
  byId: ById<T>
  allIds: string[]
}

export interface PaginatedList {
  [offset: number]: string[]
}
