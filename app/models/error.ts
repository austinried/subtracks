export class NoClientError extends Error {
  constructor() {
    super('no client in state')
  }
}
