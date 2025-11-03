import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class RootShellScreen extends StatelessWidget {
  const RootShellScreen({
    super.key,
    required this.child,
  });

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: child,
      bottomNavigationBar: BottomAppBar(
        child: Center(
          child: TextButton(
            onPressed: () {
              context.push('/now-playing');
            },
            child: Text('Now playing...'),
          ),
        ),
      ),
    );
  }
}
