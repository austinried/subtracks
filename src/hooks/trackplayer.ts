import { useUpdateAtom } from 'jotai/utils';
import TrackPlayer, { Track } from 'react-native-track-player';
import { Song } from '../models/music';
import { currentQueueNameAtom, currentTrackAtom } from '../state/trackplayer';

function mapSongToTrack(song: Song, queueName: string): Track {
  return {
    id: song.id,
    queueName,
    title: song.title,
    artist: song.artist || 'Unknown Artist',
    url: song.streamUri,
    artwork: song.coverArtUri,
    duration: song.duration,
  };
}

export const useSetQueue = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom);
  const setCurrentQueueName = useUpdateAtom(currentQueueNameAtom);

  return async (songs: Song[], name: string, playId?: string) => {
    await TrackPlayer.reset();
    const tracks = songs.map(s => mapSongToTrack(s, name));

    setCurrentQueueName(name);
    if (playId) {
      setCurrentTrack(tracks.find(t => t.id === playId));
    }

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

      // const queue = await TrackPlayer.getQueue();
      // console.log(`queue: ${JSON.stringify(queue.map(x => x.title))}`);
    }
  };
};
