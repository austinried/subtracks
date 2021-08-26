import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import PressableOpacity from '@app/components/PressableOpacity'
import SettingsItem from '@app/components/SettingsItem'
import SettingsSwitch from '@app/components/SettingsSwitch'
import TextInput from '@app/components/TextInput'
import { useSwitchActiveServer } from '@app/hooks/server'
import { Server } from '@app/models/settings'
import { selectCache } from '@app/state/cache'
import { selectSettings } from '@app/state/settings'
import { useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/core'
import React, { useCallback, useState } from 'react'
import { KeyboardTypeOptions, Linking, Modal, Pressable, StatusBar, StyleSheet, Text, View } from 'react-native'
import { ScrollView } from 'react-native-gesture-handler'
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

const ModalChoice = React.memo<{
  text: string
  value: any
  setValue: (value: any) => void
  current: boolean
  closeModal?: () => void
}>(({ text, value, setValue, current, closeModal }) => {
  const onPress = useCallback(() => {
    setValue(value)
    if (closeModal) {
      closeModal()
    }
  }, [closeModal, setValue, value])

  return (
    <PressableOpacity onPress={onPress} style={styles.modalChoice}>
      <View style={styles.modalRadio}>
        {current ? (
          <Icon name="checkbox-marked-circle" size={30} color={colors.accent} />
        ) : (
          <Icon name="checkbox-blank-circle-outline" size={30} color={colors.text.secondary} />
        )}
      </View>
      <Text style={styles.modalChoiceText}>{text}</Text>
    </PressableOpacity>
  )
})

function bitrateString(bitrate: number): string {
  return bitrate === 0 ? 'Unlimited' : `${bitrate}kbps`
}

const BitrateModal = React.memo<{
  title: string
  bitrate: number
  setBitrate: (bitrate: number) => void
}>(({ title, bitrate, setBitrate }) => {
  const [visible, setVisible] = useState(false)

  const toggleModal = useCallback(() => setVisible(!visible), [visible])

  const BitrateChoice: React.FC<{ value: number }> = useCallback(
    ({ value }) => {
      const text = bitrateString(value)
      return (
        <ModalChoice
          text={text}
          value={value}
          setValue={setBitrate}
          closeModal={toggleModal}
          current={bitrate === value}
        />
      )
    },
    [bitrate, toggleModal, setBitrate],
  )

  return (
    <>
      <SettingsItem title={title} subtitle={bitrateString(bitrate)} onPress={toggleModal} />
      <Modal animationType="fade" transparent={true} visible={visible} onRequestClose={toggleModal}>
        <Pressable style={styles.modalBackdrop} onPress={toggleModal}>
          <View style={styles.centeredView}>
            <Pressable style={styles.modalView}>
              <Header style={styles.modalHeader}>{title}</Header>
              <ScrollView style={styles.modalScroll}>
                <BitrateChoice value={24} />
                <BitrateChoice value={32} />
                <BitrateChoice value={48} />
                <BitrateChoice value={64} />
                <BitrateChoice value={96} />
                <BitrateChoice value={128} />
                <BitrateChoice value={160} />
                <BitrateChoice value={192} />
                <BitrateChoice value={256} />
                <BitrateChoice value={320} />
                <BitrateChoice value={0} />
              </ScrollView>
            </Pressable>
          </View>
        </Pressable>
      </Modal>
    </>
  )
})

const SettingsTextModal = React.memo<{
  title: string
  value: string
  setValue: (text: string) => void
  getUnit?: (text: string) => string
  keyboardType?: KeyboardTypeOptions
}>(({ title, value, setValue, getUnit, keyboardType }) => {
  const [visible, setVisible] = useState(false)
  const [inputText, setInputText] = useState(value)

  const toggleModal = useCallback(() => setVisible(!visible), [visible])

  const submit = useCallback(() => {
    setValue(inputText)
    toggleModal()
  }, [inputText, setValue, toggleModal])

  const getSubtitle = useCallback(() => {
    if (!getUnit) {
      return value
    }
    return value + ' ' + getUnit(value)
  }, [getUnit, value])

  return (
    <>
      <SettingsItem title={title} subtitle={getSubtitle()} onPress={toggleModal} />
      <Modal animationType="fade" transparent={true} visible={visible} onRequestClose={toggleModal}>
        <Pressable style={styles.modalBackdrop} onPress={toggleModal}>
          <View style={styles.centeredView}>
            <Pressable style={styles.modalView}>
              <Header style={styles.modalHeader}>{title}</Header>
              <View style={styles.modalTextInputLine}>
                <TextInput
                  style={styles.modalTextInput}
                  value={inputText}
                  onChangeText={setInputText}
                  onSubmitEditing={submit}
                  keyboardType={keyboardType}
                />
                <PressableOpacity style={styles.modalTextSubmit} onPress={submit} hitSlop={10}>
                  <Icon name="content-save-edit" color="white" size={32} />
                </PressableOpacity>
              </View>
            </Pressable>
          </View>
        </Pressable>
      </Modal>
    </>
  )
})

function secondsUnit(seconds: string): string {
  const numberValue = parseFloat(seconds)
  if (Math.abs(numberValue) !== 1) {
    return 'seconds'
  }
  return 'second'
}

const SettingsContent = React.memo(() => {
  const servers = useStore(selectSettings.servers)
  const scrobble = useStore(selectSettings.scrobble)
  const setScrobble = useStore(selectSettings.setScrobble)

  // doesn't seem to ever be a case where we want this off
  // will remove later if there isn't a use case for disabling
  // const estimateContentLength = useStore(selectSettings.estimateContentLength)
  // const setEstimateContentLength = useStore(selectSettings.setEstimateContentLength)

  const maxBitrateWifi = useStore(selectSettings.maxBitrateWifi)
  const setMaxBitrateWifi = useStore(selectSettings.setMaxBitrateWifi)

  const maxBitrateMobile = useStore(selectSettings.maxBitrateMobile)
  const setMaxBitrateMobile = useStore(selectSettings.setMaxBitrateMobile)

  const minBuffer = useStore(selectSettings.minBuffer)
  const setMinBuffer = useStore(selectSettings.setMinBuffer)
  const maxBuffer = useStore(selectSettings.maxBuffer)
  const setMaxBuffer = useStore(selectSettings.setMaxBuffer)

  const clearImageCache = useStore(selectCache.clearImageCache)
  const [clearing, setClearing] = useState(false)

  const navigation = useNavigation()

  const clear = useCallback(() => {
    setClearing(true)

    const waitForClear = async () => {
      try {
        await clearImageCache()
      } catch (err) {
        console.log(err)
      } finally {
        setClearing(false)
      }
    }
    waitForClear()
  }, [clearImageCache])

  const setMinBufferText = useCallback((text: string) => setMinBuffer(parseFloat(text)), [setMinBuffer])
  const setMaxBufferText = useCallback((text: string) => setMaxBuffer(parseFloat(text)), [setMaxBuffer])

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
      <BitrateModal title="Maximum bitrate (Wi-Fi)" bitrate={maxBitrateWifi} setBitrate={setMaxBitrateWifi} />
      <BitrateModal title="Maximum bitrate (mobile)" bitrate={maxBitrateMobile} setBitrate={setMaxBitrateMobile} />
      {/* <SettingsSwitch
        title="Estimate content length"
        subtitle='Send the "estimateContentLength" flag when streaming. Helps fix issues with seeking when the server is transcoding songs.'
        value={estimateContentLength}
        setValue={setEstimateContentLength}
      /> */}
      <SettingsTextModal
        title="Minimum buffer time"
        value={minBuffer.toString()}
        setValue={setMinBufferText}
        getUnit={secondsUnit}
        keyboardType="numeric"
      />
      <SettingsTextModal
        title="Maximum buffer time"
        value={maxBuffer.toString()}
        setValue={setMaxBufferText}
        getUnit={secondsUnit}
        keyboardType="numeric"
      />
      <Header style={styles.header}>Music</Header>
      <SettingsSwitch
        title="Scrobble plays"
        subtitle={scrobble ? 'Scrobble play history' : "Don't scrobble play history"}
        value={scrobble}
        setValue={setScrobble}
      />
      <Header style={styles.header}>Reset</Header>
      <Button
        disabled={clearing}
        style={styles.button}
        title="Clear Image Cache"
        onPress={clear}
        buttonStyle="hollow"
      />
      <Header style={styles.header}>About</Header>
      <Button
        disabled={clearing}
        style={styles.button}
        title="Project Homepage"
        onPress={() => Linking.openURL('https://github.com/austinried/subtracks')}
        buttonStyle="hollow"
      />
      <Button
        disabled={clearing}
        style={styles.button}
        title="Licenses (NPM)"
        onPress={() => navigation.navigate('web', { uri: 'file:///android_asset/licenses/npm_licenses.txt' })}
        buttonStyle="hollow"
      />
      <Button
        disabled={clearing}
        style={[styles.button, styles.licenseButton]}
        title="Licenses (Android)"
        onPress={() => navigation.navigate('web', { uri: 'file:///android_asset/licenses/android_licenses.html' })}
        buttonStyle="hollow"
      />
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
    paddingBottom: 40,
  },
  serverActive: {
    paddingLeft: 12,
  },
  header: {
    marginTop: 26,
  },
  button: {
    marginTop: 16,
  },
  licenseButton: {
    marginHorizontal: 10,
    flex: 1,
  },
  licenses: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  modalBackdrop: {
    flex: 1,
  },
  centeredView: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalView: {
    backgroundColor: colors.gradient.high,
    elevation: 5,
  },
  modalChoice: {
    paddingHorizontal: 20,
    paddingVertical: 10,
    paddingRight: 15,
    flexDirection: 'row',
    flex: 1,
  },
  modalChoiceText: {
    fontFamily: font.regular,
    fontSize: 18,
    color: colors.text.primary,
    textAlign: 'left',
    flex: 1,
  },
  modalRadio: {
    height: 30,
    width: 30,
    marginRight: 20,
  },
  modalHeader: {
    marginVertical: 20,
    marginHorizontal: 30,
  },
  modalScroll: {
    maxHeight: 475,
  },
  modalTextInputLine: {
    flexDirection: 'row',
    margin: 20,
  },
  modalTextInput: {
    flex: 1,
    paddingLeft: 12,
  },
  modalTextSubmit: {
    marginLeft: 15,
    // backgroundColor: 'green',
  },
  text: {
    color: 'white',
    fontFamily: font.regular,
    fontSize: 15,
  },
})

export default Settings
