import TrackPlayer, { Event } from 'react-native-track-player';

module.exports = async function () {
  TrackPlayer.addEventListener(Event.RemotePlay, () => TrackPlayer.play());
  TrackPlayer.addEventListener(Event.RemotePause, () => TrackPlayer.pause());
  TrackPlayer.addEventListener(Event.RemoteStop, () => TrackPlayer.destroy());

  TrackPlayer.addEventListener(Event.RemoteDuck, data => {
    if (data.permanent) {
      TrackPlayer.stop();
      return;
    }

    if (data.paused) {
      TrackPlayer.pause();
    } else {
      TrackPlayer.play();
    }
  });

  TrackPlayer.addEventListener(Event.RemoteNext, () => TrackPlayer.skipToNext().catch(() => {}));
  TrackPlayer.addEventListener(Event.RemotePrevious, () => TrackPlayer.skipToPrevious().catch(() => {}));
};
