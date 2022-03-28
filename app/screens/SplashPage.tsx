import React, { useEffect, useState } from 'react'
import { Image, View, StyleSheet, Text } from 'react-native'
import { Store, useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import GradientBackground from '@app/components/GradientBackground'
import font from '@app/styles/font'
import Animated, { useAnimatedStyle, useSharedValue, withTiming } from 'react-native-reanimated'

const selectHydrated = (store: Store) => store.hydrated

const SplashPage: React.FC<{}> = ({ children }) => {
  const [ready, setReady] = useState(false)
  const hydrated = useStore(selectHydrated)
  const opacity = useSharedValue(0)

  const animatedStyles = useAnimatedStyle(() => {
    return {
      opacity: opacity.value,
    }
  })

  const minSplashTime = new Promise(resolve => setTimeout(resolve, 1000))

  const prepare = async () => {
    return
  }

  const promise = Promise.all([prepare(), minSplashTime])

  useEffect(() => {
    opacity.value = withTiming(1, {
      duration: 200,
    })
    promise
      .then(() => {
        setReady(true)
      })
      .then(() => {
        opacity.value = withTiming(0, {
          duration: 500,
        })
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const splash = (
    <Animated.View style={[styles.splashContainer, animatedStyles]} pointerEvents="none">
      <GradientBackground style={styles.background} height="100%">
        <View style={styles.logoContainer}>
          <Image style={styles.image} source={require('@res/casette.png')} fadeDuration={0} />
          <Text style={styles.text}>subtracks</Text>
        </View>
      </GradientBackground>
    </Animated.View>
  )

  return (
    <View style={styles.container}>
      {ready && hydrated && children}
      {splash}
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'transparent',
  },
  splashContainer: {
    position: 'absolute',
    height: '100%',
    width: '100%',
  },
  background: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  logoContainer: {
    elevation: 5,
    justifyContent: 'center',
    alignItems: 'center',
  },
  image: {
    height: 150,
    width: 150,
    marginBottom: -10,
    tintColor: colors.accent,
  },
  text: {
    fontFamily: font.bold,
    fontSize: 31,
    color: colors.text.primary,
  },
})

export default SplashPage
