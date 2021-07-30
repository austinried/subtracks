import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import PressableOpacity from '@app/components/PressableOpacity'
import SettingsItem from '@app/components/SettingsItem'
import { Server } from '@app/models/settings'
import { useSetActiveServer } from '@app/state/server'
import { activeServerAtom, appSettingsAtom } from '@app/state/settings'
import colors from '@app/styles/colors'
import { useNavigation } from '@react-navigation/core'
import { useAtomValue } from 'jotai/utils'
import React, { useCallback } from 'react'
import { StatusBar, StyleSheet, View } from 'react-native'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'

const ServerItem = React.memo<{
  server: Server
}>(({ server }) => {
  const activeServer = useAtomValue(activeServerAtom)
  const setActiveServer = useSetActiveServer()
  const navigation = useNavigation()

  const setActive = useCallback(() => {
    setActiveServer(server.id)
  }, [server.id, setActiveServer])

  return (
    <SettingsItem
      title={server.address}
      subtitle={server.username}
      onPress={() => navigation.navigate('server', { id: server.id })}>
      <PressableOpacity style={styles.serverActive} onPress={setActive}>
        {activeServer && activeServer.id === server.id ? (
          <Icon name="checkbox-marked-circle" size={30} color={colors.accent} />
        ) : (
          <Icon name="checkbox-blank-circle-outline" size={30} color={colors.text.secondary} />
        )}
      </PressableOpacity>
    </SettingsItem>
  )
})

const SettingsContent = React.memo(() => {
  const settings = useAtomValue(appSettingsAtom)
  const navigation = useNavigation()

  return (
    <View style={styles.content}>
      <Header>Servers</Header>
      {settings.servers.map(s => (
        <ServerItem key={s.id} server={s} />
      ))}
      <Button title="Add Server" onPress={() => navigation.navigate('server')} buttonStyle="hollow" />
      <Header>Network</Header>
      <SettingsItem title="Max bitrate (Wi-Fi)" subtitle="Unlimited" />
      <SettingsItem title="Max bitrate (mobile)" subtitle="192kbps" />
      <Header>Reset</Header>
      <Button title="Reset everything to default" onPress={() => {}} buttonStyle="hollow" />
    </View>
  )
})

const Settings = () => {
  return (
    <GradientScrollView style={styles.scroll} contentContainerStyle={styles.scrollContentContainer}>
      <React.Suspense fallback={() => <></>}>
        <SettingsContent />
      </React.Suspense>
    </GradientScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
  },
  scrollContentContainer: {
    paddingTop: StatusBar.currentHeight,
  },
  content: {
    paddingHorizontal: 20,
  },
  text: {
    color: 'white',
  },
  serverActive: {
    paddingLeft: 12,
  },
})

export default Settings
