import 'package:flutter/material.dart';

class CircleClip extends StatelessWidget {
  const CircleClip({
    super.key,
    required this.child,
  });

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return ClipOval(
      clipBehavior: Clip.antiAlias,
      child: AspectRatio(
        aspectRatio: 1.0,
        child: child,
      ),
    );
  }
}

class RoundedBoxClip extends StatelessWidget {
  const RoundedBoxClip({
    super.key,
    required this.child,
  });

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      clipBehavior: Clip.antiAlias,
      borderRadius: BorderRadiusGeometry.circular(3),
      child: child,
    );
  }
}
