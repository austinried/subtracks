import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import PressableOpacity from '@app/components/PressableOpacity'
import SettingsItem from '@app/components/SettingsItem'
import SettingsSwitch from '@app/components/SettingsSwitch'
import TextInput from '@app/components/TextInput'
import { useSwitchActiveServer, useResetImageCache } from '@app/hooks/settings'
import { Server } from '@app/models/settings'
import { useStore, useStoreDeep } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/core'
import React, { useCallback, useState } from 'react'
import { KeyboardTypeOptions, Linking, Modal, Pressable, StyleSheet, Text, View } from 'react-native'
import { ScrollView } from 'react-native-gesture-handler'
import { useSafeAreaInsets } from 'react-native-safe-area-context'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'
import { version } from '../../package.json'

const ServerItem = React.memo<{
  server: Server
}>(({ server }) => {
  const activeServerId = useStore(store => store.settings.activeServerId)
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
        {activeServerId === server.id ? (
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
  const servers = useStoreDeep(store => store.settings.servers)
  const scrobble = useStore(store => store.settings.scrobble)
  const setScrobble = useStore(store => store.setScrobble)

  const maxBitrateWifi = useStore(store => store.settings.maxBitrateWifi)
  const setMaxBitrateWifi = useStore(store => store.setMaxBitrateWifi)

  const maxBitrateMobile = useStore(store => store.settings.maxBitrateMobile)
  const setMaxBitrateMobile = useStore(store => store.setMaxBitrateMobile)

  const minBuffer = useStore(store => store.settings.minBuffer)
  const setMinBuffer = useStore(store => store.setMinBuffer)
  const maxBuffer = useStore(store => store.settings.maxBuffer)
  const setMaxBuffer = useStore(store => store.setMaxBuffer)

  const [clearing, setClearing] = useState(false)
  const resetImageCache = useResetImageCache()

  const navigation = useNavigation()

  const clear = useCallback(async () => {
    setClearing(true)
    await resetImageCache()
    setClearing(false)
  }, [resetImageCache])

  const setMinBufferText = useCallback((text: string) => setMinBuffer(parseFloat(text)), [setMinBuffer])
  const setMaxBufferText = useCallback((text: string) => setMaxBuffer(parseFloat(text)), [setMaxBuffer])

  return (
    <View style={styles.content}>
      <Header>Servers</Header>
      {Object.values(servers).map(s => (
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
      <Text style={styles.text}>
        <Text style={styles.bold}>Subtracks</Text> version {version}
      </Text>
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
        title="Licenses"
        onPress={() => navigation.navigate('web', { uri: 'file:///android_asset/licenses.html' })}
        buttonStyle="hollow"
      />
    </View>
  )
})

const Settings = () => {
  const paddingTop = useSafeAreaInsets().top

  return (
    <GradientScrollView style={styles.scroll} contentContainerStyle={{ paddingTop }}>
      <SettingsContent />
    </GradientScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
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
  },
  text: {
    color: 'white',
    fontFamily: font.regular,
    fontSize: 16,
  },
  bold: {
    fontFamily: font.bold,
  },
})

export default Settings
