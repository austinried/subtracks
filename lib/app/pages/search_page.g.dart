// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'search_page.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$searchResultHash() => r'e5240c0c51937e1e946138d27aeaea93dc0231c3';

/// See also [searchResult].
@ProviderFor(searchResult)
final searchResultProvider = AutoDisposeFutureProvider<SearchResults>.internal(
  searchResult,
  name: r'searchResultProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$searchResultHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef SearchResultRef = AutoDisposeFutureProviderRef<SearchResults>;
String _$searchQueryHash() => r'f7624215b3d5a8b917cb0af239666a19a18d91d5';

/// See also [SearchQuery].
@ProviderFor(SearchQuery)
final searchQueryProvider =
    AutoDisposeNotifierProvider<SearchQuery, String?>.internal(
  SearchQuery.new,
  name: r'searchQueryProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$searchQueryHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$SearchQuery = AutoDisposeNotifier<String?>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member
