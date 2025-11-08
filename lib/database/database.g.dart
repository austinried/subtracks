// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'database.dart';

// ignore_for_file: type=lint
class Sources extends Table with TableInfo<Sources, Source> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  Sources(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    hasAutoIncrement: true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    $customConstraints: 'NOT NULL PRIMARY KEY AUTOINCREMENT',
  );
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
    'name',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL COLLATE NOCASE',
  );
  static const VerificationMeta _isActiveMeta = const VerificationMeta(
    'isActive',
  );
  late final GeneratedColumn<bool> isActive = GeneratedColumn<bool>(
    'is_active',
    aliasedName,
    true,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    $customConstraints: 'UNIQUE',
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: false,
    $customConstraints:
        'NOT NULL DEFAULT (strftime(\'%s\', CURRENT_TIMESTAMP))',
    defaultValue: const CustomExpression('strftime(\'%s\', CURRENT_TIMESTAMP)'),
  );
  @override
  List<GeneratedColumn> get $columns => [id, name, isActive, createdAt];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'sources';
  @override
  VerificationContext validateIntegrity(
    Insertable<Source> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('name')) {
      context.handle(
        _nameMeta,
        name.isAcceptableOrUnknown(data['name']!, _nameMeta),
      );
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('is_active')) {
      context.handle(
        _isActiveMeta,
        isActive.isAcceptableOrUnknown(data['is_active']!, _isActiveMeta),
      );
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  Source map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return Source(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      name: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}name'],
      )!,
      isActive: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}is_active'],
      ),
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}created_at'],
      )!,
    );
  }

  @override
  Sources createAlias(String alias) {
    return Sources(attachedDatabase, alias);
  }

  @override
  bool get dontWriteConstraints => true;
}

class Source extends DataClass implements Insertable<Source> {
  final int id;
  final String name;
  final bool? isActive;
  final DateTime createdAt;
  const Source({
    required this.id,
    required this.name,
    this.isActive,
    required this.createdAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['name'] = Variable<String>(name);
    if (!nullToAbsent || isActive != null) {
      map['is_active'] = Variable<bool>(isActive);
    }
    map['created_at'] = Variable<DateTime>(createdAt);
    return map;
  }

  SourcesCompanion toCompanion(bool nullToAbsent) {
    return SourcesCompanion(
      id: Value(id),
      name: Value(name),
      isActive: isActive == null && nullToAbsent
          ? const Value.absent()
          : Value(isActive),
      createdAt: Value(createdAt),
    );
  }

  factory Source.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return Source(
      id: serializer.fromJson<int>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      isActive: serializer.fromJson<bool?>(json['is_active']),
      createdAt: serializer.fromJson<DateTime>(json['created_at']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'name': serializer.toJson<String>(name),
      'is_active': serializer.toJson<bool?>(isActive),
      'created_at': serializer.toJson<DateTime>(createdAt),
    };
  }

  Source copyWith({
    int? id,
    String? name,
    Value<bool?> isActive = const Value.absent(),
    DateTime? createdAt,
  }) => Source(
    id: id ?? this.id,
    name: name ?? this.name,
    isActive: isActive.present ? isActive.value : this.isActive,
    createdAt: createdAt ?? this.createdAt,
  );
  Source copyWithCompanion(SourcesCompanion data) {
    return Source(
      id: data.id.present ? data.id.value : this.id,
      name: data.name.present ? data.name.value : this.name,
      isActive: data.isActive.present ? data.isActive.value : this.isActive,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('Source(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('isActive: $isActive, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, name, isActive, createdAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is Source &&
          other.id == this.id &&
          other.name == this.name &&
          other.isActive == this.isActive &&
          other.createdAt == this.createdAt);
}

class SourcesCompanion extends UpdateCompanion<Source> {
  final Value<int> id;
  final Value<String> name;
  final Value<bool?> isActive;
  final Value<DateTime> createdAt;
  const SourcesCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.isActive = const Value.absent(),
    this.createdAt = const Value.absent(),
  });
  SourcesCompanion.insert({
    this.id = const Value.absent(),
    required String name,
    this.isActive = const Value.absent(),
    this.createdAt = const Value.absent(),
  }) : name = Value(name);
  static Insertable<Source> custom({
    Expression<int>? id,
    Expression<String>? name,
    Expression<bool>? isActive,
    Expression<DateTime>? createdAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (isActive != null) 'is_active': isActive,
      if (createdAt != null) 'created_at': createdAt,
    });
  }

