import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class NowPlayingScreen extends StatelessWidget {
  const NowPlayingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Now Playing!'),
            TextButton(
              onPressed: () {
                context.go('/album');
              },
              child: Text('Album...'),
            ),
          ],
        ),
      ),
    );
  }
}
