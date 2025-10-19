import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class AlbumScreen extends StatelessWidget {
  const AlbumScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Album!'),
            TextButton(
              onPressed: () {
                context.push('/artist');
              },
              child: Text('Artist...'),
            ),
          ],
        ),
      ),
    );
  }
}
