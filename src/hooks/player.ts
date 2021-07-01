import { useState } from "react";
import TrackPlayer, { Track, useTrackPlayerEvents, Event, State } from "react-native-track-player";
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
  Event.PlaybackState,
  Event.PlaybackTrackChanged,
  Event.RemoteStop,
]

export const useCurrentTrackId = () => {
  const [currentTrackId, setCurrentTrackId] = useState<string | null>(null);

  useTrackPlayerEvents(currentTrackEvents, async (event) => {
    switch (event.type) {
      case Event.PlaybackState:
        switch (event.state) {
          case State.None:
          case State.Stopped:
            setCurrentTrackId(null);
            break;
        }
        break;
      case Event.PlaybackTrackChanged:
        const trackIndex = await TrackPlayer.getCurrentTrack()
        setCurrentTrackId((await TrackPlayer.getTrack(trackIndex)).id);
        break;
      case Event.RemoteStop:
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

      await TrackPlayer.add(tracks2);
      await TrackPlayer.play();

      await TrackPlayer.add(tracks1, 0);

      const queue = await TrackPlayer.getQueue();
      console.log(`queue: ${JSON.stringify(queue.map(x => x.title))}`);
    }
  }
}
