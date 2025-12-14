import 'package:logger/logger.dart';

class LogLevelFilter extends LogFilter {
  @override
  bool shouldLog(LogEvent event) {
    return event.level >= level!;
  }
}

class SubtracksLogger extends Logger {
  SubtracksLogger({
    super.filter,
    super.printer,
    super.output,
    required Level level,
  }) : _level = level,
       super(level: level);

  final Level _level;
  Level get level => _level;
}

SubtracksLogger createLogger() {
  var isDebug = false;
  assert(() {
    isDebug = true;
    return true;
  }());

  if (isDebug) {
    return SubtracksLogger(
      filter: DevelopmentFilter(),
      printer: PrettyPrinter(),
      output: ConsoleOutput(),
      level: Level.debug,
    );
  }

  // TODO: production logger
  return SubtracksLogger(
    filter: DevelopmentFilter(),
    printer: PrettyPrinter(),
    output: ConsoleOutput(),
    level: Level.debug,
  );
}

final logger = createLogger();
