import 'dart:async';

import 'package:drift/drift.dart';
import 'package:logger/logger.dart';

import '../util/logger.dart';

/// https://drift.simonbinder.eu/examples/tracing/
class LogInterceptor extends QueryInterceptor {
  Future<T> _run<T>(
    String description,
    FutureOr<T> Function() operation,
  ) async {
    final trace = logger.level >= Level.trace;
    final stopwatch = trace ? (Stopwatch()..start()) : null;

    logger.t('Running $description');

    try {
      final result = await operation();
      if (trace) {
        logger.t(' => succeeded after ${stopwatch!.elapsedMilliseconds}ms');
      }
      return result;
    } on Object catch (e, st) {
      if (trace) {
        logger.t(' => failed after ${stopwatch!.elapsedMilliseconds}ms');
      }
      logger.e('Query failed', error: e, stackTrace: st);
      rethrow;
    }
  }

  @override
  TransactionExecutor beginTransaction(QueryExecutor parent) {
    logger.t('begin');
    return super.beginTransaction(parent);
  }

  @override
  Future<void> commitTransaction(TransactionExecutor inner) {
    return _run('commit', () => inner.send());
  }

  @override
  Future<void> rollbackTransaction(TransactionExecutor inner) {
    return _run('rollback', () => inner.rollback());
  }

  @override
  Future<void> runBatched(
    QueryExecutor executor,
    BatchedStatements statements,
  ) {
    return _run(
      'batch with $statements',
      () => executor.runBatched(statements),
    );
  }

  @override
  Future<int> runInsert(
    QueryExecutor executor,
    String statement,
    List<Object?> args,
  ) {
    return _run(
      '$statement with $args',
      () => executor.runInsert(statement, args),
    );
  }

  @override
  Future<int> runUpdate(
    QueryExecutor executor,
    String statement,
    List<Object?> args,
  ) {
    return _run(
      '$statement with $args',
      () => executor.runUpdate(statement, args),
    );
  }

  @override
  Future<int> runDelete(
    QueryExecutor executor,
    String statement,
    List<Object?> args,
  ) {
    return _run(
      '$statement with $args',
      () => executor.runDelete(statement, args),
    );
  }

  @override
  Future<void> runCustom(
    QueryExecutor executor,
    String statement,
    List<Object?> args,
  ) {
    return _run(
      '$statement with $args',
      () => executor.runCustom(statement, args),
    );
  }

  @override
  Future<List<Map<String, Object?>>> runSelect(
    QueryExecutor executor,
    String statement,
    List<Object?> args,
  ) {
    return _run(
      '$statement with $args',
      () => executor.runSelect(statement, args),
    );
  }
}
