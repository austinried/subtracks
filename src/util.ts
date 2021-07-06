export function formatDuration(seconds: number): string {
  const s = Math.floor(seconds) % 60
  const m = Math.floor(seconds / 60) % 60
  const h = Math.floor(seconds / 60 / 60)

  let time = `${m.toString().padStart(1, '0')}:${s.toString().padStart(2, '0')}`
  if (h > 0) {
    time = `${h}:${time}`
  }
  return time
}

type QueuedPromise = () => Promise<any>

export class PromiseQueue {
  maxSimultaneously: number

  private active = 0
  private queue: QueuedPromise[] = []

  constructor(maxSimultaneously = 1) {
    this.maxSimultaneously = maxSimultaneously
  }

  async enqueue<T>(func: () => Promise<T>) {
    if (++this.active > this.maxSimultaneously) {
      await new Promise(resolve => this.queue.push(resolve as QueuedPromise))
    }

    try {
      return await func()
    } catch (err) {
      throw err
    } finally {
      this.active--
      if (this.queue.length) {
        this.queue.shift()?.()
      }
    }
  }
}
