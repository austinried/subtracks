import React from 'react';
import LinearGradient from 'react-native-linear-gradient';

const TopTabContainer: React.FC<{}> = ({ children }) => (
  <LinearGradient
    colors={['#383838', '#000000']}
    // colors={['#395266', '#06172d']}
    // start={{x: 0.0, y: 0.25}} end={{x: 0.5, y: 1.0}}
    locations={[0.05,0.75]}
    style={{
      flex: 1,
    }}
  >
    {children}
  </LinearGradient>
);

export default TopTabContainer;
