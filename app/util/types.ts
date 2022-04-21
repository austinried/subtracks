export type PromiseResolvedType<T> = T extends Promise<infer R> ? R : never
export type ReturnedPromiseResolvedType<T extends (...args: any) => any> = PromiseResolvedType<ReturnType<T>>
