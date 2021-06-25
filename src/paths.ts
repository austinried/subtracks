import RNFS from 'react-native-fs';

export default {
  imageCache: `${RNFS.DocumentDirectoryPath}/image_cache`,
  songCache: `${RNFS.DocumentDirectoryPath}/song_cache`,
  songs: `${RNFS.DocumentDirectoryPath}/songs`,
};
