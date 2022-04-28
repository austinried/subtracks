/* eslint-disable no-dupe-class-members */
import { EmitterSubscription, NativeEventEmitter } from 'react-native'
import { TrackExt } from '@app/models/trackplayer'

class QueueEventEmitter extends NativeEventEmitter {
  addListener(eventType: 'set', listener: (event: { queue: TrackExt[] }) => void): EmitterSubscription
  addListener(eventType: string, listener: (event: any) => void, context?: Object): EmitterSubscription {
    return super.addListener(eventType, listener, context)
  }

  emit(eventType: 'set', event: { queue: TrackExt[] }): void
  emit(eventType: string, ...params: any[]): void {
    super.emit(eventType, ...params)
  }
}

const QueueEvents = new QueueEventEmitter()
export default QueueEvents
