import TrackPlayer from 'react-native-track-player';

module.exports = async function() {
  TrackPlayer.addEventListener('remote-play', () => TrackPlayer.play());
  TrackPlayer.addEventListener('remote-pause', () => TrackPlayer.pause());
  TrackPlayer.addEventListener('remote-stop', () => TrackPlayer.destroy());

  TrackPlayer.addEventListener('remote-duck', (data) => {
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
  
  TrackPlayer.addEventListener('remote-next', () => TrackPlayer.skipToNext().catch(() => {}));
  TrackPlayer.addEventListener('remote-previous', () => TrackPlayer.skipToPrevious().catch(() => {}));
};
