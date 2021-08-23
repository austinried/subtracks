import { ToastAndroid } from 'react-native'

function toast(message: string) {
  ToastAndroid.showWithGravityAndOffset(message, ToastAndroid.SHORT, ToastAndroid.BOTTOM, 0, 180)
}

export default toast
