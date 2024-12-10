// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'query.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$PaginationImpl _$$PaginationImplFromJson(Map<String, dynamic> json) =>
    _$PaginationImpl(
      limit: (json['limit'] as num).toInt(),
      offset: (json['offset'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$$PaginationImplToJson(_$PaginationImpl instance) =>
    <String, dynamic>{
      'limit': instance.limit,
      'offset': instance.offset,
    };

_$SortByImpl _$$SortByImplFromJson(Map<String, dynamic> json) => _$SortByImpl(
      column: json['column'] as String,
      dir: $enumDecodeNullable(_$SortDirectionEnumMap, json['dir']) ??
          SortDirection.asc,
    );

Map<String, dynamic> _$$SortByImplToJson(_$SortByImpl instance) =>
    <String, dynamic>{
      'column': instance.column,
      'dir': _$SortDirectionEnumMap[instance.dir]!,
    };

const _$SortDirectionEnumMap = {
  SortDirection.asc: 'asc',
  SortDirection.desc: 'desc',
};

_$FilterWithEqualsImpl _$$FilterWithEqualsImplFromJson(
        Map<String, dynamic> json) =>
    _$FilterWithEqualsImpl(
      column: json['column'] as String,
      value: json['value'] as String,
      invert: json['invert'] as bool? ?? false,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$FilterWithEqualsImplToJson(
        _$FilterWithEqualsImpl instance) =>
    <String, dynamic>{
      'column': instance.column,
      'value': instance.value,
      'invert': instance.invert,
      'runtimeType': instance.$type,
    };

_$FilterWithGreaterThanImpl _$$FilterWithGreaterThanImplFromJson(
        Map<String, dynamic> json) =>
    _$FilterWithGreaterThanImpl(
      column: json['column'] as String,
      value: json['value'] as String,
      orEquals: json['orEquals'] as bool? ?? false,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$FilterWithGreaterThanImplToJson(
        _$FilterWithGreaterThanImpl instance) =>
    <String, dynamic>{
      'column': instance.column,
      'value': instance.value,
      'orEquals': instance.orEquals,
      'runtimeType': instance.$type,
    };

_$FilterWithIsNullImpl _$$FilterWithIsNullImplFromJson(
        Map<String, dynamic> json) =>
    _$FilterWithIsNullImpl(
      column: json['column'] as String,
      invert: json['invert'] as bool? ?? false,
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$FilterWithIsNullImplToJson(
        _$FilterWithIsNullImpl instance) =>
    <String, dynamic>{
      'column': instance.column,
      'invert': instance.invert,
      'runtimeType': instance.$type,
    };

_$FilterWithBetweenIntImpl _$$FilterWithBetweenIntImplFromJson(
        Map<String, dynamic> json) =>
    _$FilterWithBetweenIntImpl(
      column: json['column'] as String,
      from: (json['from'] as num).toInt(),
      to: (json['to'] as num).toInt(),
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$FilterWithBetweenIntImplToJson(
        _$FilterWithBetweenIntImpl instance) =>
    <String, dynamic>{
      'column': instance.column,
      'from': instance.from,
      'to': instance.to,
      'runtimeType': instance.$type,
    };

_$FilterWithIsInImpl _$$FilterWithIsInImplFromJson(Map<String, dynamic> json) =>
    _$FilterWithIsInImpl(
      column: json['column'] as String,
      invert: json['invert'] as bool? ?? false,
      values: json['values'] == null
          ? const IListConst([])
          : IList<String>.fromJson(json['values'], (value) => value as String),
      $type: json['runtimeType'] as String?,
    );

Map<String, dynamic> _$$FilterWithIsInImplToJson(
        _$FilterWithIsInImpl instance) =>
    <String, dynamic>{
      'column': instance.column,
      'invert': instance.invert,
      'values': instance.values.toJson(
        (value) => value,
      ),
      'runtimeType': instance.$type,
    };

_$ListQueryImpl _$$ListQueryImplFromJson(Map<String, dynamic> json) =>
    _$ListQueryImpl(
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

Map<String, dynamic> _$$ListQueryImplToJson(_$ListQueryImpl instance) =>
    <String, dynamic>{
      'page': instance.page,
      'sort': instance.sort,
      'filters': instance.filters.toJson(
        (value) => value,
      ),
    };
