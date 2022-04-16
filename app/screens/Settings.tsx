import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import PressableOpacity from '@app/components/PressableOpacity'
import SettingsItem from '@app/components/SettingsItem'
import SettingsSwitch from '@app/components/SettingsSwitch'
import TextInput from '@app/components/TextInput'
import { withSuspenseMemo } from '@app/components/withSuspense'
import { useResetImageCache, useSwitchActiveServer } from '@app/hooks/settings'
import { Server } from '@app/models/settings'
import { useStore, useStoreDeep } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/core'
import React, { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { KeyboardTypeOptions, Linking, Modal, Pressable, StyleSheet, Text, View } from 'react-native'
import { ScrollView } from 'react-native-gesture-handler'
import { useSafeAreaInsets } from 'react-native-safe-area-context'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'
import { version } from '../../package.json'

const ServerItem = withSuspenseMemo<{
  server: Server
}>(({ server }) => {
  const activeServerId = useStore(store => store.settings.activeServerId)
  const switchActiveServer = useSwitchActiveServer()
  const navigation = useNavigation()
  const { t } = useTranslation()

  const setActive = useCallback(() => {
    switchActiveServer(server.id)
  }, [server.id, switchActiveServer])

  return (
    <SettingsItem
      title={server.address}
      subtitle={server.username}
      onPress={() => navigation.navigate('server', { id: server.id, title: t('settings.servers.actions.edit') })}>
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

const BitrateModal = withSuspenseMemo<{
  title: string
  bitrate: number
  setBitrate: (bitrate: number) => void
}>(({ title, bitrate, setBitrate }) => {
  const { t } = useTranslation()
  const [visible, setVisible] = useState(false)

  const toggleModal = useCallback(() => setVisible(!visible), [visible])

  const bitrateText = useCallback(
    (value: number) =>
      value === 0 ? t('settings.network.values.unlimitedKbps') : t('settings.network.values.kbps', { value }),
    [t],
  )

  const BitrateChoice: React.FC<{ value: number }> = useCallback(
    ({ value }) => {
      return (
        <ModalChoice
          text={bitrateText(value)}
          value={value}
          setValue={setBitrate}
          closeModal={toggleModal}
          current={bitrate === value}
        />
      )
    },
    [bitrate, toggleModal, setBitrate, bitrateText],
  )

  return (
    <>
      <SettingsItem title={title} subtitle={bitrateText(bitrate)} onPress={toggleModal} />
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
  subtitle: (value: string) => string
  keyboardType?: KeyboardTypeOptions
}>(({ title, value, setValue, subtitle, keyboardType }) => {
  const [visible, setVisible] = useState(false)
  const [inputText, setInputText] = useState(value)

  const toggleModal = useCallback(() => setVisible(!visible), [visible])

  const submit = useCallback(() => {
    setValue(inputText)
    toggleModal()
  }, [inputText, setValue, toggleModal])

  return (
    <>
      <SettingsItem title={title} subtitle={subtitle(value)} onPress={toggleModal} />
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

const SettingsContent = withSuspenseMemo(() => {
  const { t } = useTranslation()

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

  const secondsText = useCallback((value: string) => t('settings.network.values.seconds', { value }), [t])

  return (
    <View style={styles.content}>
      <Header>{t('settings.servers.name')}</Header>
      {Object.values(servers).map(s => (
        <ServerItem key={s.id} server={s} />
      ))}
      <Button
        style={styles.button}
        title={t('settings.servers.actions.add')}
        onPress={() => navigation.navigate('server', { title: t('settings.servers.actions.add') })}
        buttonStyle="hollow"
      />
      <Header style={styles.header}>{t('settings.network.name')}</Header>
      <BitrateModal
        title={t('settings.network.options.maxBitrateWifi.title')}
        bitrate={maxBitrateWifi}
        setBitrate={setMaxBitrateWifi}
      />
      <BitrateModal
        title={t('settings.network.options.maxBitrateMobile.title')}
        bitrate={maxBitrateMobile}
        setBitrate={setMaxBitrateMobile}
      />
      <SettingsTextModal
        title={t('settings.network.options.minBuffer.title')}
        value={minBuffer.toString()}
        setValue={setMinBufferText}
        subtitle={secondsText}
        keyboardType="numeric"
      />
      <SettingsTextModal
        title={t('settings.network.options.maxBuffer.title')}
        value={maxBuffer.toString()}
        setValue={setMaxBufferText}
        subtitle={secondsText}
        keyboardType="numeric"
      />
      <Header style={styles.header}>{t('settings.music.name')}</Header>
      <SettingsSwitch
        title={t('settings.music.options.scrobble.title')}
        subtitle={
          scrobble
            ? t('settings.music.options.scrobble.descriptionOn')
            : t('settings.music.options.scrobble.descriptionOff')
        }
        value={scrobble}
        setValue={setScrobble}
      />
      <Header style={styles.header}>{t('settings.reset.name')}</Header>
      <Button
        disabled={clearing}
        style={styles.button}
        title={t('settings.reset.actions.clearImageCache')}
        onPress={clear}
        buttonStyle="hollow"
      />
      <Header style={styles.header}>{t('settings.about.name')}</Header>
      <Text style={styles.text}>
        <Text style={styles.bold}>Subtracks</Text> {t('settings.about.version', { version })}
      </Text>
      <Button
        disabled={clearing}
        style={styles.button}
        title={t('settings.about.actions.projectHomepage')}
        onPress={() => Linking.openURL('https://github.com/austinried/subtracks')}
        buttonStyle="hollow"
      />
      <Button
        disabled={clearing}
        style={styles.button}
        title={t('settings.about.actions.licenses')}
        onPress={() =>
          navigation.navigate('web', {
            uri: 'file:///android_asset/licenses.html',
            title: t('settings.about.actions.licenses'),
          })
        }
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
