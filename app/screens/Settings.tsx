import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import PressableOpacity from '@app/components/PressableOpacity'
import SettingsItem from '@app/components/SettingsItem'
import { useSwitchActiveServer } from '@app/hooks/server'
import { Server } from '@app/models/settings'
import { selectSettings } from '@app/state/settings'
import { useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import { useNavigation } from '@react-navigation/core'
import React, { useCallback } from 'react'
import { StatusBar, StyleSheet, View } from 'react-native'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'

const ServerItem = React.memo<{
  server: Server
}>(({ server }) => {
  const activeServer = useStore(selectSettings.activeServer)
  const switchActiveServer = useSwitchActiveServer()
  const navigation = useNavigation()

  const setActive = useCallback(() => {
    switchActiveServer(server.id)
  }, [server.id, switchActiveServer])

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
  const servers = useStore(selectSettings.servers)
  const navigation = useNavigation()

  return (
    <View style={styles.content}>
      <Header>Servers</Header>
      {servers.map(s => (
        <ServerItem key={s.id} server={s} />
      ))}
      <Button
        style={styles.button}
        title="Add Server"
        onPress={() => navigation.navigate('server')}
        buttonStyle="hollow"
      />
      <Header style={styles.header}>Network</Header>
      <SettingsItem title="Max bitrate (Wi-Fi)" subtitle="Unlimited" />
      <SettingsItem title="Max bitrate (mobile)" subtitle="192kbps" />
      <Header style={styles.header}>Reset</Header>
      <Button style={styles.button} title="Reset everything to default" onPress={() => {}} buttonStyle="hollow" />
    </View>
  )
})

const Settings = () => {
  return (
    <GradientScrollView style={styles.scroll} contentContainerStyle={styles.scrollContentContainer}>
      <SettingsContent />
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
  header: {
    marginTop: 26,
  },
  button: {
    marginVertical: 10,
  },
})

export default Settings
