import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import { Server } from '@app/models/settings'
import { selectSettings } from '@app/state/settings'
import { useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import md5 from 'md5'
import React, { useCallback, useState } from 'react'
import { StyleSheet, Text, TextInput, View } from 'react-native'
import { v4 as uuidv4 } from 'uuid'

function replaceIndex<T>(array: T[], index: number, replacement: T): T[] {
  const start = array.slice(0, index)
  const end = array.slice(index + 1)
  return [...start, replacement, ...end]
}

const ServerView: React.FC<{
  id?: string
}> = ({ id }) => {
  const navigation = useNavigation()
  const activeServer = useStore(selectSettings.activeServer)
  const setActiveServer = useStore(selectSettings.setActiveServer)
  const servers = useStore(selectSettings.servers)
  const setServers = useStore(selectSettings.setServers)
  const server = id ? servers.find(s => s.id === id) : undefined

  const [address, setAddress] = useState(server?.address || '')
  const [username, setUsername] = useState(server?.username || '')
  const [password, setPassword] = useState(server?.token ? 'password' : '')

  const validate = useCallback(() => {
    return !!address && !!username && !!password
  }, [address, username, password])

  const canRemove = useCallback(() => {
    return id && servers.length > 1 && activeServer?.id !== id
  }, [id, servers, activeServer])

  const exit = useCallback(() => {
    if (navigation.canGoBack()) {
      navigation.goBack()
    } else {
      navigation.navigate('main')
    }
  }, [navigation])

  const save = useCallback(() => {
    if (!validate()) {
      return
    }

    const salt = server?.salt || uuidv4()
    let token: string
    if (password === 'password' && server?.token) {
      token = server.token
    } else {
      token = md5(password + salt)
    }

    const update: Server = {
      id: server?.id || uuidv4(),
      address,
      username,
      salt,
      token,
    }

    if (server) {
      setServers(
        replaceIndex(
          servers,
          servers.findIndex(s => s.id === id),
          update,
        ),
      )
    } else {
      setServers([...servers, update])
    }

    if (!activeServer) {
      setActiveServer(update.id)
    }

    exit()
  }, [activeServer, address, exit, id, password, server, servers, setActiveServer, setServers, username, validate])

  const remove = useCallback(() => {
    if (!canRemove()) {
      return
    }

    const update = [...servers]
    update.splice(
      update.findIndex(s => s.id === id),
      1,
    )

    setServers(update)
    exit()
  }, [canRemove, exit, id, servers, setServers])

  return (
    <GradientScrollView style={styles.scroll} contentContainerStyle={styles.scrollContentContainer}>
      <View style={styles.content}>
        <Text style={styles.inputTitle}>Address</Text>
        <TextInput
          style={styles.input}
          placeholderTextColor="grey"
          selectionColor={colors.text.secondary}
          textContentType="URL"
          placeholder="Address"
          value={address}
          onChangeText={setAddress}
        />
        <Text style={styles.inputTitle}>Username</Text>
        <TextInput
          style={styles.input}
          placeholderTextColor="grey"
          selectionColor={colors.text.secondary}
          textContentType="username"
          autoCompleteType="username"
          placeholder="Username"
          value={username}
          onChangeText={setUsername}
        />
        <Text style={styles.inputTitle}>Password</Text>
        <TextInput
          style={styles.input}
          placeholderTextColor="grey"
          selectionColor={colors.text.secondary}
          textContentType="password"
          autoCompleteType="password"
          secureTextEntry={true}
          placeholder="Password"
          value={password}
          onChangeText={setPassword}
        />
        <Button
          disabled={!validate()}
          style={styles.button}
          title="Test Connection"
          buttonStyle="hollow"
          onPress={() => {}}
        />
        <Button
          style={[styles.button, styles.delete, { display: canRemove() ? 'flex' : 'none' }]}
          title="Delete"
          onPress={remove}
        />
        <Button disabled={!validate()} style={styles.button} title="Save" onPress={save} />
      </View>
    </GradientScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
  },
  scrollContentContainer: {
    // paddingTop: StatusBar.currentHeight,
  },
  content: {
    paddingHorizontal: 20,
  },
  inputTitle: {
    fontFamily: font.semiBold,
    fontSize: 16,
    color: colors.text.primary,
    marginTop: 10,
  },
  input: {
    borderBottomWidth: 1.5,
    borderColor: colors.text.primary,
    fontFamily: font.regular,
    fontSize: 16,
    color: colors.text.primary,
    marginBottom: 26,
  },
  button: {
    marginTop: 16,
  },
  delete: {
    backgroundColor: 'red',
  },
})

export default ServerView
