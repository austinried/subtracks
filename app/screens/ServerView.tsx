import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import { Server } from '@app/models/settings'
import { useStore, useStoreDeep } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import toast from '@app/util/toast'
import { useNavigation } from '@react-navigation/native'
import md5 from 'md5'
import React, { useCallback, useState } from 'react'
import { StyleSheet, Text, TextInput, View, ViewStyle } from 'react-native'
import uuid from 'react-native-uuid'
import SettingsSwitch from '@app/components/SettingsSwitch'

const PASSWORD_PLACEHOLDER = 'PASSWORD_PLACEHOLDER'

const ServerView: React.FC<{
  id?: string
}> = ({ id }) => {
  const navigation = useNavigation()
  const activeServerId = useStore(store => store.settings.activeServerId)
  const servers = useStoreDeep(store => store.settings.servers)
  const addServer = useStore(store => store.addServer)
  const updateServer = useStore(store => store.updateServer)
  const removeServer = useStore(store => store.removeServer)
  const server = id ? servers[id] : undefined
  const pingServer = useStore(store => store.pingServer)

  const [address, setAddress] = useState(server?.address || '')
  const [username, setUsername] = useState(server?.username || '')

  const [usePlainPassword, setUsePlainPassword] = useState(server?.usePlainPassword ?? false)
  const [password, setPassword] = useState(
    server?.usePlainPassword ? server.plainPassword || '' : server?.token ? PASSWORD_PLACEHOLDER : '',
  )

  const [testing, setTesting] = useState(false)
  const [removing, setRemoving] = useState(false)
  const [saving, setSaving] = useState(false)

  const validate = useCallback(() => {
    return !!address && !!username && !!password
  }, [address, username, password])

  const canRemove = useCallback(() => {
    return id && Object.keys(servers).length > 1 && activeServerId !== id
  }, [id, servers, activeServerId])

  const exit = useCallback(() => {
    if (navigation.canGoBack()) {
      navigation.goBack()
    } else {
      navigation.navigate('main')
    }
  }, [navigation])

  const createServer = useCallback<() => Server>(() => {
    if (usePlainPassword) {
      return {
        id: server?.id || (uuid.v4() as string),
        usePlainPassword,
        plainPassword: password,
        address,
        username,
      }
    }

    let token: string
    let salt: string

    if (server && !server.usePlainPassword && password === PASSWORD_PLACEHOLDER) {
      salt = server.salt
      token = server.token
    } else {
      salt = uuid.v4() as string
      token = md5(password + salt)
    }

    return {
      id: server?.id || (uuid.v4() as string),
      address,
      username,
      usePlainPassword,
      salt,
      token,
    }
  }, [usePlainPassword, server, address, username, password])

  const save = useCallback(() => {
    if (!validate()) {
      return
    }

    setSaving(true)
    const update = createServer()

    const waitForSave = async () => {
      try {
        if (id) {
          updateServer(update)
        } else {
          await addServer(update)
        }
        exit()
      } catch (err) {
        setSaving(false)
      }
    }
    waitForSave()
  }, [addServer, createServer, exit, id, updateServer, validate])

  const remove = useCallback(() => {
    if (!canRemove()) {
      return
    }

    setRemoving(true)
    const waitForRemove = async () => {
      try {
        await removeServer(id as string)
        exit()
      } catch (err) {
        setRemoving(false)
      }
    }
    waitForRemove()
  }, [canRemove, exit, id, removeServer])

  const togglePlainPassword = useCallback(
    (value: boolean) => {
      setUsePlainPassword(value)

      if (value) {
        if (server && server.usePlainPassword) {
          setPassword(server.plainPassword)
        } else if (server) {
          setPassword('')
        }
      } else {
        if (server && !server.usePlainPassword) {
          setPassword(PASSWORD_PLACEHOLDER)
        }
      }
    },
    [server],
  )

  const test = useCallback(() => {
    setTesting(true)
    const potential = createServer()

    const ping = async () => {
      const res = await pingServer(potential)
      if (res) {
        toast(`Connection to ${potential.address} OK!`)
      } else {
        toast(`Connection to ${potential.address} failed, check settings or server`)
      }
      setTesting(false)
    }
    ping()
  }, [createServer, pingServer])

  const disableControls = useCallback(() => {
    return !validate() || testing || removing || saving
  }, [validate, testing, removing, saving])

  const formatAddress = useCallback(() => {
    let addressFormatted = address.trim()

    if (addressFormatted.endsWith('/')) {
      addressFormatted = addressFormatted.substr(0, addressFormatted.length - 1)
    }

    if (addressFormatted.length > 0 && !addressFormatted.includes(':/')) {
      addressFormatted = `http://${addressFormatted}`
    }

    setAddress(addressFormatted)
  }, [address])

  const deleteStyle: ViewStyle = {
    display: canRemove() ? 'flex' : 'none',
  }

  return (
    <GradientScrollView style={styles.scroll}>
      <View style={styles.content}>
        <Text style={styles.inputTitle}>Address</Text>
        <TextInput
          style={styles.input}
          placeholderTextColor="grey"
          selectionColor={colors.text.secondary}
          textContentType="URL"
          placeholder="http://demo.navidrome.org"
          autoCorrect={false}
          autoCapitalize="none"
          value={address}
          onChangeText={setAddress}
          onBlur={formatAddress}
        />
        <Text style={styles.inputTitle}>Username</Text>
        <TextInput
          style={styles.input}
          placeholderTextColor="grey"
          selectionColor={colors.text.secondary}
          textContentType="username"
          autoComplete="username"
          importantForAutofill="yes"
          autoCapitalize="none"
          placeholder="demo"
          value={username}
          onChangeText={setUsername}
        />
        <Text style={styles.inputTitle}>Password</Text>
        <TextInput
          style={styles.input}
          placeholderTextColor="grey"
          selectionColor={colors.text.secondary}
          textContentType="password"
          autoComplete="password"
          autoCapitalize="none"
          importantForAutofill="yes"
          secureTextEntry={true}
          placeholder="demo"
          value={password}
          onChangeText={setPassword}
        />
        <SettingsSwitch
          title="Force plain text password"
          subtitle={
            usePlainPassword
              ? 'Send password in plain text (legacy, make sure your connection is secure!)'
              : 'Send password as token + salt'
          }
          value={usePlainPassword}
          setValue={togglePlainPassword}
        />
        <Button
          disabled={disableControls()}
          style={styles.button}
          title="Test Connection"
          buttonStyle="hollow"
          onPress={test}
        />
        <Button
          disabled={disableControls()}
          style={[styles.button, styles.delete, deleteStyle]}
          title="Delete"
          onPress={remove}
        />
        <Button disabled={disableControls()} style={styles.button} title="Save" onPress={save} />
      </View>
    </GradientScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
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
