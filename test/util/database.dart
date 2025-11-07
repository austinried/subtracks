import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:subtracks/database/database.dart';

SubtracksDatabase testDatabase() => SubtracksDatabase(
  DatabaseConnection(
    NativeDatabase.memory(),
    closeStreamsSynchronously: true,
  ),
);
