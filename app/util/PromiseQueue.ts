type QueuedPromise = () => Promise<any>

class PromiseQueue {
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

export default PromiseQueue
