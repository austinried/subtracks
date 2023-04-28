// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'query.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$_Pagination _$$_PaginationFromJson(Map<String, dynamic> json) =>
    _$_Pagination(
      limit: json['limit'] as int,
      offset: json['offset'] as int? ?? 0,
    );

Map<String, dynamic> _$$_PaginationToJson(_$_Pagination instance) =>
    <String, dynamic>{
      'limit': instance.limit,
      'offset': instance.offset,
    };

_$_SortBy _$$_SortByFromJson(Map<String, dynamic> json) => _$_SortBy(
      column: json['column'] as String,
      dir: $enumDecodeNullable(_$SortDirectionEnumMap, json['dir']) ??
          SortDirection.asc,
    );

Map<String, dynamic> _$$_SortByToJson(_$_SortBy instance) => <String, dynamic>{
      'column': instance.column,
      'dir': _$SortDirectionEnumMap[instance.dir]!,
    };

const _$SortDirectionEnumMap = {
  SortDirection.asc: 'asc',
  SortDirection.desc: 'desc',
};

_$_FilterWithEquals _$$_FilterWithEqualsFromJson(Map<String, dynamic> json) =>
    _$_FilterWithEquals(
      column: json['column'] as String,
      value: json['value'] as String,
      invert: json['invert'] as bool? ?? false,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$_FilterWithEqualsToJson(_$_FilterWithEquals instance) =>
    <String, dynamic>{
      'column': instance.column,
      'value': instance.value,
      'invert': instance.invert,
      'runtimeType': instance.$type,
    };

_$_FilterWithGreaterThan _$$_FilterWithGreaterThanFromJson(
        Map<String, dynamic> json) =>
    _$_FilterWithGreaterThan(
      column: json['column'] as String,
      value: json['value'] as String,
      orEquals: json['orEquals'] as bool? ?? false,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$_FilterWithGreaterThanToJson(
        _$_FilterWithGreaterThan instance) =>
    <String, dynamic>{
      'column': instance.column,
      'value': instance.value,
      'orEquals': instance.orEquals,
      'runtimeType': instance.$type,
    };

_$_FilterWithIsNull _$$_FilterWithIsNullFromJson(Map<String, dynamic> json) =>
    _$_FilterWithIsNull(
      column: json['column'] as String,
      invert: json['invert'] as bool? ?? false,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$_FilterWithIsNullToJson(_$_FilterWithIsNull instance) =>
    <String, dynamic>{
      'column': instance.column,
      'invert': instance.invert,
      'runtimeType': instance.$type,
    };

_$_FilterWithBetweenInt _$$_FilterWithBetweenIntFromJson(
        Map<String, dynamic> json) =>
    _$_FilterWithBetweenInt(
      column: json['column'] as String,
      from: json['from'] as int,
      to: json['to'] as int,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$_FilterWithBetweenIntToJson(
        _$_FilterWithBetweenInt instance) =>
    <String, dynamic>{
      'column': instance.column,
      'from': instance.from,
      'to': instance.to,
      'runtimeType': instance.$type,
    };

_$_FilterWithIsIn _$$_FilterWithIsInFromJson(Map<String, dynamic> json) =>
    _$_FilterWithIsIn(
      column: json['column'] as String,
      invert: json['invert'] as bool? ?? false,
      values: json['values'] == null
          ? const IListConst([])
          : IList<String>.fromJson(json['values'], (value) => value as String),
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$_FilterWithIsInToJson(_$_FilterWithIsIn instance) =>
    <String, dynamic>{
      'column': instance.column,
      'invert': instance.invert,
      'values': instance.values.toJson(
        (value) => value,
      ),
      'runtimeType': instance.$type,
    };

_$_ListQuery _$$_ListQueryFromJson(Map<String, dynamic> json) => _$_ListQuery(
      page: json['page'] == null
          ? const Pagination(limit: -1, offset: 0)
          : Pagination.fromJson(json['page'] as Map<String, dynamic>),
      sort: json['sort'] == null
          ? null
          : SortBy.fromJson(json['sort'] as Map<String, dynamic>),
      filters: json['filters'] == null
          ? const IListConst([])
          : IList<FilterWith>.fromJson(json['filters'],
              (value) => FilterWith.fromJson(value as Map<String, dynamic>)),
    );

Map<String, dynamic> _$$_ListQueryToJson(_$_ListQuery instance) =>
    <String, dynamic>{
      'page': instance.page,
      'sort': instance.sort,
      'filters': instance.filters.toJson(
        (value) => value,
      ),
    };
