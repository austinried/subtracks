import 'dart:async';

import 'package:drift/drift.dart';
import 'package:drift/isolate.dart';

/// https://github.com/simolus3/drift/issues/2326#issuecomment-1445138730
class ErrorLoggingDatabase implements QueryExecutor {
  final QueryExecutor inner;
  final void Function(Object, StackTrace) onError;

  ErrorLoggingDatabase(this.inner, this.onError);

  Future<T> _handleErrors<T>(Future<T> Function() body) {
    return Future.sync(body)
        .onError<DriftWrappedException>((error, stackTrace) {
      onError(error, error.trace ?? stackTrace);
      throw error;
    }).onError<DriftRemoteException>((error, stackTrace) {
      onError(error, error.remoteStackTrace ?? stackTrace);
      throw error;
    });
  }

  @override
  TransactionExecutor beginTransaction() {
    return _ErrorLoggingTransactionExecutor(inner.beginTransaction(), onError);
  }

  @override
  Future<void> close() {
    return _handleErrors(inner.close);
  }

  @override
  SqlDialect get dialect => inner.dialect;

  @override
  Future<bool> ensureOpen(QueryExecutorUser user) {
    return _handleErrors(() => inner.ensureOpen(user));
  }

  @override
  Future<void> runBatched(BatchedStatements statements) {
    return _handleErrors(() => inner.runBatched(statements));
  }

  @override
  Future<void> runCustom(String statement, [List<Object?>? args]) {
    return _handleErrors(() => inner.runCustom(statement, args));
  }

  @override
  Future<int> runDelete(String statement, List<Object?> args) {
    return _handleErrors(() => inner.runDelete(statement, args));
  }

  @override
  Future<int> runInsert(String statement, List<Object?> args) {
    return _handleErrors(() => inner.runInsert(statement, args));
  }

  @override
  Future<List<Map<String, Object?>>> runSelect(
      String statement, List<Object?> args) {
    return _handleErrors(() => inner.runSelect(statement, args));
  }

  @override
  Future<int> runUpdate(String statement, List<Object?> args) {
    return _handleErrors(() => inner.runUpdate(statement, args));
  }
}

class _ErrorLoggingTransactionExecutor extends ErrorLoggingDatabase
    implements TransactionExecutor {
  final TransactionExecutor transaction;

  _ErrorLoggingTransactionExecutor(
      this.transaction, void Function(Object, StackTrace) onError)
      : super(transaction, onError);

  @override
  Future<void> rollback() {
    return _handleErrors(transaction.rollback);
  }

  @override
  Future<void> send() {
    return _handleErrors(transaction.send);
  }

  @override
  bool get supportsNestedTransactions => transaction.supportsNestedTransactions;
}
