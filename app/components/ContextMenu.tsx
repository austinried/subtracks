import PressableOpacity from '@app/components/PressableOpacity'
import { useStar } from '@app/hooks/query'
import { StarrableItemType, Song, Artist, Album } from '@app/models/library'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { NavigationProp, useNavigation } from '@react-navigation/native'
import { ReactComponentLike } from 'prop-types'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { ScrollView, StyleProp, StyleSheet, Text, View, ViewStyle } from 'react-native'
import { Menu, MenuOption, MenuOptions, MenuTrigger, renderers } from 'react-native-popup-menu'
import IconFA from 'react-native-vector-icons/FontAwesome'
import IconFA5 from 'react-native-vector-icons/FontAwesome5'
import CoverArt from './CoverArt'
import { Star } from './Star'
import { withSuspenseMemo } from './withSuspense'

const { SlideInMenu } = renderers

type ContextMenuProps = {
  menuStyle?: StyleProp<ViewStyle>
  triggerWrapperStyle?: StyleProp<ViewStyle>
  triggerOuterWrapperStyle?: StyleProp<ViewStyle>
  triggerTouchableStyle?: StyleProp<ViewStyle>
  onPress?: () => any
  triggerOnLongPress?: boolean
  disabled?: boolean
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
  triggerOnLongPress,
  disabled,
}) => {
  menuStyle = menuStyle || { flex: 1 }
  triggerWrapperStyle = triggerWrapperStyle || { flex: 1 }
  triggerOuterWrapperStyle = triggerOuterWrapperStyle || { flex: 1 }
  triggerTouchableStyle = triggerTouchableStyle || { flex: 1 }
  return (
    <Menu renderer={SlideInMenu} style={menuStyle}>
      <MenuTrigger
        disabled={disabled}
        triggerOnLongPress={triggerOnLongPress === undefined ? true : triggerOnLongPress}
        customStyles={{
          triggerOuterWrapper: triggerOuterWrapperStyle,
          triggerWrapper: triggerWrapperStyle,
          triggerTouchable: { style: triggerTouchableStyle, disabled },
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
  onSelect?: () => any
}

const ContextMenuOption: React.FC<ContextMenuOptionProps> = ({ onSelect, children }) => (
  <MenuOption style={styles.option} onSelect={onSelect}>
    {children}
  </MenuOption>
)

type ContextMenuIconTextOptionProps = ContextMenuOptionProps & {
  IconComponent?: ReactComponentLike
  IconComponentRaw?: React.ReactNode
  name?: string
  size?: number
  color?: string
  text: string
}

const ContextMenuIconTextOption = React.memo<ContextMenuIconTextOptionProps>(
  ({ onSelect, IconComponent, IconComponentRaw, name, color, size, text }) => {
    let Icon: React.ReactNode
    if (IconComponentRaw) {
      Icon = IconComponentRaw
    } else if (IconComponent) {
      Icon = <IconComponent name={name} size={size} color={color || colors.text.primary} />
    } else {
      Icon = <></>
    }
    return (
      <ContextMenuOption onSelect={onSelect}>
        <View style={styles.icon}>{Icon}</View>
        <Text style={styles.optionText} numberOfLines={1} adjustsFontSizeToFit={true} minimumFontScale={0.6}>
          {text}
        </Text>
      </ContextMenuOption>
    )
  },
)

const MenuHeader = React.memo<{
  coverArt?: string
  artistId?: string
  albumId?: string
  title: string
  subtitle?: string
}>(({ coverArt, artistId, albumId, title, subtitle }) => {
  let CoverArtComponent = <></>
  if (artistId) {
    CoverArtComponent = (
      <CoverArt
        type="artist"
        artistId={artistId}
        style={styles.coverArt}
        resizeMode="cover"
        round={true}
        size="thumbnail"
        fadeDuration={0}
      />
    )
  } else if (albumId) {
    CoverArtComponent = (
      <CoverArt
        type="album"
        albumId={albumId}
        style={styles.coverArt}
        resizeMode="cover"
        size="thumbnail"
        fadeDuration={0}
      />
    )
  } else {
    CoverArtComponent = (
      <CoverArt
        type="cover"
        coverArt={coverArt}
        style={styles.coverArt}
        resizeMode="cover"
        size="thumbnail"
        fadeDuration={0}
      />
    )
  }

  return (
    <View style={styles.menuHeader}>
      {CoverArtComponent}
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
  )
})

const OptionStar = withSuspenseMemo<{
  id: string
  type: StarrableItemType
  additionalText?: string
}>(({ id, type, additionalText: text }) => {
  const { query, toggle } = useStar(id, type)
  const { t } = useTranslation()

  return (
    <ContextMenuIconTextOption
      IconComponentRaw={<Star starred={!!query.data} size={26} />}
      text={(query.data ? t('context.actions.unstar') : t('context.actions.star')) + (text ? ` ${text}` : '')}
      onSelect={() => toggle.mutate()}
    />
  )
})

const OptionViewArtist = withSuspenseMemo<{
  navigation: NavigationProp<any>
  artist?: string
  artistId?: string
}>(({ navigation, artist, artistId }) => {
  const { t } = useTranslation()

  if (!artist || !artistId) {
    return <></>
  }

  return (
    <ContextMenuIconTextOption
      IconComponent={IconFA}
      name="microphone"
      size={26}
      text={t('resources.artist.actions.view')}
      onSelect={() => navigation.navigate('artist', { id: artistId, title: artist })}
    />
  )
})

const OptionViewAlbum = withSuspenseMemo<{
  navigation: NavigationProp<any>
  album?: string
  albumId?: string
}>(({ navigation, album, albumId }) => {
  const { t } = useTranslation()

  if (!album || !albumId) {
    return <></>
  }

  return (
    <ContextMenuIconTextOption
      IconComponent={IconFA5}
      name="compact-disc"
      size={26}
      text={t('resources.album.actions.view')}
      onSelect={() => navigation.navigate('album', { id: albumId, title: album })}
    />
  )
})

// const OptionDownload = React.memo<{
//   itemType: string
// }>(({ itemType }) => (
//   <ContextMenuIconTextOption IconComponent={IconMat} name="file-download" size={26} text={`Download ${itemType}`} />
// ))

export type AlbumContextPressableProps = ContextMenuProps & {
  album: Album
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
          <OptionStar id={album.id} type={album.itemType} />
          <OptionViewArtist artist={album.artist} artistId={album.artistId} navigation={navigation} />
          {/* <OptionDownload itemType={album.itemType} /> */}
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
      menuHeader={<MenuHeader title={song.title} subtitle={song.artist} albumId={song.albumId} />}
      menuOptions={
        <>
          <OptionStar id={song.id} type={song.itemType} />
          <OptionViewArtist artist={song.artist} artistId={song.artistId} navigation={navigation} />
          <OptionViewAlbum album={song.album} albumId={song.albumId} navigation={navigation} />
          {/* <OptionDownload itemType={song.itemType} /> */}
        </>
      }>
      {children}
    </ContextMenu>
  )
}

export type ArtistContextPressableProps = ContextMenuProps & {
  artist: Artist
}

export const ArtistContextPressable: React.FC<ArtistContextPressableProps> = props => {
  const { artist, children } = props

  return (
    <ContextMenu
      {...props}
      menuHeader={<MenuHeader title={artist.name} artistId={artist.id} />}
      menuOptions={
        <>
          <OptionStar id={artist.id} type={artist.itemType} />
          {/* <OptionDownload itemType={artist.itemType} /> */}
        </>
      }>
      {children}
    </ContextMenu>
  )
}

export type NowPlayingContextPressableProps = ContextMenuProps & {
  song: Song
}

export const NowPlayingContextPressable: React.FC<NowPlayingContextPressableProps> = props => {
  const navigation = useNavigation()
  const { song, children } = props

  return (
    <ContextMenu
      {...props}
      menuHeader={<MenuHeader title={song.title} subtitle={song.artist} albumId={song.albumId} />}
      menuOptions={
        <>
          <OptionStar id={song.id} type={song.itemType} />
          <OptionViewArtist artist={song.artist} artistId={song.artistId} navigation={navigation} />
          <OptionViewAlbum album={song.album} albumId={song.albumId} navigation={navigation} />
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
    paddingHorizontal: 20,
    // backgroundColor: 'purple',
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
    // paddingHorizontal: 100,
    flexDirection: 'row',
    alignItems: 'center',
    // backgroundColor: 'blue',
    overflow: 'hidden',
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
