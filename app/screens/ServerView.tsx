import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import SettingsSwitch from '@app/components/SettingsSwitch'
import { withSuspense } from '@app/components/withSuspense'
import { Server } from '@app/models/settings'
import { useStore, useStoreDeep } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import toast from '@app/util/toast'
import { useNavigation } from '@react-navigation/native'
import md5 from 'md5'
import React, { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { StyleSheet, Text, TextInput, View, ViewStyle } from 'react-native'
import uuid from 'react-native-uuid'

const PASSWORD_PLACEHOLDER = 'PASSWORD_PLACEHOLDER'

const ServerView = withSuspense<{
  id?: string
  title?: string
}>(({ id, title }) => {
  const { t } = useTranslation('settings.servers')
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

  useEffect(() => {
    navigation.setOptions({ title })
  }, [navigation, title])

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
        id: server?.id || '',
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
      id: server?.id || '',
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

    const update = createServer()

    if (id) {
      updateServer(update)
    } else {
      addServer(update)
    }

    exit()
  }, [addServer, createServer, exit, id, updateServer, validate])

  const remove = useCallback(() => {
    if (!canRemove()) {
      return
    }

    removeServer(id as string)
    exit()
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
      toast(
        t(`messages.${res ? 'connectionOk' : 'connectionFailed'}`, {
          address: potential.address,
          interpolation: { escapeValue: false },
        }),
      )
      setTesting(false)
    }
    ping()
  }, [createServer, pingServer, t])

  const disableControls = useCallback(() => {
    return !validate() || testing
  }, [validate, testing])

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
        <Text style={styles.inputTitle}>{t('fields.address')}</Text>
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
        <Text style={styles.inputTitle}>{t('fields.username')}</Text>
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
        <Text style={styles.inputTitle}>{t('fields.password')}</Text>
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
          title={t('options.forcePlaintextPassword.title')}
          subtitle={
            usePlainPassword
              ? t('options.forcePlaintextPassword.descriptionOn')
              : t('options.forcePlaintextPassword.descriptionOff')
          }
          value={usePlainPassword}
          setValue={togglePlainPassword}
        />
        <Button
          disabled={disableControls()}
          style={styles.button}
          title={t('actions.testConnection')}
          buttonStyle="hollow"
          onPress={test}
        />
        <Button
          disabled={disableControls()}
          style={[styles.button, styles.delete, deleteStyle]}
          title={t('actions.delete')}
          onPress={remove}
        />
        <Button disabled={disableControls()} style={styles.button} title={t('actions.save')} onPress={save} />
      </View>
    </GradientScrollView>
  )
})

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