  SourcesCompanion copyWith({
    Value<int>? id,
    Value<String>? name,
    Value<bool?>? isActive,
    Value<DateTime>? createdAt,
  }) {
    return SourcesCompanion(
      id: id ?? this.id,
      name: name ?? this.name,
      isActive: isActive ?? this.isActive,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (isActive.present) {
      map['is_active'] = Variable<bool>(isActive.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SourcesCompanion(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('isActive: $isActive, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }
}

class SubsonicSettings extends Table
    with TableInfo<SubsonicSettings, SubsonicSetting> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  SubsonicSettings(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _sourceIdMeta = const VerificationMeta(
    'sourceId',
  );
  late final GeneratedColumn<int> sourceId = GeneratedColumn<int>(
    'source_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    $customConstraints: 'NOT NULL PRIMARY KEY',
  );
  late final GeneratedColumnWithTypeConverter<Uri, String> address =
      GeneratedColumn<String>(
        'address',
        aliasedName,
        false,
        type: DriftSqlType.string,
        requiredDuringInsert: true,
        $customConstraints: 'NOT NULL',
      ).withConverter<Uri>(SubsonicSettings.$converteraddress);
  static const VerificationMeta _usernameMeta = const VerificationMeta(
    'username',
  );
  late final GeneratedColumn<String> username = GeneratedColumn<String>(
    'username',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _passwordMeta = const VerificationMeta(
    'password',
  );
  late final GeneratedColumn<String> password = GeneratedColumn<String>(
    'password',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _useTokenAuthMeta = const VerificationMeta(
    'useTokenAuth',
  );
  late final GeneratedColumn<bool> useTokenAuth = GeneratedColumn<bool>(
    'use_token_auth',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    $customConstraints: 'NOT NULL DEFAULT 1',
    defaultValue: const CustomExpression('1'),
  );
  @override
  List<GeneratedColumn> get $columns => [
    sourceId,
    address,
    username,
    password,
    useTokenAuth,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'subsonic_settings';
  @override
  VerificationContext validateIntegrity(
    Insertable<SubsonicSetting> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('source_id')) {
      context.handle(
        _sourceIdMeta,
        sourceId.isAcceptableOrUnknown(data['source_id']!, _sourceIdMeta),
      );
    }
    if (data.containsKey('username')) {
      context.handle(
        _usernameMeta,
        username.isAcceptableOrUnknown(data['username']!, _usernameMeta),
      );
    } else if (isInserting) {
      context.missing(_usernameMeta);
    }
    if (data.containsKey('password')) {
      context.handle(
        _passwordMeta,
        password.isAcceptableOrUnknown(data['password']!, _passwordMeta),
      );
    } else if (isInserting) {
      context.missing(_passwordMeta);
    }
    if (data.containsKey('use_token_auth')) {
      context.handle(
        _useTokenAuthMeta,
        useTokenAuth.isAcceptableOrUnknown(
          data['use_token_auth']!,
          _useTokenAuthMeta,
        ),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {sourceId};
  @override
  SubsonicSetting map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return SubsonicSetting(
      sourceId: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}source_id'],
      )!,
      address: SubsonicSettings.$converteraddress.fromSql(
        attachedDatabase.typeMapping.read(
          DriftSqlType.string,
          data['${effectivePrefix}address'],
        )!,
      ),
      username: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}username'],
      )!,
      password: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}password'],
      )!,
      useTokenAuth: attachedDatabase.typeMapping.read(
        DriftSqlType.bool,
        data['${effectivePrefix}use_token_auth'],
      )!,
    );
  }

  @override
  SubsonicSettings createAlias(String alias) {
    return SubsonicSettings(attachedDatabase, alias);
  }

  static TypeConverter<Uri, String> $converteraddress = const UriConverter();
  @override
  List<String> get customConstraints => const [
    'FOREIGN KEY(source_id)REFERENCES sources(id)ON DELETE CASCADE',
  ];
  @override
  bool get dontWriteConstraints => true;
}

class SubsonicSetting extends DataClass implements Insertable<SubsonicSetting> {
  final int sourceId;
  final Uri address;
  final String username;
  final String password;
  final bool useTokenAuth;
  const SubsonicSetting({
    required this.sourceId,
    required this.address,
    required this.username,
    required this.password,
    required this.useTokenAuth,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['source_id'] = Variable<int>(sourceId);
    {
      map['address'] = Variable<String>(
        SubsonicSettings.$converteraddress.toSql(address),
      );
    }
    map['username'] = Variable<String>(username);
    map['password'] = Variable<String>(password);
    map['use_token_auth'] = Variable<bool>(useTokenAuth);
    return map;
  }

  SubsonicSettingsCompanion toCompanion(bool nullToAbsent) {
    return SubsonicSettingsCompanion(
      sourceId: Value(sourceId),
      address: Value(address),
      username: Value(username),
      password: Value(password),
      useTokenAuth: Value(useTokenAuth),
    );
  }

  factory SubsonicSetting.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return SubsonicSetting(
      sourceId: serializer.fromJson<int>(json['source_id']),
      address: serializer.fromJson<Uri>(json['address']),
      username: serializer.fromJson<String>(json['username']),
      password: serializer.fromJson<String>(json['password']),
      useTokenAuth: serializer.fromJson<bool>(json['use_token_auth']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'source_id': serializer.toJson<int>(sourceId),
      'address': serializer.toJson<Uri>(address),
      'username': serializer.toJson<String>(username),
      'password': serializer.toJson<String>(password),
      'use_token_auth': serializer.toJson<bool>(useTokenAuth),
    };
  }

  SubsonicSetting copyWith({
    int? sourceId,
    Uri? address,
    String? username,
    String? password,
    bool? useTokenAuth,
  }) => SubsonicSetting(
    sourceId: sourceId ?? this.sourceId,
    address: address ?? this.address,
    username: username ?? this.username,
    password: password ?? this.password,
    useTokenAuth: useTokenAuth ?? this.useTokenAuth,
  );
  SubsonicSetting copyWithCompanion(SubsonicSettingsCompanion data) {
    return SubsonicSetting(
      sourceId: data.sourceId.present ? data.sourceId.value : this.sourceId,
      address: data.address.present ? data.address.value : this.address,
      username: data.username.present ? data.username.value : this.username,
      password: data.password.present ? data.password.value : this.password,
      useTokenAuth: data.useTokenAuth.present
          ? data.useTokenAuth.value
          : this.useTokenAuth,
    );
  }

  @override
  String toString() {
    return (StringBuffer('SubsonicSetting(')
          ..write('sourceId: $sourceId, ')
          ..write('address: $address, ')
          ..write('username: $username, ')
          ..write('password: $password, ')
          ..write('useTokenAuth: $useTokenAuth')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(sourceId, address, username, password, useTokenAuth);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is SubsonicSetting &&
          other.sourceId == this.sourceId &&
          other.address == this.address &&
          other.username == this.username &&
          other.password == this.password &&
          other.useTokenAuth == this.useTokenAuth);
}

class SubsonicSettingsCompanion extends UpdateCompanion<SubsonicSetting> {
  final Value<int> sourceId;
  final Value<Uri> address;
  final Value<String> username;
  final Value<String> password;
  final Value<bool> useTokenAuth;
  const SubsonicSettingsCompanion({
    this.sourceId = const Value.absent(),
    this.address = const Value.absent(),
    this.username = const Value.absent(),
    this.password = const Value.absent(),
    this.useTokenAuth = const Value.absent(),
  });
  SubsonicSettingsCompanion.insert({
    this.sourceId = const Value.absent(),
    required Uri address,
    required String username,
    required String password,
    this.useTokenAuth = const Value.absent(),
  }) : address = Value(address),
       username = Value(username),
       password = Value(password);
  static Insertable<SubsonicSetting> custom({
    Expression<int>? sourceId,
    Expression<String>? address,
    Expression<String>? username,
    Expression<String>? password,
    Expression<bool>? useTokenAuth,
  }) {
    return RawValuesInsertable({
      if (sourceId != null) 'source_id': sourceId,
      if (address != null) 'address': address,
      if (username != null) 'username': username,
      if (password != null) 'password': password,
      if (useTokenAuth != null) 'use_token_auth': useTokenAuth,
    });
  }

  SubsonicSettingsCompanion copyWith({
    Value<int>? sourceId,
    Value<Uri>? address,
    Value<String>? username,
    Value<String>? password,
    Value<bool>? useTokenAuth,
  }) {
    return SubsonicSettingsCompanion(
      sourceId: sourceId ?? this.sourceId,
      address: address ?? this.address,
      username: username ?? this.username,
      password: password ?? this.password,
      useTokenAuth: useTokenAuth ?? this.useTokenAuth,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (sourceId.present) {
      map['source_id'] = Variable<int>(sourceId.value);
    }
    if (address.present) {
      map['address'] = Variable<String>(
        SubsonicSettings.$converteraddress.toSql(address.value),
      );
    }
    if (username.present) {
      map['username'] = Variable<String>(username.value);
    }
    if (password.present) {
      map['password'] = Variable<String>(password.value);
    }
    if (useTokenAuth.present) {
      map['use_token_auth'] = Variable<bool>(useTokenAuth.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SubsonicSettingsCompanion(')
          ..write('sourceId: $sourceId, ')
          ..write('address: $address, ')
          ..write('username: $username, ')
          ..write('password: $password, ')
          ..write('useTokenAuth: $useTokenAuth')
          ..write(')'))
        .toString();
  }
}

class Artists extends Table with TableInfo<Artists, models.Artist> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  Artists(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _sourceIdMeta = const VerificationMeta(
    'sourceId',
  );
  late final GeneratedColumn<int> sourceId = GeneratedColumn<int>(
    'source_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
    'name',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL COLLATE NOCASE',
  );
  static const VerificationMeta _starredMeta = const VerificationMeta(
    'starred',
  );
  late final GeneratedColumn<DateTime> starred = GeneratedColumn<DateTime>(
    'starred',
    aliasedName,
    true,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  @override
  List<GeneratedColumn> get $columns => [sourceId, id, name, starred];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'artists';
  @override
  VerificationContext validateIntegrity(
    Insertable<models.Artist> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('source_id')) {
      context.handle(
        _sourceIdMeta,
        sourceId.isAcceptableOrUnknown(data['source_id']!, _sourceIdMeta),
      );
    } else if (isInserting) {
      context.missing(_sourceIdMeta);
    }
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('name')) {
      context.handle(
        _nameMeta,
        name.isAcceptableOrUnknown(data['name']!, _nameMeta),
      );
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('starred')) {
      context.handle(
        _starredMeta,
        starred.isAcceptableOrUnknown(data['starred']!, _starredMeta),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {sourceId, id};
  @override
  models.Artist map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return models.Artist(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      name: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}name'],
      )!,
      starred: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}starred'],
      ),
    );
  }

  @override
  Artists createAlias(String alias) {
    return Artists(attachedDatabase, alias);
  }

  @override
  List<String> get customConstraints => const [
    'PRIMARY KEY(source_id, id)',
    'FOREIGN KEY(source_id)REFERENCES sources(id)ON DELETE CASCADE',
  ];
  @override
  bool get dontWriteConstraints => true;
}

class ArtistsCompanion extends UpdateCompanion<models.Artist> {
  final Value<int> sourceId;
  final Value<String> id;
  final Value<String> name;
  final Value<DateTime?> starred;
  final Value<int> rowid;
  const ArtistsCompanion({
    this.sourceId = const Value.absent(),
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.starred = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  ArtistsCompanion.insert({
    required int sourceId,
    required String id,
    required String name,
    this.starred = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : sourceId = Value(sourceId),
       id = Value(id),
       name = Value(name);
  static Insertable<models.Artist> custom({
    Expression<int>? sourceId,
    Expression<String>? id,
    Expression<String>? name,
    Expression<DateTime>? starred,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (sourceId != null) 'source_id': sourceId,
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (starred != null) 'starred': starred,
      if (rowid != null) 'rowid': rowid,
    });
  }

  ArtistsCompanion copyWith({
    Value<int>? sourceId,
    Value<String>? id,
    Value<String>? name,
    Value<DateTime?>? starred,
    Value<int>? rowid,
  }) {
    return ArtistsCompanion(
      sourceId: sourceId ?? this.sourceId,
      id: id ?? this.id,
      name: name ?? this.name,
      starred: starred ?? this.starred,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (sourceId.present) {
      map['source_id'] = Variable<int>(sourceId.value);
    }
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (starred.present) {
      map['starred'] = Variable<DateTime>(starred.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('ArtistsCompanion(')
          ..write('sourceId: $sourceId, ')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('starred: $starred, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class Albums extends Table with TableInfo<Albums, models.Album> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  Albums(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _sourceIdMeta = const VerificationMeta(
    'sourceId',
  );
  late final GeneratedColumn<int> sourceId = GeneratedColumn<int>(
    'source_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _artistIdMeta = const VerificationMeta(
    'artistId',
  );
  late final GeneratedColumn<String> artistId = GeneratedColumn<String>(
    'artist_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
    'name',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL COLLATE NOCASE',
  );
  static const VerificationMeta _albumArtistMeta = const VerificationMeta(
    'albumArtist',
  );
  late final GeneratedColumn<String> albumArtist = GeneratedColumn<String>(
    'album_artist',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: 'COLLATE NOCASE',
  );
  static const VerificationMeta _createdMeta = const VerificationMeta(
    'created',
  );
  late final GeneratedColumn<DateTime> created = GeneratedColumn<DateTime>(
    'created',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _coverArtMeta = const VerificationMeta(
    'coverArt',
  );
  late final GeneratedColumn<String> coverArt = GeneratedColumn<String>(
    'cover_art',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _genreMeta = const VerificationMeta('genre');
  late final GeneratedColumn<String> genre = GeneratedColumn<String>(
    'genre',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _yearMeta = const VerificationMeta('year');
  late final GeneratedColumn<int> year = GeneratedColumn<int>(
    'year',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _starredMeta = const VerificationMeta(
    'starred',
  );
  late final GeneratedColumn<DateTime> starred = GeneratedColumn<DateTime>(
    'starred',
    aliasedName,
    true,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _frequentRankMeta = const VerificationMeta(
    'frequentRank',
  );
  late final GeneratedColumn<int> frequentRank = GeneratedColumn<int>(
    'frequent_rank',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _recentRankMeta = const VerificationMeta(
    'recentRank',
  );
  late final GeneratedColumn<int> recentRank = GeneratedColumn<int>(
    'recent_rank',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  @override
  List<GeneratedColumn> get $columns => [
    sourceId,
    id,
    artistId,
    name,
    albumArtist,
    created,
    coverArt,
    genre,
    year,
    starred,
    frequentRank,
    recentRank,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'albums';
  @override
  VerificationContext validateIntegrity(
    Insertable<models.Album> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('source_id')) {
      context.handle(
        _sourceIdMeta,
        sourceId.isAcceptableOrUnknown(data['source_id']!, _sourceIdMeta),
      );
    } else if (isInserting) {
      context.missing(_sourceIdMeta);
    }
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('artist_id')) {
      context.handle(
        _artistIdMeta,
        artistId.isAcceptableOrUnknown(data['artist_id']!, _artistIdMeta),
      );
    }
    if (data.containsKey('name')) {
      context.handle(
        _nameMeta,
        name.isAcceptableOrUnknown(data['name']!, _nameMeta),
      );
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('album_artist')) {
      context.handle(
        _albumArtistMeta,
        albumArtist.isAcceptableOrUnknown(
          data['album_artist']!,
          _albumArtistMeta,
        ),
      );
    }
    if (data.containsKey('created')) {
      context.handle(
        _createdMeta,
        created.isAcceptableOrUnknown(data['created']!, _createdMeta),
      );
    } else if (isInserting) {
      context.missing(_createdMeta);
    }
    if (data.containsKey('cover_art')) {
      context.handle(
        _coverArtMeta,
        coverArt.isAcceptableOrUnknown(data['cover_art']!, _coverArtMeta),
      );
    }
    if (data.containsKey('genre')) {
      context.handle(
        _genreMeta,
        genre.isAcceptableOrUnknown(data['genre']!, _genreMeta),
      );
    }
    if (data.containsKey('year')) {
      context.handle(
        _yearMeta,
        year.isAcceptableOrUnknown(data['year']!, _yearMeta),
      );
    }
    if (data.containsKey('starred')) {
      context.handle(
        _starredMeta,
        starred.isAcceptableOrUnknown(data['starred']!, _starredMeta),
      );
    }
    if (data.containsKey('frequent_rank')) {
      context.handle(
        _frequentRankMeta,
        frequentRank.isAcceptableOrUnknown(
          data['frequent_rank']!,
          _frequentRankMeta,
        ),
      );
    }
    if (data.containsKey('recent_rank')) {
      context.handle(
        _recentRankMeta,
        recentRank.isAcceptableOrUnknown(data['recent_rank']!, _recentRankMeta),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {sourceId, id};
  @override
  models.Album map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return models.Album(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      artistId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}artist_id'],
      ),
      name: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}name'],
      )!,
      albumArtist: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}album_artist'],
      ),
      created: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}created'],
      )!,
      coverArt: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}cover_art'],
      ),
      year: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}year'],
      ),
      starred: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}starred'],
      ),
      genre: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}genre'],
      ),
      frequentRank: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}frequent_rank'],
      ),
      recentRank: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}recent_rank'],
      ),
    );
  }

  @override
  Albums createAlias(String alias) {
    return Albums(attachedDatabase, alias);
  }

  @override
  List<String> get customConstraints => const [
    'PRIMARY KEY(source_id, id)',
    'FOREIGN KEY(source_id)REFERENCES sources(id)ON DELETE CASCADE',
  ];
  @override
  bool get dontWriteConstraints => true;
}

class AlbumsCompanion extends UpdateCompanion<models.Album> {
  final Value<int> sourceId;
  final Value<String> id;
  final Value<String?> artistId;
  final Value<String> name;
  final Value<String?> albumArtist;
  final Value<DateTime> created;
  final Value<String?> coverArt;
  final Value<String?> genre;
  final Value<int?> year;
  final Value<DateTime?> starred;
  final Value<int?> frequentRank;
  final Value<int?> recentRank;
  final Value<int> rowid;
  const AlbumsCompanion({
    this.sourceId = const Value.absent(),
    this.id = const Value.absent(),
    this.artistId = const Value.absent(),
    this.name = const Value.absent(),
    this.albumArtist = const Value.absent(),
    this.created = const Value.absent(),
    this.coverArt = const Value.absent(),
    this.genre = const Value.absent(),
    this.year = const Value.absent(),
    this.starred = const Value.absent(),
    this.frequentRank = const Value.absent(),
    this.recentRank = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  AlbumsCompanion.insert({
    required int sourceId,
    required String id,
    this.artistId = const Value.absent(),
    required String name,
    this.albumArtist = const Value.absent(),
    required DateTime created,
    this.coverArt = const Value.absent(),
    this.genre = const Value.absent(),
    this.year = const Value.absent(),
    this.starred = const Value.absent(),
    this.frequentRank = const Value.absent(),
    this.recentRank = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : sourceId = Value(sourceId),
       id = Value(id),
       name = Value(name),
       created = Value(created);
  static Insertable<models.Album> custom({
    Expression<int>? sourceId,
    Expression<String>? id,
    Expression<String>? artistId,
    Expression<String>? name,
    Expression<String>? albumArtist,
    Expression<DateTime>? created,
    Expression<String>? coverArt,
    Expression<String>? genre,
    Expression<int>? year,
    Expression<DateTime>? starred,
    Expression<int>? frequentRank,
    Expression<int>? recentRank,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (sourceId != null) 'source_id': sourceId,
      if (id != null) 'id': id,
      if (artistId != null) 'artist_id': artistId,
      if (name != null) 'name': name,
      if (albumArtist != null) 'album_artist': albumArtist,
      if (created != null) 'created': created,
      if (coverArt != null) 'cover_art': coverArt,
      if (genre != null) 'genre': genre,
      if (year != null) 'year': year,
      if (starred != null) 'starred': starred,
      if (frequentRank != null) 'frequent_rank': frequentRank,
      if (recentRank != null) 'recent_rank': recentRank,
      if (rowid != null) 'rowid': rowid,
    });
  }

  AlbumsCompanion copyWith({
    Value<int>? sourceId,
    Value<String>? id,
    Value<String?>? artistId,
    Value<String>? name,
    Value<String?>? albumArtist,
    Value<DateTime>? created,
    Value<String?>? coverArt,
    Value<String?>? genre,
    Value<int?>? year,
    Value<DateTime?>? starred,
    Value<int?>? frequentRank,
    Value<int?>? recentRank,
    Value<int>? rowid,
  }) {
    return AlbumsCompanion(
      sourceId: sourceId ?? this.sourceId,
      id: id ?? this.id,
      artistId: artistId ?? this.artistId,
      name: name ?? this.name,
      albumArtist: albumArtist ?? this.albumArtist,
      created: created ?? this.created,
      coverArt: coverArt ?? this.coverArt,
      genre: genre ?? this.genre,
      year: year ?? this.year,
      starred: starred ?? this.starred,
      frequentRank: frequentRank ?? this.frequentRank,
      recentRank: recentRank ?? this.recentRank,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (sourceId.present) {
      map['source_id'] = Variable<int>(sourceId.value);
    }
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (artistId.present) {
      map['artist_id'] = Variable<String>(artistId.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (albumArtist.present) {
      map['album_artist'] = Variable<String>(albumArtist.value);
    }
    if (created.present) {
      map['created'] = Variable<DateTime>(created.value);
    }
    if (coverArt.present) {
      map['cover_art'] = Variable<String>(coverArt.value);
    }
    if (genre.present) {
      map['genre'] = Variable<String>(genre.value);
    }
    if (year.present) {
      map['year'] = Variable<int>(year.value);
    }
    if (starred.present) {
      map['starred'] = Variable<DateTime>(starred.value);
    }
    if (frequentRank.present) {
      map['frequent_rank'] = Variable<int>(frequentRank.value);
    }
    if (recentRank.present) {
      map['recent_rank'] = Variable<int>(recentRank.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('AlbumsCompanion(')
          ..write('sourceId: $sourceId, ')
          ..write('id: $id, ')
          ..write('artistId: $artistId, ')
          ..write('name: $name, ')
          ..write('albumArtist: $albumArtist, ')
          ..write('created: $created, ')
          ..write('coverArt: $coverArt, ')
          ..write('genre: $genre, ')
          ..write('year: $year, ')
          ..write('starred: $starred, ')
          ..write('frequentRank: $frequentRank, ')
          ..write('recentRank: $recentRank, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class Playlists extends Table with TableInfo<Playlists, models.Playlist> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  Playlists(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _sourceIdMeta = const VerificationMeta(
    'sourceId',
  );
  late final GeneratedColumn<int> sourceId = GeneratedColumn<int>(
    'source_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
    'name',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL COLLATE NOCASE',
  );
  static const VerificationMeta _commentMeta = const VerificationMeta(
    'comment',
  );
  late final GeneratedColumn<String> comment = GeneratedColumn<String>(
    'comment',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: 'COLLATE NOCASE',
  );
  static const VerificationMeta _coverArtMeta = const VerificationMeta(
    'coverArt',
  );
  late final GeneratedColumn<String> coverArt = GeneratedColumn<String>(
    'cover_art',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _createdMeta = const VerificationMeta(
    'created',
  );
  late final GeneratedColumn<DateTime> created = GeneratedColumn<DateTime>(
    'created',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _changedMeta = const VerificationMeta(
    'changed',
  );
  late final GeneratedColumn<DateTime> changed = GeneratedColumn<DateTime>(
    'changed',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  @override
  List<GeneratedColumn> get $columns => [
    sourceId,
    id,
    name,
    comment,
    coverArt,
    created,
    changed,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'playlists';
  @override
  VerificationContext validateIntegrity(
    Insertable<models.Playlist> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('source_id')) {
      context.handle(
        _sourceIdMeta,
        sourceId.isAcceptableOrUnknown(data['source_id']!, _sourceIdMeta),
      );
    } else if (isInserting) {
      context.missing(_sourceIdMeta);
    }
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('name')) {
      context.handle(
        _nameMeta,
        name.isAcceptableOrUnknown(data['name']!, _nameMeta),
      );
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('comment')) {
      context.handle(
        _commentMeta,
        comment.isAcceptableOrUnknown(data['comment']!, _commentMeta),
      );
    }
    if (data.containsKey('cover_art')) {
      context.handle(
        _coverArtMeta,
        coverArt.isAcceptableOrUnknown(data['cover_art']!, _coverArtMeta),
      );
    }
    if (data.containsKey('created')) {
      context.handle(
        _createdMeta,
        created.isAcceptableOrUnknown(data['created']!, _createdMeta),
      );
    } else if (isInserting) {
      context.missing(_createdMeta);
    }
    if (data.containsKey('changed')) {
      context.handle(
        _changedMeta,
        changed.isAcceptableOrUnknown(data['changed']!, _changedMeta),
      );
    } else if (isInserting) {
      context.missing(_changedMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {sourceId, id};
  @override
  models.Playlist map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return models.Playlist(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      name: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}name'],
      )!,
      comment: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}comment'],
      ),
      created: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}created'],
      )!,
      changed: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}changed'],
      )!,
      coverArt: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}cover_art'],
      ),
    );
  }

  @override
  Playlists createAlias(String alias) {
    return Playlists(attachedDatabase, alias);
  }

  @override
  List<String> get customConstraints => const [
    'PRIMARY KEY(source_id, id)',
    'FOREIGN KEY(source_id)REFERENCES sources(id)ON DELETE CASCADE',
  ];
  @override
  bool get dontWriteConstraints => true;
}

class PlaylistsCompanion extends UpdateCompanion<models.Playlist> {
  final Value<int> sourceId;
  final Value<String> id;
  final Value<String> name;
  final Value<String?> comment;
  final Value<String?> coverArt;
  final Value<DateTime> created;
  final Value<DateTime> changed;
  final Value<int> rowid;
  const PlaylistsCompanion({
    this.sourceId = const Value.absent(),
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.comment = const Value.absent(),
    this.coverArt = const Value.absent(),
    this.created = const Value.absent(),
    this.changed = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  PlaylistsCompanion.insert({
    required int sourceId,
    required String id,
    required String name,
    this.comment = const Value.absent(),
    this.coverArt = const Value.absent(),
    required DateTime created,
    required DateTime changed,
    this.rowid = const Value.absent(),
  }) : sourceId = Value(sourceId),
       id = Value(id),
       name = Value(name),
       created = Value(created),
       changed = Value(changed);
  static Insertable<models.Playlist> custom({
    Expression<int>? sourceId,
    Expression<String>? id,
    Expression<String>? name,
    Expression<String>? comment,
    Expression<String>? coverArt,
    Expression<DateTime>? created,
    Expression<DateTime>? changed,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (sourceId != null) 'source_id': sourceId,
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (comment != null) 'comment': comment,
      if (coverArt != null) 'cover_art': coverArt,
      if (created != null) 'created': created,
      if (changed != null) 'changed': changed,
      if (rowid != null) 'rowid': rowid,
    });
  }

  PlaylistsCompanion copyWith({
    Value<int>? sourceId,
    Value<String>? id,
    Value<String>? name,
    Value<String?>? comment,
    Value<String?>? coverArt,
    Value<DateTime>? created,
    Value<DateTime>? changed,
    Value<int>? rowid,
  }) {
    return PlaylistsCompanion(
      sourceId: sourceId ?? this.sourceId,
      id: id ?? this.id,
      name: name ?? this.name,
      comment: comment ?? this.comment,
      coverArt: coverArt ?? this.coverArt,
      created: created ?? this.created,
      changed: changed ?? this.changed,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (sourceId.present) {
      map['source_id'] = Variable<int>(sourceId.value);
    }
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (comment.present) {
      map['comment'] = Variable<String>(comment.value);
    }
    if (coverArt.present) {
      map['cover_art'] = Variable<String>(coverArt.value);
    }
    if (created.present) {
      map['created'] = Variable<DateTime>(created.value);
    }
    if (changed.present) {
      map['changed'] = Variable<DateTime>(changed.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('PlaylistsCompanion(')
          ..write('sourceId: $sourceId, ')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('comment: $comment, ')
          ..write('coverArt: $coverArt, ')
          ..write('created: $created, ')
          ..write('changed: $changed, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class PlaylistSongs extends Table
    with TableInfo<PlaylistSongs, models.PlaylistSong> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  PlaylistSongs(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _sourceIdMeta = const VerificationMeta(
    'sourceId',
  );
  late final GeneratedColumn<int> sourceId = GeneratedColumn<int>(
    'source_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _playlistIdMeta = const VerificationMeta(
    'playlistId',
  );
  late final GeneratedColumn<String> playlistId = GeneratedColumn<String>(
    'playlist_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _songIdMeta = const VerificationMeta('songId');
  late final GeneratedColumn<String> songId = GeneratedColumn<String>(
    'song_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _positionMeta = const VerificationMeta(
    'position',
  );
  late final GeneratedColumn<int> position = GeneratedColumn<int>(
    'position',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  @override
  List<GeneratedColumn> get $columns => [
    sourceId,
    playlistId,
    songId,
    position,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'playlist_songs';
  @override
  VerificationContext validateIntegrity(
    Insertable<models.PlaylistSong> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('source_id')) {
      context.handle(
        _sourceIdMeta,
        sourceId.isAcceptableOrUnknown(data['source_id']!, _sourceIdMeta),
      );
    } else if (isInserting) {
      context.missing(_sourceIdMeta);
    }
    if (data.containsKey('playlist_id')) {
      context.handle(
        _playlistIdMeta,
        playlistId.isAcceptableOrUnknown(data['playlist_id']!, _playlistIdMeta),
      );
    } else if (isInserting) {
      context.missing(_playlistIdMeta);
    }
    if (data.containsKey('song_id')) {
      context.handle(
        _songIdMeta,
        songId.isAcceptableOrUnknown(data['song_id']!, _songIdMeta),
      );
    } else if (isInserting) {
      context.missing(_songIdMeta);
    }
    if (data.containsKey('position')) {
      context.handle(
        _positionMeta,
        position.isAcceptableOrUnknown(data['position']!, _positionMeta),
      );
    } else if (isInserting) {
      context.missing(_positionMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {sourceId, playlistId, position};
  @override
  models.PlaylistSong map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return models.PlaylistSong(
      playlistId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}playlist_id'],
      )!,
      songId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}song_id'],
      )!,
      position: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}position'],
      )!,
    );
  }

  @override
  PlaylistSongs createAlias(String alias) {
    return PlaylistSongs(attachedDatabase, alias);
  }

  @override
  List<String> get customConstraints => const [
    'PRIMARY KEY(source_id, playlist_id, position)',
    'FOREIGN KEY(source_id)REFERENCES sources(id)ON DELETE CASCADE',
  ];
  @override
  bool get dontWriteConstraints => true;
}

class PlaylistSongsCompanion extends UpdateCompanion<models.PlaylistSong> {
  final Value<int> sourceId;
  final Value<String> playlistId;
  final Value<String> songId;
  final Value<int> position;
  final Value<int> rowid;
  const PlaylistSongsCompanion({
    this.sourceId = const Value.absent(),
    this.playlistId = const Value.absent(),
    this.songId = const Value.absent(),
    this.position = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  PlaylistSongsCompanion.insert({
    required int sourceId,
    required String playlistId,
    required String songId,
    required int position,
    this.rowid = const Value.absent(),
  }) : sourceId = Value(sourceId),
       playlistId = Value(playlistId),
       songId = Value(songId),
       position = Value(position);
  static Insertable<models.PlaylistSong> custom({
    Expression<int>? sourceId,
    Expression<String>? playlistId,
    Expression<String>? songId,
    Expression<int>? position,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (sourceId != null) 'source_id': sourceId,
      if (playlistId != null) 'playlist_id': playlistId,
      if (songId != null) 'song_id': songId,
      if (position != null) 'position': position,
      if (rowid != null) 'rowid': rowid,
    });
  }

  PlaylistSongsCompanion copyWith({
    Value<int>? sourceId,
    Value<String>? playlistId,
    Value<String>? songId,
    Value<int>? position,
    Value<int>? rowid,
  }) {
    return PlaylistSongsCompanion(
      sourceId: sourceId ?? this.sourceId,
      playlistId: playlistId ?? this.playlistId,
      songId: songId ?? this.songId,
      position: position ?? this.position,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (sourceId.present) {
      map['source_id'] = Variable<int>(sourceId.value);
    }
    if (playlistId.present) {
      map['playlist_id'] = Variable<String>(playlistId.value);
    }
    if (songId.present) {
      map['song_id'] = Variable<String>(songId.value);
    }
    if (position.present) {
      map['position'] = Variable<int>(position.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('PlaylistSongsCompanion(')
          ..write('sourceId: $sourceId, ')
          ..write('playlistId: $playlistId, ')
          ..write('songId: $songId, ')
          ..write('position: $position, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class Songs extends Table with TableInfo<Songs, models.Song> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  Songs(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _sourceIdMeta = const VerificationMeta(
    'sourceId',
  );
  late final GeneratedColumn<int> sourceId = GeneratedColumn<int>(
    'source_id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL',
  );
  static const VerificationMeta _albumIdMeta = const VerificationMeta(
    'albumId',
  );
  late final GeneratedColumn<String> albumId = GeneratedColumn<String>(
    'album_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _artistIdMeta = const VerificationMeta(
    'artistId',
  );
  late final GeneratedColumn<String> artistId = GeneratedColumn<String>(
    'artist_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _titleMeta = const VerificationMeta('title');
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
    'title',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    $customConstraints: 'NOT NULL COLLATE NOCASE',
  );
  static const VerificationMeta _albumMeta = const VerificationMeta('album');
  late final GeneratedColumn<String> album = GeneratedColumn<String>(
    'album',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: 'COLLATE NOCASE',
  );
  static const VerificationMeta _artistMeta = const VerificationMeta('artist');
  late final GeneratedColumn<String> artist = GeneratedColumn<String>(
    'artist',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: 'COLLATE NOCASE',
  );
  late final GeneratedColumnWithTypeConverter<Duration?, int> duration =
      GeneratedColumn<int>(
        'duration',
        aliasedName,
        true,
        type: DriftSqlType.int,
        requiredDuringInsert: false,
        $customConstraints: '',
      ).withConverter<Duration?>(Songs.$converterdurationn);
  static const VerificationMeta _trackMeta = const VerificationMeta('track');
  late final GeneratedColumn<int> track = GeneratedColumn<int>(
    'track',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _discMeta = const VerificationMeta('disc');
  late final GeneratedColumn<int> disc = GeneratedColumn<int>(
    'disc',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _starredMeta = const VerificationMeta(
    'starred',
  );
  late final GeneratedColumn<DateTime> starred = GeneratedColumn<DateTime>(
    'starred',
    aliasedName,
    true,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  static const VerificationMeta _genreMeta = const VerificationMeta('genre');
  late final GeneratedColumn<String> genre = GeneratedColumn<String>(
    'genre',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
    $customConstraints: '',
  );
  @override
  List<GeneratedColumn> get $columns => [
    sourceId,
    id,
    albumId,
    artistId,
    title,
    album,
    artist,
    duration,
    track,
    disc,
    starred,
    genre,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'songs';
  @override
  VerificationContext validateIntegrity(
    Insertable<models.Song> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('source_id')) {
      context.handle(
        _sourceIdMeta,
        sourceId.isAcceptableOrUnknown(data['source_id']!, _sourceIdMeta),
      );
    } else if (isInserting) {
      context.missing(_sourceIdMeta);
    }
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('album_id')) {
      context.handle(
        _albumIdMeta,
        albumId.isAcceptableOrUnknown(data['album_id']!, _albumIdMeta),
      );
    }
    if (data.containsKey('artist_id')) {
      context.handle(
        _artistIdMeta,
        artistId.isAcceptableOrUnknown(data['artist_id']!, _artistIdMeta),
      );
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('album')) {
      context.handle(
        _albumMeta,
        album.isAcceptableOrUnknown(data['album']!, _albumMeta),
      );
    }
    if (data.containsKey('artist')) {
      context.handle(
        _artistMeta,
        artist.isAcceptableOrUnknown(data['artist']!, _artistMeta),
      );
    }
    if (data.containsKey('track')) {
      context.handle(
        _trackMeta,
        track.isAcceptableOrUnknown(data['track']!, _trackMeta),
      );
    }
    if (data.containsKey('disc')) {
      context.handle(
        _discMeta,
        disc.isAcceptableOrUnknown(data['disc']!, _discMeta),
      );
    }
    if (data.containsKey('starred')) {
      context.handle(
        _starredMeta,
        starred.isAcceptableOrUnknown(data['starred']!, _starredMeta),
      );
    }
    if (data.containsKey('genre')) {
      context.handle(
        _genreMeta,
        genre.isAcceptableOrUnknown(data['genre']!, _genreMeta),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {sourceId, id};
  @override
  models.Song map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return models.Song(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      albumId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}album_id'],
      ),
      artistId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}artist_id'],
      ),
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      artist: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}artist'],
      ),
      album: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}album'],
      ),
      duration: Songs.$converterdurationn.fromSql(
        attachedDatabase.typeMapping.read(
          DriftSqlType.int,
          data['${effectivePrefix}duration'],
        ),
      ),
      track: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}track'],
      ),
      disc: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}disc'],
      ),
      starred: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}starred'],
      ),
      genre: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}genre'],
      ),
    );
  }

  @override
  Songs createAlias(String alias) {
    return Songs(attachedDatabase, alias);
  }

  static TypeConverter<Duration, int> $converterduration =
      const DurationSecondsConverter();
  static TypeConverter<Duration?, int?> $converterdurationn =
      NullAwareTypeConverter.wrap($converterduration);
  @override
  List<String> get customConstraints => const [
    'PRIMARY KEY(source_id, id)',
    'FOREIGN KEY(source_id)REFERENCES sources(id)ON DELETE CASCADE',
  ];
  @override
  bool get dontWriteConstraints => true;
}

class SongsCompanion extends UpdateCompanion<models.Song> {
  final Value<int> sourceId;
  final Value<String> id;
  final Value<String?> albumId;
  final Value<String?> artistId;
  final Value<String> title;
  final Value<String?> album;
  final Value<String?> artist;
  final Value<Duration?> duration;
  final Value<int?> track;
  final Value<int?> disc;
  final Value<DateTime?> starred;
  final Value<String?> genre;
  final Value<int> rowid;
  const SongsCompanion({
    this.sourceId = const Value.absent(),
    this.id = const Value.absent(),
    this.albumId = const Value.absent(),
    this.artistId = const Value.absent(),
    this.title = const Value.absent(),
    this.album = const Value.absent(),
    this.artist = const Value.absent(),
    this.duration = const Value.absent(),
    this.track = const Value.absent(),
    this.disc = const Value.absent(),
    this.starred = const Value.absent(),
    this.genre = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  SongsCompanion.insert({
    required int sourceId,
    required String id,
    this.albumId = const Value.absent(),
    this.artistId = const Value.absent(),
    required String title,
    this.album = const Value.absent(),
    this.artist = const Value.absent(),
    this.duration = const Value.absent(),
    this.track = const Value.absent(),
    this.disc = const Value.absent(),
    this.starred = const Value.absent(),
    this.genre = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : sourceId = Value(sourceId),
       id = Value(id),
       title = Value(title);
  static Insertable<models.Song> custom({
    Expression<int>? sourceId,
    Expression<String>? id,
    Expression<String>? albumId,
    Expression<String>? artistId,
    Expression<String>? title,
    Expression<String>? album,
    Expression<String>? artist,
    Expression<int>? duration,
    Expression<int>? track,
    Expression<int>? disc,
    Expression<DateTime>? starred,
    Expression<String>? genre,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (sourceId != null) 'source_id': sourceId,
      if (id != null) 'id': id,
      if (albumId != null) 'album_id': albumId,
      if (artistId != null) 'artist_id': artistId,
      if (title != null) 'title': title,
      if (album != null) 'album': album,
      if (artist != null) 'artist': artist,
      if (duration != null) 'duration': duration,
      if (track != null) 'track': track,
      if (disc != null) 'disc': disc,
      if (starred != null) 'starred': starred,
      if (genre != null) 'genre': genre,
      if (rowid != null) 'rowid': rowid,
    });
  }

  SongsCompanion copyWith({
    Value<int>? sourceId,
    Value<String>? id,
    Value<String?>? albumId,
    Value<String?>? artistId,
    Value<String>? title,
    Value<String?>? album,
    Value<String?>? artist,
    Value<Duration?>? duration,
    Value<int?>? track,
    Value<int?>? disc,
    Value<DateTime?>? starred,
    Value<String?>? genre,
    Value<int>? rowid,
  }) {
    return SongsCompanion(
      sourceId: sourceId ?? this.sourceId,
      id: id ?? this.id,
      albumId: albumId ?? this.albumId,
      artistId: artistId ?? this.artistId,
      title: title ?? this.title,
      album: album ?? this.album,
      artist: artist ?? this.artist,
      duration: duration ?? this.duration,
      track: track ?? this.track,
      disc: disc ?? this.disc,
      starred: starred ?? this.starred,
      genre: genre ?? this.genre,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (sourceId.present) {
      map['source_id'] = Variable<int>(sourceId.value);
    }
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (albumId.present) {
      map['album_id'] = Variable<String>(albumId.value);
    }
    if (artistId.present) {
      map['artist_id'] = Variable<String>(artistId.value);
    }
    if (title.present) {
      map['title'] = Variable<String>(title.value);
    }
    if (album.present) {
      map['album'] = Variable<String>(album.value);
    }
    if (artist.present) {
      map['artist'] = Variable<String>(artist.value);
    }
    if (duration.present) {
      map['duration'] = Variable<int>(
        Songs.$converterdurationn.toSql(duration.value),
      );
    }
    if (track.present) {
      map['track'] = Variable<int>(track.value);
    }
    if (disc.present) {
      map['disc'] = Variable<int>(disc.value);
    }
    if (starred.present) {
      map['starred'] = Variable<DateTime>(starred.value);
    }
    if (genre.present) {
      map['genre'] = Variable<String>(genre.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SongsCompanion(')
          ..write('sourceId: $sourceId, ')
          ..write('id: $id, ')
          ..write('albumId: $albumId, ')
          ..write('artistId: $artistId, ')
          ..write('title: $title, ')
          ..write('album: $album, ')
          ..write('artist: $artist, ')
          ..write('duration: $duration, ')
          ..write('track: $track, ')
          ..write('disc: $disc, ')
          ..write('starred: $starred, ')
          ..write('genre: $genre, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

abstract class _$SubtracksDatabase extends GeneratedDatabase {
  _$SubtracksDatabase(QueryExecutor e) : super(e);
  $SubtracksDatabaseManager get managers => $SubtracksDatabaseManager(this);
  late final Sources sources = Sources(this);
  late final SubsonicSettings subsonicSettings = SubsonicSettings(this);
  late final Artists artists = Artists(this);
  late final Index artistsSourceId = Index(
    'artists_source_id',
    'CREATE INDEX artists_source_id ON artists (source_id)',
  );
  late final Albums albums = Albums(this);
  late final Index albumsSourceId = Index(
    'albums_source_id',
    'CREATE INDEX albums_source_id ON albums (source_id)',
  );
  late final Index albumsSourceIdArtistIdIdx = Index(
    'albums_source_id_artist_id_idx',
    'CREATE INDEX albums_source_id_artist_id_idx ON albums (source_id, artist_id)',
  );
  late final Playlists playlists = Playlists(this);
  late final Index playlistsSourceId = Index(
    'playlists_source_id',
    'CREATE INDEX playlists_source_id ON playlists (source_id)',
  );
  late final PlaylistSongs playlistSongs = PlaylistSongs(this);
  late final Index playlistSongsSourceIdPlaylistIdIdx = Index(
    'playlist_songs_source_id_playlist_id_idx',
    'CREATE INDEX playlist_songs_source_id_playlist_id_idx ON playlist_songs (source_id, playlist_id)',
  );
  late final Index playlistSongsSourceIdSongIdIdx = Index(
    'playlist_songs_source_id_song_id_idx',
    'CREATE INDEX playlist_songs_source_id_song_id_idx ON playlist_songs (source_id, song_id)',
  );
  late final Songs songs = Songs(this);
  late final Index songsSourceIdAlbumIdIdx = Index(
    'songs_source_id_album_id_idx',
    'CREATE INDEX songs_source_id_album_id_idx ON songs (source_id, album_id)',
  );
  late final Index songsSourceIdArtistIdIdx = Index(
    'songs_source_id_artist_id_idx',
    'CREATE INDEX songs_source_id_artist_id_idx ON songs (source_id, artist_id)',
  );
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
    sources,
    subsonicSettings,
    artists,
    artistsSourceId,
    albums,
    albumsSourceId,
    albumsSourceIdArtistIdIdx,
    playlists,
    playlistsSourceId,
    playlistSongs,
    playlistSongsSourceIdPlaylistIdIdx,
    playlistSongsSourceIdSongIdIdx,
    songs,
    songsSourceIdAlbumIdIdx,
    songsSourceIdArtistIdIdx,
  ];
  @override
  StreamQueryUpdateRules get streamUpdateRules => const StreamQueryUpdateRules([
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'sources',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [TableUpdate('subsonic_settings', kind: UpdateKind.delete)],
    ),
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'sources',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [TableUpdate('artists', kind: UpdateKind.delete)],
    ),
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'sources',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [TableUpdate('albums', kind: UpdateKind.delete)],
    ),
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'sources',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [TableUpdate('playlists', kind: UpdateKind.delete)],
    ),
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'sources',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [TableUpdate('playlist_songs', kind: UpdateKind.delete)],
    ),
    WritePropagation(
      on: TableUpdateQuery.onTableName(
        'sources',
        limitUpdateKind: UpdateKind.delete,
      ),
      result: [TableUpdate('songs', kind: UpdateKind.delete)],
    ),
  ]);
}

typedef $SourcesCreateCompanionBuilder =
    SourcesCompanion Function({
      Value<int> id,
      required String name,
      Value<bool?> isActive,
      Value<DateTime> createdAt,
    });
typedef $SourcesUpdateCompanionBuilder =
    SourcesCompanion Function({
      Value<int> id,
      Value<String> name,
      Value<bool?> isActive,
      Value<DateTime> createdAt,
    });

class $SourcesFilterComposer extends Composer<_$SubtracksDatabase, Sources> {
  $SourcesFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get isActive => $composableBuilder(
    column: $table.isActive,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $SourcesOrderingComposer extends Composer<_$SubtracksDatabase, Sources> {
  $SourcesOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get isActive => $composableBuilder(
    column: $table.isActive,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $SourcesAnnotationComposer
    extends Composer<_$SubtracksDatabase, Sources> {
  $SourcesAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<bool> get isActive =>
      $composableBuilder(column: $table.isActive, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);
}

class $SourcesTableManager
    extends
        RootTableManager<
          _$SubtracksDatabase,
          Sources,
          Source,
          $SourcesFilterComposer,
          $SourcesOrderingComposer,
          $SourcesAnnotationComposer,
          $SourcesCreateCompanionBuilder,
          $SourcesUpdateCompanionBuilder,
          (Source, BaseReferences<_$SubtracksDatabase, Sources, Source>),
          Source,
          PrefetchHooks Function()
        > {
  $SourcesTableManager(_$SubtracksDatabase db, Sources table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $SourcesFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $SourcesOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $SourcesAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<String> name = const Value.absent(),
                Value<bool?> isActive = const Value.absent(),
                Value<DateTime> createdAt = const Value.absent(),
              }) => SourcesCompanion(
                id: id,
                name: name,
                isActive: isActive,
                createdAt: createdAt,
              ),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                required String name,
                Value<bool?> isActive = const Value.absent(),
                Value<DateTime> createdAt = const Value.absent(),
              }) => SourcesCompanion.insert(
                id: id,
                name: name,
                isActive: isActive,
                createdAt: createdAt,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $SourcesProcessedTableManager =
    ProcessedTableManager<
      _$SubtracksDatabase,
      Sources,
      Source,
      $SourcesFilterComposer,
      $SourcesOrderingComposer,
      $SourcesAnnotationComposer,
      $SourcesCreateCompanionBuilder,
      $SourcesUpdateCompanionBuilder,
      (Source, BaseReferences<_$SubtracksDatabase, Sources, Source>),
      Source,
      PrefetchHooks Function()
    >;
typedef $SubsonicSettingsCreateCompanionBuilder =
    SubsonicSettingsCompanion Function({
      Value<int> sourceId,
      required Uri address,
      required String username,
      required String password,
      Value<bool> useTokenAuth,
    });
typedef $SubsonicSettingsUpdateCompanionBuilder =
    SubsonicSettingsCompanion Function({
      Value<int> sourceId,
      Value<Uri> address,
      Value<String> username,
      Value<String> password,
      Value<bool> useTokenAuth,
    });

class $SubsonicSettingsFilterComposer
    extends Composer<_$SubtracksDatabase, SubsonicSettings> {
  $SubsonicSettingsFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnWithTypeConverterFilters<Uri, Uri, String> get address =>
      $composableBuilder(
        column: $table.address,
        builder: (column) => ColumnWithTypeConverterFilters(column),
      );

  ColumnFilters<String> get username => $composableBuilder(
    column: $table.username,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get password => $composableBuilder(
    column: $table.password,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get useTokenAuth => $composableBuilder(
    column: $table.useTokenAuth,
    builder: (column) => ColumnFilters(column),
  );
}

class $SubsonicSettingsOrderingComposer
    extends Composer<_$SubtracksDatabase, SubsonicSettings> {
  $SubsonicSettingsOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get address => $composableBuilder(
    column: $table.address,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get username => $composableBuilder(
    column: $table.username,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get password => $composableBuilder(
    column: $table.password,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get useTokenAuth => $composableBuilder(
    column: $table.useTokenAuth,
    builder: (column) => ColumnOrderings(column),
  );
}

class $SubsonicSettingsAnnotationComposer
    extends Composer<_$SubtracksDatabase, SubsonicSettings> {
  $SubsonicSettingsAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get sourceId =>
      $composableBuilder(column: $table.sourceId, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Uri, String> get address =>
      $composableBuilder(column: $table.address, builder: (column) => column);

  GeneratedColumn<String> get username =>
      $composableBuilder(column: $table.username, builder: (column) => column);

  GeneratedColumn<String> get password =>
      $composableBuilder(column: $table.password, builder: (column) => column);

  GeneratedColumn<bool> get useTokenAuth => $composableBuilder(
    column: $table.useTokenAuth,
    builder: (column) => column,
  );
}

class $SubsonicSettingsTableManager
    extends
        RootTableManager<
          _$SubtracksDatabase,
          SubsonicSettings,
          SubsonicSetting,
          $SubsonicSettingsFilterComposer,
          $SubsonicSettingsOrderingComposer,
          $SubsonicSettingsAnnotationComposer,
          $SubsonicSettingsCreateCompanionBuilder,
          $SubsonicSettingsUpdateCompanionBuilder,
          (
            SubsonicSetting,
            BaseReferences<
              _$SubtracksDatabase,
              SubsonicSettings,
              SubsonicSetting
            >,
          ),
          SubsonicSetting,
          PrefetchHooks Function()
        > {
  $SubsonicSettingsTableManager(_$SubtracksDatabase db, SubsonicSettings table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $SubsonicSettingsFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $SubsonicSettingsOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $SubsonicSettingsAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> sourceId = const Value.absent(),
                Value<Uri> address = const Value.absent(),
                Value<String> username = const Value.absent(),
                Value<String> password = const Value.absent(),
                Value<bool> useTokenAuth = const Value.absent(),
              }) => SubsonicSettingsCompanion(
                sourceId: sourceId,
                address: address,
                username: username,
                password: password,
                useTokenAuth: useTokenAuth,
              ),
          createCompanionCallback:
              ({
                Value<int> sourceId = const Value.absent(),
                required Uri address,
                required String username,
                required String password,
                Value<bool> useTokenAuth = const Value.absent(),
              }) => SubsonicSettingsCompanion.insert(
                sourceId: sourceId,
                address: address,
                username: username,
                password: password,
                useTokenAuth: useTokenAuth,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $SubsonicSettingsProcessedTableManager =
    ProcessedTableManager<
      _$SubtracksDatabase,
      SubsonicSettings,
      SubsonicSetting,
      $SubsonicSettingsFilterComposer,
      $SubsonicSettingsOrderingComposer,
      $SubsonicSettingsAnnotationComposer,
      $SubsonicSettingsCreateCompanionBuilder,
      $SubsonicSettingsUpdateCompanionBuilder,
      (
        SubsonicSetting,
        BaseReferences<_$SubtracksDatabase, SubsonicSettings, SubsonicSetting>,
      ),
      SubsonicSetting,
      PrefetchHooks Function()
    >;
typedef $ArtistsCreateCompanionBuilder =
    ArtistsCompanion Function({
      required int sourceId,
      required String id,
      required String name,
      Value<DateTime?> starred,
      Value<int> rowid,
    });
typedef $ArtistsUpdateCompanionBuilder =
    ArtistsCompanion Function({
      Value<int> sourceId,
      Value<String> id,
      Value<String> name,
      Value<DateTime?> starred,
      Value<int> rowid,
    });

class $ArtistsFilterComposer extends Composer<_$SubtracksDatabase, Artists> {
  $ArtistsFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get starred => $composableBuilder(
    column: $table.starred,
    builder: (column) => ColumnFilters(column),
  );
}

class $ArtistsOrderingComposer extends Composer<_$SubtracksDatabase, Artists> {
  $ArtistsOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get starred => $composableBuilder(
    column: $table.starred,
    builder: (column) => ColumnOrderings(column),
  );
}

class $ArtistsAnnotationComposer
    extends Composer<_$SubtracksDatabase, Artists> {
  $ArtistsAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get sourceId =>
      $composableBuilder(column: $table.sourceId, builder: (column) => column);

  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<DateTime> get starred =>
      $composableBuilder(column: $table.starred, builder: (column) => column);
}

class $ArtistsTableManager
    extends
        RootTableManager<
          _$SubtracksDatabase,
          Artists,
          models.Artist,
          $ArtistsFilterComposer,
          $ArtistsOrderingComposer,
          $ArtistsAnnotationComposer,
          $ArtistsCreateCompanionBuilder,
          $ArtistsUpdateCompanionBuilder,
          (
            models.Artist,
            BaseReferences<_$SubtracksDatabase, Artists, models.Artist>,
          ),
          models.Artist,
          PrefetchHooks Function()
        > {
  $ArtistsTableManager(_$SubtracksDatabase db, Artists table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $ArtistsFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $ArtistsOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $ArtistsAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> sourceId = const Value.absent(),
                Value<String> id = const Value.absent(),
                Value<String> name = const Value.absent(),
                Value<DateTime?> starred = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => ArtistsCompanion(
                sourceId: sourceId,
                id: id,
                name: name,
                starred: starred,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required int sourceId,
                required String id,
                required String name,
                Value<DateTime?> starred = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => ArtistsCompanion.insert(
                sourceId: sourceId,
                id: id,
                name: name,
                starred: starred,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $ArtistsProcessedTableManager =
    ProcessedTableManager<
      _$SubtracksDatabase,
      Artists,
      models.Artist,
      $ArtistsFilterComposer,
      $ArtistsOrderingComposer,
      $ArtistsAnnotationComposer,
      $ArtistsCreateCompanionBuilder,
      $ArtistsUpdateCompanionBuilder,
      (
        models.Artist,
        BaseReferences<_$SubtracksDatabase, Artists, models.Artist>,
      ),
      models.Artist,
      PrefetchHooks Function()
    >;
typedef $AlbumsCreateCompanionBuilder =
    AlbumsCompanion Function({
      required int sourceId,
      required String id,
      Value<String?> artistId,
      required String name,
      Value<String?> albumArtist,
      required DateTime created,
      Value<String?> coverArt,
      Value<String?> genre,
      Value<int?> year,
      Value<DateTime?> starred,
      Value<int?> frequentRank,
      Value<int?> recentRank,
      Value<int> rowid,
    });
typedef $AlbumsUpdateCompanionBuilder =
    AlbumsCompanion Function({
      Value<int> sourceId,
      Value<String> id,
      Value<String?> artistId,
      Value<String> name,
      Value<String?> albumArtist,
      Value<DateTime> created,
      Value<String?> coverArt,
      Value<String?> genre,
      Value<int?> year,
      Value<DateTime?> starred,
      Value<int?> frequentRank,
      Value<int?> recentRank,
      Value<int> rowid,
    });

class $AlbumsFilterComposer extends Composer<_$SubtracksDatabase, Albums> {
  $AlbumsFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get artistId => $composableBuilder(
    column: $table.artistId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get albumArtist => $composableBuilder(
    column: $table.albumArtist,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get created => $composableBuilder(
    column: $table.created,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get coverArt => $composableBuilder(
    column: $table.coverArt,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get genre => $composableBuilder(
    column: $table.genre,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get year => $composableBuilder(
    column: $table.year,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get starred => $composableBuilder(
    column: $table.starred,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get frequentRank => $composableBuilder(
    column: $table.frequentRank,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get recentRank => $composableBuilder(
    column: $table.recentRank,
    builder: (column) => ColumnFilters(column),
  );
}

class $AlbumsOrderingComposer extends Composer<_$SubtracksDatabase, Albums> {
  $AlbumsOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get artistId => $composableBuilder(
    column: $table.artistId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get albumArtist => $composableBuilder(
    column: $table.albumArtist,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get created => $composableBuilder(
    column: $table.created,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get coverArt => $composableBuilder(
    column: $table.coverArt,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get genre => $composableBuilder(
    column: $table.genre,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get year => $composableBuilder(
    column: $table.year,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get starred => $composableBuilder(
    column: $table.starred,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get frequentRank => $composableBuilder(
    column: $table.frequentRank,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get recentRank => $composableBuilder(
    column: $table.recentRank,
    builder: (column) => ColumnOrderings(column),
  );
}

class $AlbumsAnnotationComposer extends Composer<_$SubtracksDatabase, Albums> {
  $AlbumsAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get sourceId =>
      $composableBuilder(column: $table.sourceId, builder: (column) => column);

  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get artistId =>
      $composableBuilder(column: $table.artistId, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<String> get albumArtist => $composableBuilder(
    column: $table.albumArtist,
    builder: (column) => column,
  );

  GeneratedColumn<DateTime> get created =>
      $composableBuilder(column: $table.created, builder: (column) => column);

  GeneratedColumn<String> get coverArt =>
      $composableBuilder(column: $table.coverArt, builder: (column) => column);

  GeneratedColumn<String> get genre =>
      $composableBuilder(column: $table.genre, builder: (column) => column);

  GeneratedColumn<int> get year =>
      $composableBuilder(column: $table.year, builder: (column) => column);

  GeneratedColumn<DateTime> get starred =>
      $composableBuilder(column: $table.starred, builder: (column) => column);

  GeneratedColumn<int> get frequentRank => $composableBuilder(
    column: $table.frequentRank,
    builder: (column) => column,
  );

  GeneratedColumn<int> get recentRank => $composableBuilder(
    column: $table.recentRank,
    builder: (column) => column,
  );
}

class $AlbumsTableManager
    extends
        RootTableManager<
          _$SubtracksDatabase,
          Albums,
          models.Album,
          $AlbumsFilterComposer,
          $AlbumsOrderingComposer,
          $AlbumsAnnotationComposer,
          $AlbumsCreateCompanionBuilder,
          $AlbumsUpdateCompanionBuilder,
          (
            models.Album,
            BaseReferences<_$SubtracksDatabase, Albums, models.Album>,
          ),
          models.Album,
          PrefetchHooks Function()
        > {
  $AlbumsTableManager(_$SubtracksDatabase db, Albums table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $AlbumsFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $AlbumsOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $AlbumsAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> sourceId = const Value.absent(),
                Value<String> id = const Value.absent(),
                Value<String?> artistId = const Value.absent(),
                Value<String> name = const Value.absent(),
                Value<String?> albumArtist = const Value.absent(),
                Value<DateTime> created = const Value.absent(),
                Value<String?> coverArt = const Value.absent(),
                Value<String?> genre = const Value.absent(),
                Value<int?> year = const Value.absent(),
                Value<DateTime?> starred = const Value.absent(),
                Value<int?> frequentRank = const Value.absent(),
                Value<int?> recentRank = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => AlbumsCompanion(
                sourceId: sourceId,
                id: id,
                artistId: artistId,
                name: name,
                albumArtist: albumArtist,
                created: created,
                coverArt: coverArt,
                genre: genre,
                year: year,
                starred: starred,
                frequentRank: frequentRank,
                recentRank: recentRank,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required int sourceId,
                required String id,
                Value<String?> artistId = const Value.absent(),
                required String name,
                Value<String?> albumArtist = const Value.absent(),
                required DateTime created,
                Value<String?> coverArt = const Value.absent(),
                Value<String?> genre = const Value.absent(),
                Value<int?> year = const Value.absent(),
                Value<DateTime?> starred = const Value.absent(),
                Value<int?> frequentRank = const Value.absent(),
                Value<int?> recentRank = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => AlbumsCompanion.insert(
                sourceId: sourceId,
                id: id,
                artistId: artistId,
                name: name,
                albumArtist: albumArtist,
                created: created,
                coverArt: coverArt,
                genre: genre,
                year: year,
                starred: starred,
                frequentRank: frequentRank,
                recentRank: recentRank,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $AlbumsProcessedTableManager =
    ProcessedTableManager<
      _$SubtracksDatabase,
      Albums,
      models.Album,
      $AlbumsFilterComposer,
      $AlbumsOrderingComposer,
      $AlbumsAnnotationComposer,
      $AlbumsCreateCompanionBuilder,
      $AlbumsUpdateCompanionBuilder,
      (models.Album, BaseReferences<_$SubtracksDatabase, Albums, models.Album>),
      models.Album,
      PrefetchHooks Function()
    >;
typedef $PlaylistsCreateCompanionBuilder =
    PlaylistsCompanion Function({
      required int sourceId,
      required String id,
      required String name,
      Value<String?> comment,
      Value<String?> coverArt,
      required DateTime created,
      required DateTime changed,
      Value<int> rowid,
    });
typedef $PlaylistsUpdateCompanionBuilder =
    PlaylistsCompanion Function({
      Value<int> sourceId,
      Value<String> id,
      Value<String> name,
      Value<String?> comment,
      Value<String?> coverArt,
      Value<DateTime> created,
      Value<DateTime> changed,
      Value<int> rowid,
    });

class $PlaylistsFilterComposer
    extends Composer<_$SubtracksDatabase, Playlists> {
  $PlaylistsFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get comment => $composableBuilder(
    column: $table.comment,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get coverArt => $composableBuilder(
    column: $table.coverArt,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get created => $composableBuilder(
    column: $table.created,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get changed => $composableBuilder(
    column: $table.changed,
    builder: (column) => ColumnFilters(column),
  );
}

class $PlaylistsOrderingComposer
    extends Composer<_$SubtracksDatabase, Playlists> {
  $PlaylistsOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get comment => $composableBuilder(
    column: $table.comment,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get coverArt => $composableBuilder(
    column: $table.coverArt,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get created => $composableBuilder(
    column: $table.created,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get changed => $composableBuilder(
    column: $table.changed,
    builder: (column) => ColumnOrderings(column),
  );
}

class $PlaylistsAnnotationComposer
    extends Composer<_$SubtracksDatabase, Playlists> {
  $PlaylistsAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get sourceId =>
      $composableBuilder(column: $table.sourceId, builder: (column) => column);

  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<String> get comment =>
      $composableBuilder(column: $table.comment, builder: (column) => column);

  GeneratedColumn<String> get coverArt =>
      $composableBuilder(column: $table.coverArt, builder: (column) => column);

  GeneratedColumn<DateTime> get created =>
      $composableBuilder(column: $table.created, builder: (column) => column);

  GeneratedColumn<DateTime> get changed =>
      $composableBuilder(column: $table.changed, builder: (column) => column);
}

class $PlaylistsTableManager
    extends
        RootTableManager<
          _$SubtracksDatabase,
          Playlists,
          models.Playlist,
          $PlaylistsFilterComposer,
          $PlaylistsOrderingComposer,
          $PlaylistsAnnotationComposer,
          $PlaylistsCreateCompanionBuilder,
          $PlaylistsUpdateCompanionBuilder,
          (
            models.Playlist,
            BaseReferences<_$SubtracksDatabase, Playlists, models.Playlist>,
          ),
          models.Playlist,
          PrefetchHooks Function()
        > {
  $PlaylistsTableManager(_$SubtracksDatabase db, Playlists table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $PlaylistsFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $PlaylistsOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $PlaylistsAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> sourceId = const Value.absent(),
                Value<String> id = const Value.absent(),
                Value<String> name = const Value.absent(),
                Value<String?> comment = const Value.absent(),
                Value<String?> coverArt = const Value.absent(),
                Value<DateTime> created = const Value.absent(),
                Value<DateTime> changed = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => PlaylistsCompanion(
                sourceId: sourceId,
                id: id,
                name: name,
                comment: comment,
                coverArt: coverArt,
                created: created,
                changed: changed,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required int sourceId,
                required String id,
                required String name,
                Value<String?> comment = const Value.absent(),
                Value<String?> coverArt = const Value.absent(),
                required DateTime created,
                required DateTime changed,
                Value<int> rowid = const Value.absent(),
              }) => PlaylistsCompanion.insert(
                sourceId: sourceId,
                id: id,
                name: name,
                comment: comment,
                coverArt: coverArt,
                created: created,
                changed: changed,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $PlaylistsProcessedTableManager =
    ProcessedTableManager<
      _$SubtracksDatabase,
      Playlists,
      models.Playlist,
      $PlaylistsFilterComposer,
      $PlaylistsOrderingComposer,
      $PlaylistsAnnotationComposer,
      $PlaylistsCreateCompanionBuilder,
      $PlaylistsUpdateCompanionBuilder,
      (
        models.Playlist,
        BaseReferences<_$SubtracksDatabase, Playlists, models.Playlist>,
      ),
      models.Playlist,
      PrefetchHooks Function()
    >;
typedef $PlaylistSongsCreateCompanionBuilder =
    PlaylistSongsCompanion Function({
      required int sourceId,
      required String playlistId,
      required String songId,
      required int position,
      Value<int> rowid,
    });
typedef $PlaylistSongsUpdateCompanionBuilder =
    PlaylistSongsCompanion Function({
      Value<int> sourceId,
      Value<String> playlistId,
      Value<String> songId,
      Value<int> position,
      Value<int> rowid,
    });

class $PlaylistSongsFilterComposer
    extends Composer<_$SubtracksDatabase, PlaylistSongs> {
  $PlaylistSongsFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get playlistId => $composableBuilder(
    column: $table.playlistId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get songId => $composableBuilder(
    column: $table.songId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get position => $composableBuilder(
    column: $table.position,
    builder: (column) => ColumnFilters(column),
  );
}

class $PlaylistSongsOrderingComposer
    extends Composer<_$SubtracksDatabase, PlaylistSongs> {
  $PlaylistSongsOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get playlistId => $composableBuilder(
    column: $table.playlistId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get songId => $composableBuilder(
    column: $table.songId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get position => $composableBuilder(
    column: $table.position,
    builder: (column) => ColumnOrderings(column),
  );
}

class $PlaylistSongsAnnotationComposer
    extends Composer<_$SubtracksDatabase, PlaylistSongs> {
  $PlaylistSongsAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get sourceId =>
      $composableBuilder(column: $table.sourceId, builder: (column) => column);

  GeneratedColumn<String> get playlistId => $composableBuilder(
    column: $table.playlistId,
    builder: (column) => column,
  );

  GeneratedColumn<String> get songId =>
      $composableBuilder(column: $table.songId, builder: (column) => column);

  GeneratedColumn<int> get position =>
      $composableBuilder(column: $table.position, builder: (column) => column);
}

class $PlaylistSongsTableManager
    extends
        RootTableManager<
          _$SubtracksDatabase,
          PlaylistSongs,
          models.PlaylistSong,
          $PlaylistSongsFilterComposer,
          $PlaylistSongsOrderingComposer,
          $PlaylistSongsAnnotationComposer,
          $PlaylistSongsCreateCompanionBuilder,
          $PlaylistSongsUpdateCompanionBuilder,
          (
            models.PlaylistSong,
            BaseReferences<
              _$SubtracksDatabase,
              PlaylistSongs,
              models.PlaylistSong
            >,
          ),
          models.PlaylistSong,
          PrefetchHooks Function()
        > {
  $PlaylistSongsTableManager(_$SubtracksDatabase db, PlaylistSongs table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $PlaylistSongsFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $PlaylistSongsOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $PlaylistSongsAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> sourceId = const Value.absent(),
                Value<String> playlistId = const Value.absent(),
                Value<String> songId = const Value.absent(),
                Value<int> position = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => PlaylistSongsCompanion(
                sourceId: sourceId,
                playlistId: playlistId,
                songId: songId,
                position: position,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required int sourceId,
                required String playlistId,
                required String songId,
                required int position,
                Value<int> rowid = const Value.absent(),
              }) => PlaylistSongsCompanion.insert(
                sourceId: sourceId,
                playlistId: playlistId,
                songId: songId,
                position: position,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $PlaylistSongsProcessedTableManager =
    ProcessedTableManager<
      _$SubtracksDatabase,
      PlaylistSongs,
      models.PlaylistSong,
      $PlaylistSongsFilterComposer,
      $PlaylistSongsOrderingComposer,
      $PlaylistSongsAnnotationComposer,
      $PlaylistSongsCreateCompanionBuilder,
      $PlaylistSongsUpdateCompanionBuilder,
      (
        models.PlaylistSong,
        BaseReferences<_$SubtracksDatabase, PlaylistSongs, models.PlaylistSong>,
      ),
      models.PlaylistSong,
      PrefetchHooks Function()
    >;
typedef $SongsCreateCompanionBuilder =
    SongsCompanion Function({
      required int sourceId,
      required String id,
      Value<String?> albumId,
      Value<String?> artistId,
      required String title,
      Value<String?> album,
      Value<String?> artist,
      Value<Duration?> duration,
      Value<int?> track,
      Value<int?> disc,
      Value<DateTime?> starred,
      Value<String?> genre,
      Value<int> rowid,
    });
typedef $SongsUpdateCompanionBuilder =
    SongsCompanion Function({
      Value<int> sourceId,
      Value<String> id,
      Value<String?> albumId,
      Value<String?> artistId,
      Value<String> title,
      Value<String?> album,
      Value<String?> artist,
      Value<Duration?> duration,
      Value<int?> track,
      Value<int?> disc,
      Value<DateTime?> starred,
      Value<String?> genre,
      Value<int> rowid,
    });

class $SongsFilterComposer extends Composer<_$SubtracksDatabase, Songs> {
  $SongsFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get albumId => $composableBuilder(
    column: $table.albumId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get artistId => $composableBuilder(
    column: $table.artistId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get album => $composableBuilder(
    column: $table.album,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get artist => $composableBuilder(
    column: $table.artist,
    builder: (column) => ColumnFilters(column),
  );

  ColumnWithTypeConverterFilters<Duration?, Duration, int> get duration =>
      $composableBuilder(
        column: $table.duration,
        builder: (column) => ColumnWithTypeConverterFilters(column),
      );

  ColumnFilters<int> get track => $composableBuilder(
    column: $table.track,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get disc => $composableBuilder(
    column: $table.disc,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get starred => $composableBuilder(
    column: $table.starred,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get genre => $composableBuilder(
    column: $table.genre,
    builder: (column) => ColumnFilters(column),
  );
}

class $SongsOrderingComposer extends Composer<_$SubtracksDatabase, Songs> {
  $SongsOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get sourceId => $composableBuilder(
    column: $table.sourceId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get albumId => $composableBuilder(
    column: $table.albumId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get artistId => $composableBuilder(
    column: $table.artistId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get album => $composableBuilder(
    column: $table.album,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get artist => $composableBuilder(
    column: $table.artist,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get duration => $composableBuilder(
    column: $table.duration,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get track => $composableBuilder(
    column: $table.track,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get disc => $composableBuilder(
    column: $table.disc,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get starred => $composableBuilder(
    column: $table.starred,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get genre => $composableBuilder(
    column: $table.genre,
    builder: (column) => ColumnOrderings(column),
  );
}

class $SongsAnnotationComposer extends Composer<_$SubtracksDatabase, Songs> {
  $SongsAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get sourceId =>
      $composableBuilder(column: $table.sourceId, builder: (column) => column);

  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get albumId =>
      $composableBuilder(column: $table.albumId, builder: (column) => column);

  GeneratedColumn<String> get artistId =>
      $composableBuilder(column: $table.artistId, builder: (column) => column);

  GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  GeneratedColumn<String> get album =>
      $composableBuilder(column: $table.album, builder: (column) => column);

  GeneratedColumn<String> get artist =>
      $composableBuilder(column: $table.artist, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Duration?, int> get duration =>
      $composableBuilder(column: $table.duration, builder: (column) => column);

  GeneratedColumn<int> get track =>
      $composableBuilder(column: $table.track, builder: (column) => column);

  GeneratedColumn<int> get disc =>
      $composableBuilder(column: $table.disc, builder: (column) => column);

  GeneratedColumn<DateTime> get starred =>
      $composableBuilder(column: $table.starred, builder: (column) => column);

  GeneratedColumn<String> get genre =>
      $composableBuilder(column: $table.genre, builder: (column) => column);
}

class $SongsTableManager
    extends
        RootTableManager<
          _$SubtracksDatabase,
          Songs,
          models.Song,
          $SongsFilterComposer,
          $SongsOrderingComposer,
          $SongsAnnotationComposer,
          $SongsCreateCompanionBuilder,
          $SongsUpdateCompanionBuilder,
          (
            models.Song,
            BaseReferences<_$SubtracksDatabase, Songs, models.Song>,
          ),
          models.Song,
          PrefetchHooks Function()
        > {
  $SongsTableManager(_$SubtracksDatabase db, Songs table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $SongsFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $SongsOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $SongsAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> sourceId = const Value.absent(),
                Value<String> id = const Value.absent(),
                Value<String?> albumId = const Value.absent(),
                Value<String?> artistId = const Value.absent(),
                Value<String> title = const Value.absent(),
                Value<String?> album = const Value.absent(),
                Value<String?> artist = const Value.absent(),
                Value<Duration?> duration = const Value.absent(),
                Value<int?> track = const Value.absent(),
                Value<int?> disc = const Value.absent(),
                Value<DateTime?> starred = const Value.absent(),
                Value<String?> genre = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => SongsCompanion(
                sourceId: sourceId,
                id: id,
                albumId: albumId,
                artistId: artistId,
                title: title,
                album: album,
                artist: artist,
                duration: duration,
                track: track,
                disc: disc,
                starred: starred,
                genre: genre,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required int sourceId,
                required String id,
                Value<String?> albumId = const Value.absent(),
                Value<String?> artistId = const Value.absent(),
                required String title,
                Value<String?> album = const Value.absent(),
                Value<String?> artist = const Value.absent(),
                Value<Duration?> duration = const Value.absent(),
                Value<int?> track = const Value.absent(),
                Value<int?> disc = const Value.absent(),
                Value<DateTime?> starred = const Value.absent(),
                Value<String?> genre = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => SongsCompanion.insert(
                sourceId: sourceId,
                id: id,
                albumId: albumId,
                artistId: artistId,
                title: title,
                album: album,
                artist: artist,
                duration: duration,
                track: track,
                disc: disc,
                starred: starred,
                genre: genre,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $SongsProcessedTableManager =
    ProcessedTableManager<
      _$SubtracksDatabase,
      Songs,
      models.Song,
      $SongsFilterComposer,
      $SongsOrderingComposer,
      $SongsAnnotationComposer,
      $SongsCreateCompanionBuilder,
      $SongsUpdateCompanionBuilder,
      (models.Song, BaseReferences<_$SubtracksDatabase, Songs, models.Song>),
      models.Song,
      PrefetchHooks Function()
    >;

class $SubtracksDatabaseManager {
  final _$SubtracksDatabase _db;
  $SubtracksDatabaseManager(this._db);
  $SourcesTableManager get sources => $SourcesTableManager(_db, _db.sources);
  $SubsonicSettingsTableManager get subsonicSettings =>
      $SubsonicSettingsTableManager(_db, _db.subsonicSettings);
  $ArtistsTableManager get artists => $ArtistsTableManager(_db, _db.artists);
  $AlbumsTableManager get albums => $AlbumsTableManager(_db, _db.albums);
  $PlaylistsTableManager get playlists =>
      $PlaylistsTableManager(_db, _db.playlists);
  $PlaylistSongsTableManager get playlistSongs =>
      $PlaylistSongsTableManager(_db, _db.playlistSongs);
  $SongsTableManager get songs => $SongsTableManager(_db, _db.songs);
}
