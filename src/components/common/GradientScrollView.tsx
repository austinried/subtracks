import React from 'react';
import { ScrollView, ScrollViewProps } from 'react-native';
import GradientBackground from './GradientBackground';

const GradientScrollView: React.FC<ScrollViewProps> = props => (
  <ScrollView overScrollMode="never" {...props}>
    <GradientBackground />
    {props.children}
  </ScrollView>
);

export default GradientScrollView;
