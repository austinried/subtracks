import PressableOpacity from '@app/components/PressableOpacity'
import { AlbumListItem, Song } from '@app/models/music'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { NavigationProp, useNavigation } from '@react-navigation/native'
import { ReactComponentLike } from 'prop-types'
import React from 'react'
import { ScrollView, StyleProp, StyleSheet, Text, View, ViewStyle } from 'react-native'
import FastImage from 'react-native-fast-image'
import { Menu, MenuOption, MenuOptions, MenuTrigger, renderers } from 'react-native-popup-menu'
import IconFA from 'react-native-vector-icons/FontAwesome'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'
import IconMat from 'react-native-vector-icons/MaterialIcons'
import CoverArt from './CoverArt'

const { SlideInMenu } = renderers

type ContextMenuProps = {
  menuStyle?: StyleProp<ViewStyle>
  triggerWrapperStyle?: StyleProp<ViewStyle>
  triggerOuterWrapperStyle?: StyleProp<ViewStyle>
  triggerTouchableStyle?: StyleProp<ViewStyle>
  onPress?: () => void
}

type InternalContextMenuProps = ContextMenuProps & {
  menuHeader: React.ReactNode
  menuOptions: React.ReactNode
}

const ContextMenu: React.FC<InternalContextMenuProps> = ({
  menuStyle,
  triggerWrapperStyle,
  triggerOuterWrapperStyle,
  triggerTouchableStyle,
  onPress,
  menuHeader,
  menuOptions,
  children,
}) => {
  menuStyle = menuStyle || { flex: 1 }
  triggerWrapperStyle = triggerWrapperStyle || { flex: 1 }
  triggerOuterWrapperStyle = triggerOuterWrapperStyle || { flex: 1 }
  triggerTouchableStyle = triggerTouchableStyle || { flex: 1 }
  return (
    <Menu renderer={SlideInMenu} style={menuStyle}>
      <MenuTrigger
        triggerOnLongPress={true}
        customStyles={{
          triggerOuterWrapper: triggerOuterWrapperStyle,
          triggerWrapper: triggerWrapperStyle,
          triggerTouchable: { style: triggerTouchableStyle },
          TriggerTouchableComponent: PressableOpacity,
        }}
        onAlternativeAction={onPress}>
        {children}
      </MenuTrigger>
      <MenuOptions
        customStyles={styles}
        renderOptionsContainer={(options: any) => (
          <ScrollView>
            {menuHeader}
            {options}
          </ScrollView>
        )}>
        {menuOptions}
      </MenuOptions>
    </Menu>
  )
}

type ContextMenuOptionProps = {
  onSelect?: () => void
}

const ContextMenuOption: React.FC<ContextMenuOptionProps> = ({ onSelect, children }) => (
  <MenuOption style={styles.option} onSelect={onSelect}>
    {children}
  </MenuOption>
)

type ContextMenuIconTextOptionProps = ContextMenuOptionProps & {
  IconComponent: ReactComponentLike
  name: string
  size: number
  color?: string
  text: string
}

const ContextMenuIconTextOption = React.memo<ContextMenuIconTextOptionProps>(
  ({ onSelect, IconComponent, name, color, size, text }) => (
    <ContextMenuOption onSelect={onSelect}>
      <View style={styles.icon}>
        <IconComponent name={name} size={size} color={color || colors.text.primary} />
      </View>
      <Text style={styles.optionText}>{text}</Text>
    </ContextMenuOption>
  ),
)

const MenuHeader = React.memo<{
  coverArt?: string
  title: string
  subtitle?: string
}>(({ coverArt, title, subtitle }) => (
  <View style={styles.menuHeader}>
    {coverArt ? (
      <CoverArt coverArt={coverArt} style={styles.coverArt} resizeMode={FastImage.resizeMode.cover} />
    ) : (
      <></>
    )}
    <View style={styles.menuHeaderText}>
      <Text numberOfLines={1} style={styles.menuTitle}>
        {title}
      </Text>
      {subtitle ? (
        <Text numberOfLines={1} style={styles.menuSubtitle}>
          {subtitle}
        </Text>
      ) : (
        <></>
      )}
    </View>
  </View>
))

