import { useNavigation } from '@react-navigation/native';
import { useAtomValue } from 'jotai/utils';
import React, { useEffect, useState } from 'react';
import { GestureResponderEvent, Image, Pressable, ScrollView, Text, useWindowDimensions, View } from 'react-native';
import { TrackPlayerEvents } from 'react-native-track-player';
import { useCurrentTrackId, useSetQueue } from '../../hooks/player';
import { albumAtomFamily } from '../../state/music';
import colors from '../../styles/colors';
import text from '../../styles/text';
import AlbumCover from './AlbumCover';
import TopTabContainer from './TopTabContainer';

function secondsToTime(s: number): string {
  const seconds = s % 60;
  const minutes = Math.floor(s / 60) % 60;
  const hours = Math.floor(s / 60 / 60);

  let time = `${minutes.toString().padStart(1, '0')}:${seconds.toString().padStart(2, '0')}`;
  if (hours > 0) {
    time = `${hours}:${time}`;
  }
  return time;
}

const Button: React.FC<{
  title: string;
  onPress: (event: GestureResponderEvent) => void;
}> = ({ title, onPress }) => {
  const [opacity, setOpacity] = useState(1);

  return (
    <Pressable
      onPress={onPress}
      onPressIn={() => setOpacity(0.6)}
      onPressOut={() => setOpacity(1)}
      onLongPress={() => setOpacity(1)}
      style={{
        backgroundColor: colors.accent,
        paddingHorizontal: 24,
        minHeight: 42,
        justifyContent: 'center',
        borderRadius: 1000,
        opacity,
      }}
    >
      <Text style={{ ...text.button }}>{title}</Text>
    </Pressable>
  );
}

const songEvents = [
  TrackPlayerEvents.PLAYBACK_STATE,
  TrackPlayerEvents.PLAYBACK_TRACK_CHANGED,
]

const SongItem: React.FC<{
  id: string;
  title: string
  artist?: string;
  onPress: (event: GestureResponderEvent) => void;
}> = ({ id, title, artist, onPress }) => {
  const [opacity, setOpacity] = useState(1);
  const currentTrackId = useCurrentTrackId();

  return (
    <View
      style={{
        marginTop: 20,
        marginLeft: 4,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}
    >
      <Pressable
        onPress={onPress}
        onPressIn={() => setOpacity(0.6)}
        onPressOut={() => setOpacity(1)}
        onLongPress={() => setOpacity(1)}
        style={{
          flex: 1,
          opacity,
        }}
      >
        <Text style={{
          ...text.songListTitle,
          color: currentTrackId === id ? colors.accent : colors.text.primary,
        }}>{title}</Text>
        <Text style={text.songListSubtitle}>{artist}</Text>
      </Pressable>
      <View style={{
        flexDirection: 'row',
        alignItems: 'center',
        marginLeft: 10,
      }}>
        {/* <Text style={text.songListSubtitle}>{secondsToTime(duration || 0)}</Text> */}
        <Image
          source={require('../../../res/star.png')}
          style={{
            height: 28,
            width: 28,
            tintColor: colors.text.secondary,
            marginLeft: 10,
          }}
        />
        <Image
          source={require('../../../res/more_vertical.png')}
          style={{
            height: 28,
            width: 28,
            tintColor: colors.text.secondary,
            marginLeft: 12,
            marginRight: 2,
          }}
        />
      </View>
    </View>
  );
}

const AlbumDetails: React.FC<{
  id: string,
}> = ({ id }) => {
  const album = useAtomValue(albumAtomFamily(id));
  const layout = useWindowDimensions();
  const setQueue = useSetQueue();

  const coverSize = layout.width - layout.width / 2.5;

  if (!album) {
    return (
      <Text style={text.paragraph}>No Album</Text>
    );
  }

  return (
    <ScrollView
      style={{
        flex: 1,
      }}
      contentContainerStyle={{
        alignItems: 'center',
        paddingTop: coverSize / 8,
      }}
    >
      <AlbumCover
        height={coverSize}
        width={coverSize}
        coverArtUri={album.coverArtUri}
      />

      <Text style={{
        ...text.title,
        marginTop: 12,
        width: layout.width - layout.width / 8,
        textAlign: 'center',
      }}>{album.name}</Text>

      <Text style={{
        ...text.itemSubtitle,
        fontSize: 14,
        marginTop: 4,
        marginBottom: 20,
        width: layout.width - layout.width / 8,
        textAlign: 'center',
      }}>{album.artist}{album.year ? ` • ${album.year}` : ''}</Text>

      <View style={{
        flexDirection: 'row'
      }}>
        <Button
          title='Play Album'
          onPress={() => setQueue(album.songs, album.songs[0].id)}
        />
        {/* <View style={{ width: 6, }}></View>
        <Button
          title='S'
          onPress={() => null}
        /> */}
      </View>

      <View style={{
        width: layout.width - (layout.width / 20),
        marginTop: 20,
        marginBottom: 30,
      }}>
      {album.songs
        .sort((a, b) => (a.track as number) - (b.track as number))
        .map(s => (
          <SongItem 
            key={s.id}
            id={s.id}
            title={s.title}
            artist={s.artist}
            onPress={() => setQueue(album.songs, s.id)}
          />
      ))}
      </View>

    </ScrollView>
  );
}


const AlbumView: React.FC<{
  id: string,
  title: string;
}> = ({ id, title }) => {
  const navigation = useNavigation();

  useEffect(() => {
    navigation.setOptions({ title });
  });

  return (
    <TopTabContainer>
      <React.Suspense fallback={<Text>Loading...</Text>}>
        <AlbumDetails id={id} />
      </React.Suspense>
    </TopTabContainer>
  );
};

export default React.memo(AlbumView);
