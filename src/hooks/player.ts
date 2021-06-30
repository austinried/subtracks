import { useState } from "react";
import TrackPlayer, { STATE_NONE, STATE_STOPPED, Track, TrackPlayerEvents, useTrackPlayerEvents } from "react-native-track-player";
import { Song } from "../models/music";

function mapSongToTrack(song: Song): Track {
  return {
    id: song.id,
    title: song.title,
    artist: song.artist || 'Unknown Artist',
    url: song.streamUri,
    artwork: song.coverArtUri,
    duration: song.duration,
  }
}

const currentTrackEvents = [
  TrackPlayerEvents.PLAYBACK_STATE,
  TrackPlayerEvents.PLAYBACK_TRACK_CHANGED,
  TrackPlayerEvents.REMOTE_STOP,
]

export const useCurrentTrackId = () => {
  const [currentTrackId, setCurrentTrackId] = useState<string | null>(null);

  useTrackPlayerEvents(currentTrackEvents, async (event) => {
    switch (event.type) {
      case TrackPlayerEvents.PLAYBACK_STATE:
        switch (event.state) {
          case STATE_NONE:
          case STATE_STOPPED:
            setCurrentTrackId(null);
            break;
        }
        break;
      case TrackPlayerEvents.PLAYBACK_TRACK_CHANGED:
        setCurrentTrackId(await TrackPlayer.getCurrentTrack());
        break;
      case TrackPlayerEvents.REMOTE_STOP:
        setCurrentTrackId(null);
        break;
      default:
        break;
    }
  });

  return currentTrackId;
}

export const useSetQueue = () => {
  return async (songs: Song[], playId?: string) => {
    await TrackPlayer.reset();
    const tracks = songs.map(mapSongToTrack);

    if (!playId) {
      await TrackPlayer.add(tracks);
    } else if (playId === tracks[0].id) {
      await TrackPlayer.add(tracks);
      await TrackPlayer.play();
    } else {
      const playIndex = tracks.findIndex(t => t.id === playId);
      const tracks1 = tracks.slice(0, playIndex);
      const tracks2 = tracks.slice(playIndex);

      console.log('tracks1: ' + JSON.stringify(tracks1.map(t => t.title)));
      console.log('tracks2: ' + JSON.stringify(tracks2.map(t => t.title)));

      await TrackPlayer.add(tracks2);
      await TrackPlayer.play();

      await TrackPlayer.add(tracks1, playId);

      const queue = await TrackPlayer.getQueue();
      console.log('queue: ' + JSON.stringify(queue.map(t => t.title)));
    }
  }
}
