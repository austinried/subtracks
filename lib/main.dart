import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:stack_trace/stack_trace.dart' as stack_trace;
import 'package:worker_manager/worker_manager.dart';

import 'app/app.dart';

void main() async {
  // TOOD: probably remove before live
  // https://stackoverflow.com/a/73770713
  FlutterError.demangleStackTrace = (StackTrace stack) {
    if (stack is stack_trace.Trace) return stack.vmTrace;
    if (stack is stack_trace.Chain) return stack.toTrace().vmTrace;
    return stack;
  };

  // keep some threads warm for background (palette generation) tasks
  await Executor().warmUp();

  WidgetsFlutterBinding.ensureInitialized();
  runApp(const ProviderScope(child: MyApp()));
}
