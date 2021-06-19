import React from 'react';
import LinearGradient from 'react-native-linear-gradient';
import colors from '../../styles/colors';

const TopTabContainer: React.FC<{}> = ({ children }) => (
  <LinearGradient
    colors={[colors.gradient.high, colors.gradient.mid, colors.gradient.low]}
    locations={[0.03,0.3,0.7]}
    style={{
      flex: 1,
    }}
  >
    {children}
  </LinearGradient>
);

export default TopTabContainer;
