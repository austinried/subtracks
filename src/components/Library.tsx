import React from 'react';
import { Text, View, Image } from 'react-native';

const SectionHeader: React.FC<{ title: string }> = ({ title }) => {
  return (
    <View style={{
      height: 60,
      flexDirection: 'row',
      justifyContent: 'space-between',
      // backgroundColor: 'green',
      alignItems: 'center',
      paddingLeft: 15,
      paddingRight: 15,
    }}>
      <Text style={{
        color: 'white',
        fontSize: 34,
        fontWeight: 'normal',
        fontFamily: 'Rubik-VariableFont_wght',
      }}>{title}</Text>
      <Image 
        style={{
          width: 32,
          height: 32,
          tintColor: 'white',
        }}
        source={require('../../res/chevron_right.png')}
      />
    </View>
  );
}

const AlbumCoverList = () => {
  
}

const Library = () => (
  <View style={{
    flex: 1,
    backgroundColor: '#3b3b3b',
  }}>
    <SectionHeader title='Albums' />
    <SectionHeader title='Artists' />
    <SectionHeader title='Playlists' />
  </View>
);

export default Library;
