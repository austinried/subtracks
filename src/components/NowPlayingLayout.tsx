import React from 'react';
import { StyleSheet, Text, View } from 'react-native';

const NowPlayingLayout = () => {
  return (
    <View
      style={{
        // background
        backgroundColor: 'darkblue',
        flex: 1,
      }}>
      {/* top bar */}
      <View
        style={{
          height: 70,
          flexDirection: 'row',
        }}>
        <View style={{ width: 70, height: 70, backgroundColor: 'grey' }} />
        <View style={{ flex: 1, alignItems: 'center', height: 70 }}>
          <View style={{ flex: 1 }} />
          <Text style={styles.text}>Playing from Your Library</Text>
          <Text style={styles.text}>Songs</Text>
          <View style={{ flex: 1 }} />
        </View>
        <View style={{ width: 70, height: 70, backgroundColor: 'grey' }} />
      </View>

      {/* album art */}
      <View
        style={{
          flex: 5,
          // backgroundColor: 'darkorange',
          alignItems: 'center',
        }}>
        <View style={{ flex: 1 }} />
        <View
          style={{
            width: 320,
            height: 320,
            backgroundColor: 'grey',
          }}
        />
        <View style={{ flex: 1 }} />
      </View>

      {/* song/album/artist title */}
      <View
        style={{
          flex: 1,
          // backgroundColor: 'green',
          alignItems: 'center',
        }}>
        <Text style={{ ...styles.text, fontSize: 26 }}>Name of the Song</Text>
        <Text style={{ ...styles.text, fontSize: 20, fontWeight: 'normal' }}>Cool Artist</Text>
      </View>

      {/* seek bar */}
      <View
        style={{
          flex: 0.7,
          // backgroundColor: 'red',
          flexDirection: 'row',
          alignItems: 'center',
        }}>
        <View style={{ width: 20 }} />
        <View
          style={{
            flex: 1,
          }}>
          <View>
            <View
              style={{
                backgroundColor: 'grey',
                height: 3,
                marginBottom: 3,
                // flex: 1,
              }}
            />
            <View
              style={{
                flexDirection: 'row',
              }}>
              <Text style={{ ...styles.text, fontWeight: 'normal' }}>00:00</Text>
              <View style={{ flex: 1 }} />
              <Text style={{ ...styles.text, fontWeight: 'normal' }}>00:00</Text>
            </View>
          </View>
        </View>
        <View style={{ width: 20 }} />
      </View>

      {/* main player controls */}
      <View
        style={{
          height: 90,
          // backgroundColor: 'darkorange',
          flexDirection: 'row',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}>
        <View style={{ width: 14 }} />
        <View style={{ width: 60, height: 60, backgroundColor: 'grey' }} />
        <View style={{ width: 60, height: 60, backgroundColor: 'grey' }} />
        <View style={{ width: 90, height: 90, backgroundColor: 'grey' }} />
        <View style={{ width: 60, height: 60, backgroundColor: 'grey' }} />
        <View style={{ width: 60, height: 60, backgroundColor: 'grey' }} />
        <View style={{ width: 14 }} />
      </View>

      {/* extra controls */}
      <View
        style={{
          flex: 1,
          // backgroundColor: 'green',
          flexDirection: 'row',
        }}>
        <View style={{ width: 14 }} />
        <View style={{ width: 60, height: 60, backgroundColor: 'grey' }} />
        <View style={{ flex: 1 }} />
        <View style={{ width: 60, height: 60, backgroundColor: 'grey' }} />
        <View style={{ width: 14 }} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  text: {
    color: 'white',
    fontWeight: 'bold',
  },
});

export default NowPlayingLayout;
