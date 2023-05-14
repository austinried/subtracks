import 'package:flutter/material.dart';

void showErrorSnackbar(BuildContext context, String message) {
  final colors = Theme.of(context).colorScheme;

  ScaffoldMessenger.of(context).showSnackBar(SnackBar(
    content: Text(message, style: TextStyle(color: colors.onErrorContainer)),
    backgroundColor: colors.errorContainer,
    showCloseIcon: true,
    closeIconColor: colors.onErrorContainer,
    behavior: SnackBarBehavior.floating,
    duration: const Duration(seconds: 10),
  ));
}
