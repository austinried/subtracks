// import 'dart:convert';

import 'dart:io';

import 'package:collection/collection.dart';
import 'package:flutter/foundation.dart';
import 'package:logging/logging.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

class AnsiColor {
  /// ANSI Control Sequence Introducer, signals the terminal for new settings.
  static const ansiEsc = '\x1B[';

  /// Reset all colors and options for current SGRs to terminal defaults.
  static const ansiDefault = '${ansiEsc}0m';

  final int? fg;
  final int? bg;
  final bool color;

  AnsiColor.none()
      : fg = null,
        bg = null,
        color = false;

  AnsiColor.fg(this.fg)
      : bg = null,
        color = true;

  AnsiColor.bg(this.bg)
      : fg = null,
        color = true;

  @override
  String toString() {
    if (fg != null) {
      return '${ansiEsc}38;5;${fg}m';
    } else if (bg != null) {
      return '${ansiEsc}48;5;${bg}m';
    } else {
      return '';
    }
  }

  String call(String msg) {
    if (color) {
      // ignore: unnecessary_brace_in_string_interps
      return '${this}$msg$ansiDefault';
    } else {
      return msg;
    }
  }

  AnsiColor toFg() => AnsiColor.fg(bg);

  AnsiColor toBg() => AnsiColor.bg(fg);

  /// Defaults the terminal's foreground color without altering the background.
  String get resetForeground => color ? '${ansiEsc}39m' : '';

  /// Defaults the terminal's background color without altering the foreground.
  String get resetBackground => color ? '${ansiEsc}49m' : '';

  static int grey(double level) => 232 + (level.clamp(0.0, 1.0) * 23).round();
}

final levelColors = {
  Level.FINEST: AnsiColor.fg(AnsiColor.grey(0.5)),
  Level.FINER: AnsiColor.fg(AnsiColor.grey(0.5)),
  Level.FINE: AnsiColor.fg(AnsiColor.grey(0.5)),
  Level.CONFIG: AnsiColor.fg(81),
  Level.INFO: AnsiColor.fg(12),
  Level.WARNING: AnsiColor.fg(208),
  Level.SEVERE: AnsiColor.fg(196),
  Level.SHOUT: AnsiColor.fg(199),
};

class LogData {
  final String? message;
  final Object? data;

  const LogData(this.message, this.data);
}

String _format(
  LogRecord event, {
  bool color = false,
  bool time = true,
  bool level = true,
}) {
  var message = '';
  if (time) message += '${event.time.toIso8601String()} ';
  if (level) message += '${event.level.name} ';

  final object = event.object;
  if (object is LogData) {
    message += '${object.message}';
    message += '\n${object.data}';
  } else if (object != null) {
    message += 'Object';
    message += '\n$object';
  } else {
    message += event.message;
  }

  if (event.error != null) {
    message += '\n${event.error}';
  }
  if (event.stackTrace != null) {
    message += '\n${event.stackTrace}';
  }

  return color
      ? message.split('\n').map((e) => levelColors[event.level]!(e)).join('\n')
      : message;
}

Future<void> _printFile(String event, String dir) async {
  final now = DateTime.now();
  final file = File(p.join(dir, '${now.year}-${now.month}-${now.day}.txt'));

  if (!event.endsWith('\n')) {
    event += '\n';
  }

  await file.writeAsString(event, mode: FileMode.writeOnlyAppend, flush: true);
}

Future<Directory> logDirectory() async {
  return Directory(
    p.join((await getApplicationDocumentsDirectory()).path, 'logs'),
  );
}

Future<List<File>> logFiles() async {
  final dir = await logDirectory();
  return dir.listSync().whereType<File>().toList()
    ..sort(
      (a, b) => b.statSync().modified.compareTo(a.statSync().modified),
    );
}

final log = Logger('default');

Future<void> initLogging() async {
  final dir = (await logDirectory())..create();

  final files = await logFiles();
  if (files.length > 7) {
    for (var file in files.slice(7)) {
      await file.delete();
    }
  }

  Logger.root.level = kDebugMode ? Level.ALL : Level.INFO;
  Logger.root.onRecord.asyncMap((event) async {
    if (kDebugMode) {
      print(_format(event, color: true, time: false, level: false));
    } else {
      await _printFile(
        _format(event, color: false, time: true, level: true),
        dir.path,
      );
    }
  }).listen((_) {}, cancelOnError: false);

  log.info('start');
}
