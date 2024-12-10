// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'download_service.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

/// @nodoc
mixin _$DownloadState {
  IList<Download> get downloads => throw _privateConstructorUsedError;
  IList<SourceId> get deletes => throw _privateConstructorUsedError;
  IList<SourceId> get listDownloads => throw _privateConstructorUsedError;
  IList<SourceId> get listCancels => throw _privateConstructorUsedError;
  String get saveDir => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $DownloadStateCopyWith<DownloadState> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DownloadStateCopyWith<$Res> {
  factory $DownloadStateCopyWith(
          DownloadState value, $Res Function(DownloadState) then) =
      _$DownloadStateCopyWithImpl<$Res, DownloadState>;
  @useResult
  $Res call(
      {IList<Download> downloads,
      IList<SourceId> deletes,
      IList<SourceId> listDownloads,
      IList<SourceId> listCancels,
      String saveDir});
}

/// @nodoc
class _$DownloadStateCopyWithImpl<$Res, $Val extends DownloadState>
    implements $DownloadStateCopyWith<$Res> {
  _$DownloadStateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? downloads = null,
    Object? deletes = null,
    Object? listDownloads = null,
    Object? listCancels = null,
    Object? saveDir = null,
  }) {
    return _then(_value.copyWith(
      downloads: null == downloads
          ? _value.downloads
          : downloads // ignore: cast_nullable_to_non_nullable
              as IList<Download>,
      deletes: null == deletes
          ? _value.deletes
          : deletes // ignore: cast_nullable_to_non_nullable
              as IList<SourceId>,
      listDownloads: null == listDownloads
          ? _value.listDownloads
          : listDownloads // ignore: cast_nullable_to_non_nullable
              as IList<SourceId>,
      listCancels: null == listCancels
          ? _value.listCancels
          : listCancels // ignore: cast_nullable_to_non_nullable
              as IList<SourceId>,
      saveDir: null == saveDir
          ? _value.saveDir
          : saveDir // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$DownloadStateImplCopyWith<$Res>
    implements $DownloadStateCopyWith<$Res> {
  factory _$$DownloadStateImplCopyWith(
          _$DownloadStateImpl value, $Res Function(_$DownloadStateImpl) then) =
      __$$DownloadStateImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {IList<Download> downloads,
      IList<SourceId> deletes,
      IList<SourceId> listDownloads,
      IList<SourceId> listCancels,
      String saveDir});
}

/// @nodoc
class __$$DownloadStateImplCopyWithImpl<$Res>
    extends _$DownloadStateCopyWithImpl<$Res, _$DownloadStateImpl>
    implements _$$DownloadStateImplCopyWith<$Res> {
  __$$DownloadStateImplCopyWithImpl(
      _$DownloadStateImpl _value, $Res Function(_$DownloadStateImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? downloads = null,
    Object? deletes = null,
    Object? listDownloads = null,
    Object? listCancels = null,
    Object? saveDir = null,
  }) {
    return _then(_$DownloadStateImpl(
      downloads: null == downloads
          ? _value.downloads
          : downloads // ignore: cast_nullable_to_non_nullable
              as IList<Download>,
      deletes: null == deletes
          ? _value.deletes
          : deletes // ignore: cast_nullable_to_non_nullable
              as IList<SourceId>,
      listDownloads: null == listDownloads
          ? _value.listDownloads
          : listDownloads // ignore: cast_nullable_to_non_nullable
              as IList<SourceId>,
      listCancels: null == listCancels
          ? _value.listCancels
          : listCancels // ignore: cast_nullable_to_non_nullable
              as IList<SourceId>,
      saveDir: null == saveDir
          ? _value.saveDir
          : saveDir // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc

class _$DownloadStateImpl implements _DownloadState {
  const _$DownloadStateImpl(
      {this.downloads = const IListConst([]),
      this.deletes = const IListConst([]),
      this.listDownloads = const IListConst([]),
      this.listCancels = const IListConst([]),
      required this.saveDir});

  @override
  @JsonKey()
  final IList<Download> downloads;
  @override
  @JsonKey()
  final IList<SourceId> deletes;
  @override
  @JsonKey()
  final IList<SourceId> listDownloads;
  @override
  @JsonKey()
  final IList<SourceId> listCancels;
  @override
  final String saveDir;

  @override
  String toString() {
    return 'DownloadState(downloads: $downloads, deletes: $deletes, listDownloads: $listDownloads, listCancels: $listCancels, saveDir: $saveDir)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DownloadStateImpl &&
            const DeepCollectionEquality().equals(other.downloads, downloads) &&
            const DeepCollectionEquality().equals(other.deletes, deletes) &&
            const DeepCollectionEquality()
                .equals(other.listDownloads, listDownloads) &&
            const DeepCollectionEquality()
                .equals(other.listCancels, listCancels) &&
            (identical(other.saveDir, saveDir) || other.saveDir == saveDir));
  }

  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(downloads),
      const DeepCollectionEquality().hash(deletes),
      const DeepCollectionEquality().hash(listDownloads),
      const DeepCollectionEquality().hash(listCancels),
      saveDir);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DownloadStateImplCopyWith<_$DownloadStateImpl> get copyWith =>
      __$$DownloadStateImplCopyWithImpl<_$DownloadStateImpl>(this, _$identity);
}

abstract class _DownloadState implements DownloadState {
  const factory _DownloadState(
      {final IList<Download> downloads,
      final IList<SourceId> deletes,
      final IList<SourceId> listDownloads,
      final IList<SourceId> listCancels,
      required final String saveDir}) = _$DownloadStateImpl;

  @override
  IList<Download> get downloads;
  @override
  IList<SourceId> get deletes;
  @override
  IList<SourceId> get listDownloads;
  @override
  IList<SourceId> get listCancels;
  @override
  String get saveDir;
  @override
  @JsonKey(ignore: true)
  _$$DownloadStateImplCopyWith<_$DownloadStateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
mixin _$Download {
  String get taskId => throw _privateConstructorUsedError;
  DownloadTaskStatus get status => throw _privateConstructorUsedError;
  int get progress => throw _privateConstructorUsedError;
  String get url => throw _privateConstructorUsedError;
  String? get filename => throw _privateConstructorUsedError;
  String get savedDir => throw _privateConstructorUsedError;
  int get timeCreated => throw _privateConstructorUsedError;
  bool get allowCellular => throw _privateConstructorUsedError;

  @JsonKey(ignore: true)
  $DownloadCopyWith<Download> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $DownloadCopyWith<$Res> {
  factory $DownloadCopyWith(Download value, $Res Function(Download) then) =
      _$DownloadCopyWithImpl<$Res, Download>;
  @useResult
  $Res call(
      {String taskId,
      DownloadTaskStatus status,
      int progress,
      String url,
      String? filename,
      String savedDir,
      int timeCreated,
      bool allowCellular});
}

/// @nodoc
class _$DownloadCopyWithImpl<$Res, $Val extends Download>
    implements $DownloadCopyWith<$Res> {
  _$DownloadCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? taskId = null,
    Object? status = null,
    Object? progress = null,
    Object? url = null,
    Object? filename = freezed,
    Object? savedDir = null,
    Object? timeCreated = null,
    Object? allowCellular = null,
  }) {
    return _then(_value.copyWith(
      taskId: null == taskId
          ? _value.taskId
          : taskId // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as DownloadTaskStatus,
      progress: null == progress
          ? _value.progress
          : progress // ignore: cast_nullable_to_non_nullable
              as int,
      url: null == url
          ? _value.url
          : url // ignore: cast_nullable_to_non_nullable
              as String,
      filename: freezed == filename
          ? _value.filename
          : filename // ignore: cast_nullable_to_non_nullable
              as String?,
      savedDir: null == savedDir
          ? _value.savedDir
          : savedDir // ignore: cast_nullable_to_non_nullable
              as String,
      timeCreated: null == timeCreated
          ? _value.timeCreated
          : timeCreated // ignore: cast_nullable_to_non_nullable
              as int,
      allowCellular: null == allowCellular
          ? _value.allowCellular
          : allowCellular // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$DownloadImplCopyWith<$Res>
    implements $DownloadCopyWith<$Res> {
  factory _$$DownloadImplCopyWith(
          _$DownloadImpl value, $Res Function(_$DownloadImpl) then) =
      __$$DownloadImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String taskId,
      DownloadTaskStatus status,
      int progress,
      String url,
      String? filename,
      String savedDir,
      int timeCreated,
      bool allowCellular});
}

/// @nodoc
class __$$DownloadImplCopyWithImpl<$Res>
    extends _$DownloadCopyWithImpl<$Res, _$DownloadImpl>
    implements _$$DownloadImplCopyWith<$Res> {
  __$$DownloadImplCopyWithImpl(
      _$DownloadImpl _value, $Res Function(_$DownloadImpl) _then)
      : super(_value, _then);

  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? taskId = null,
    Object? status = null,
    Object? progress = null,
    Object? url = null,
    Object? filename = freezed,
    Object? savedDir = null,
    Object? timeCreated = null,
    Object? allowCellular = null,
  }) {
    return _then(_$DownloadImpl(
      taskId: null == taskId
          ? _value.taskId
          : taskId // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as DownloadTaskStatus,
      progress: null == progress
          ? _value.progress
          : progress // ignore: cast_nullable_to_non_nullable
              as int,
      url: null == url
          ? _value.url
          : url // ignore: cast_nullable_to_non_nullable
              as String,
      filename: freezed == filename
          ? _value.filename
          : filename // ignore: cast_nullable_to_non_nullable
              as String?,
      savedDir: null == savedDir
          ? _value.savedDir
          : savedDir // ignore: cast_nullable_to_non_nullable
              as String,
      timeCreated: null == timeCreated
          ? _value.timeCreated
          : timeCreated // ignore: cast_nullable_to_non_nullable
              as int,
      allowCellular: null == allowCellular
          ? _value.allowCellular
          : allowCellular // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc

class _$DownloadImpl extends _Download {
  const _$DownloadImpl(
      {required this.taskId,
      required this.status,
      required this.progress,
      required this.url,
      required this.filename,
      required this.savedDir,
      required this.timeCreated,
      required this.allowCellular})
      : super._();

  @override
  final String taskId;
  @override
  final DownloadTaskStatus status;
  @override
  final int progress;
  @override
  final String url;
  @override
  final String? filename;
  @override
  final String savedDir;
  @override
  final int timeCreated;
  @override
  final bool allowCellular;

  @override
  String toString() {
    return 'Download(taskId: $taskId, status: $status, progress: $progress, url: $url, filename: $filename, savedDir: $savedDir, timeCreated: $timeCreated, allowCellular: $allowCellular)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$DownloadImpl &&
            (identical(other.taskId, taskId) || other.taskId == taskId) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.progress, progress) ||
                other.progress == progress) &&
            (identical(other.url, url) || other.url == url) &&
            (identical(other.filename, filename) ||
                other.filename == filename) &&
            (identical(other.savedDir, savedDir) ||
                other.savedDir == savedDir) &&
            (identical(other.timeCreated, timeCreated) ||
                other.timeCreated == timeCreated) &&
            (identical(other.allowCellular, allowCellular) ||
                other.allowCellular == allowCellular));
  }

  @override
  int get hashCode => Object.hash(runtimeType, taskId, status, progress, url,
      filename, savedDir, timeCreated, allowCellular);

  @JsonKey(ignore: true)
  @override
  @pragma('vm:prefer-inline')
  _$$DownloadImplCopyWith<_$DownloadImpl> get copyWith =>
      __$$DownloadImplCopyWithImpl<_$DownloadImpl>(this, _$identity);
}

abstract class _Download extends Download {
  const factory _Download(
      {required final String taskId,
      required final DownloadTaskStatus status,
      required final int progress,
      required final String url,
      required final String? filename,
      required final String savedDir,
      required final int timeCreated,
      required final bool allowCellular}) = _$DownloadImpl;
  const _Download._() : super._();

  @override
  String get taskId;
  @override
  DownloadTaskStatus get status;
  @override
  int get progress;
  @override
  String get url;
  @override
  String? get filename;
  @override
  String get savedDir;
  @override
  int get timeCreated;
  @override
  bool get allowCellular;
  @override
  @JsonKey(ignore: true)
  _$$DownloadImplCopyWith<_$DownloadImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
