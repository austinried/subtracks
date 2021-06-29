import { useNavigation } from '@react-navigation/native';
import { useAtomValue } from 'jotai/utils';
import React, { useEffect } from 'react';
import { ScrollView, Text, useWindowDimensions, View, Image, Pressable, GestureResponderEvent } from 'react-native';
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
  return (
    <Pressable
      onPress={onPress}
      style={{
        backgroundColor: colors.accent,
        paddingHorizontal: 24,
        minHeight: 42,
        justifyContent: 'center',
        borderRadius: 1000,
      }}
    >
      <Text style={{ ...text.button }}>{title}</Text>
    </Pressable>
  );
}

const AlbumDetails: React.FC<{
  id: string,
}> = ({ id }) => {
  const album = useAtomValue(albumAtomFamily(id));
  const layout = useWindowDimensions();

  const coverSize = layout.width - layout.width / 2;

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
        coverArtUri={album?.coverArtUri}
      />

      <Text style={{
        ...text.title,
        marginTop: 12,
        width: layout.width - layout.width / 8,
        textAlign: 'center',
      }}>{album?.name}</Text>

      <Text style={{
        ...text.itemSubtitle,
        fontSize: 14,
        marginTop: 4,
        marginBottom: 20,
        width: layout.width - layout.width / 8,
        textAlign: 'center',
      }}>{album?.artist}{album?.year ? ` • ${album.year}` : ''}</Text>

      <View style={{
        flexDirection: 'row'
      }}>
        <Button
          title='Play Album'
          onPress={() => null}
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
      {album?.songs
        .sort((a, b) => (a.track as number) - (b.track as number))
        .map(s => (
          <View key={s.id} style={{
            marginTop: 20,
            marginLeft: 4,
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}>
            <View style={{
              flexShrink: 1,
            }}>
              <Text style={text.songListTitle}>{s.title}</Text>
              <Text style={text.songListSubtitle}>{s.artist}</Text>
            </View>
            <View style={{
              flexDirection: 'row',
              alignItems: 'center',
              marginLeft: 10,
            }}>
              {/* <Text style={text.songListSubtitle}>{secondsToTime(s.duration || 0)}</Text> */}
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
