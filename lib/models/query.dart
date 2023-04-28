import 'package:fast_immutable_collections/fast_immutable_collections.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'query.freezed.dart';
part 'query.g.dart';

enum SortDirection {
  asc('ASC'),
  desc('DESC');

  const SortDirection(this.value);
  final String value;
}

@freezed
class Pagination with _$Pagination {
  const factory Pagination({
    required int limit,
    @Default(0) int offset,
  }) = _Pagination;

  factory Pagination.fromJson(Map<String, dynamic> json) =>
      _$PaginationFromJson(json);
}

@freezed
class SortBy with _$SortBy {
  const factory SortBy({
    required String column,
    @Default(SortDirection.asc) SortDirection dir,
  }) = _SortBy;

  factory SortBy.fromJson(Map<String, dynamic> json) => _$SortByFromJson(json);
}

@freezed
class FilterWith with _$FilterWith {
  const factory FilterWith.equals({
    required String column,
    required String value,
    @Default(false) bool invert,
  }) = _FilterWithEquals;

  const factory FilterWith.greaterThan({
    required String column,
    required String value,
    @Default(false) bool orEquals,
  }) = _FilterWithGreaterThan;

  const factory FilterWith.isNull({
    required String column,
    @Default(false) bool invert,
  }) = _FilterWithIsNull;

  const factory FilterWith.betweenInt({
    required String column,
    required int from,
    required int to,
  }) = _FilterWithBetweenInt;

  const factory FilterWith.isIn({
    required String column,
    @Default(false) bool invert,
    @Default(IListConst([])) IList<String> values,
  }) = _FilterWithIsIn;

  factory FilterWith.fromJson(Map<String, dynamic> json) =>
      _$FilterWithFromJson(json);
}

@freezed
class ListQuery with _$ListQuery {
  const factory ListQuery({
    @Default(Pagination(limit: -1, offset: 0)) Pagination page,
    SortBy? sort,
    @Default(IListConst([])) IList<FilterWith> filters,
  }) = _ListQuery;

  factory ListQuery.fromJson(Map<String, dynamic> json) =>
      _$ListQueryFromJson(json);
}

@freezed
class ListQueryOptions with _$ListQueryOptions {
  const factory ListQueryOptions({
    required IList<String> sortColumns,
    required IList<String> filterColumns,
  }) = _ListQueryOptions;
}

@freezed
class LibraryListQuery with _$LibraryListQuery {
  const factory LibraryListQuery({
    required ListQueryOptions options,
    required ListQuery query,
  }) = _LibraryListQuery;
}