const OptionStar = React.memo(() => (
  <ContextMenuIconTextOption IconComponent={IconFA} name="star-o" size={26} text="Star" />
))

const OptionViewArtist = React.memo<{
  navigation: NavigationProp<any>
  artist?: string
  artistId?: string
}>(({ navigation, artist, artistId }) => {
  if (!artist || !artistId) {
    return <></>
  }

  return (
    <ContextMenuIconTextOption
      IconComponent={IconFA}
      name="microphone"
      size={26}
      text="View artist"
      onSelect={() => navigation.navigate('artist', { id: artistId, title: artist })}
    />
  )
})

const OptionViewAlbum = React.memo<{
  navigation: NavigationProp<any>
  album?: string
  albumId?: string
}>(({ navigation, album, albumId }) => {
  if (!album || !albumId) {
    return <></>
  }

  return (
    <ContextMenuIconTextOption
      IconComponent={IconFA5}
      name="compact-disc"
      size={26}
      text="View album"
      onSelect={() => navigation.navigate('album', { id: albumId, title: album })}
    />
  )
})

const OptionDownload = React.memo<{
  itemType: string
}>(({ itemType }) => (
  <ContextMenuIconTextOption IconComponent={IconMat} name="file-download" size={26} text={`Download ${itemType}`} />
))

export type AlbumContextPressableProps = ContextMenuProps & {
  album: AlbumListItem
}

export const AlbumContextPressable: React.FC<AlbumContextPressableProps> = props => {
  const navigation = useNavigation()
  const { album, children } = props

  return (
    <ContextMenu
      {...props}
      menuHeader={<MenuHeader title={album.name} subtitle={album.artist} coverArt={album.coverArt} />}
      menuOptions={
        <>
          <OptionStar />
          <OptionViewArtist artist={album.artist} artistId={album.artistId} navigation={navigation} />
          <OptionDownload itemType={album.itemType} />
        </>
      }>
      {children}
    </ContextMenu>
  )
}

export type SongContextPressableProps = ContextMenuProps & {
  song: Song
}

export const SongContextPressable: React.FC<SongContextPressableProps> = props => {
  const navigation = useNavigation()
  const { song, children } = props

  return (
    <ContextMenu
      {...props}
      menuHeader={<MenuHeader title={song.title} subtitle={song.artist} coverArt={song.coverArt} />}
      menuOptions={
        <>
          <OptionStar />
          <OptionViewArtist artist={song.artist} artistId={song.artistId} navigation={navigation} />
          <OptionViewAlbum album={song.album} albumId={song.albumId} navigation={navigation} />
          <OptionDownload itemType={song.itemType} />
        </>
      }>
      {children}
    </ContextMenu>
  )
}

const styles = StyleSheet.create({
  optionsContainer: {
    backgroundColor: 'rgba(45, 45, 45, 0.95)',
    maxHeight: 365,
  },
  optionsWrapper: {
    // marginBottom: 10,
  },
  menuHeader: {
    paddingTop: 14,
    paddingBottom: 10,
    paddingHorizontal: 20,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  coverArt: {
    width: 42,
    height: 42,
  },
  menuHeaderText: {
    flex: 1,
    marginLeft: 10,
  },
  menuTitle: {
    fontFamily: font.semiBold,
    fontSize: 16,
    color: colors.text.primary,
  },
  menuSubtitle: {
    fontFamily: font.regular,
    fontSize: 14,
    color: colors.text.secondary,
  },
  option: {
    paddingVertical: 8,
    paddingHorizontal: 20,
    flexDirection: 'row',
    alignItems: 'center',
  },
  icon: {
    marginRight: 10,
    width: 32,
    height: 32,
    justifyContent: 'center',
    alignItems: 'center',
    // backgroundColor: 'red',
  },
  optionText: {
    fontFamily: font.semiBold,
    fontSize: 16,
    color: colors.text.primary,
    // backgroundColor: 'green',
  },
})