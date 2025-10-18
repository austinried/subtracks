import 'package:go_router/go_router.dart';
import 'package:subtracks/screens/home.dart';

final router = GoRouter(
  routes: [
    GoRoute(
      path: '/',
      builder: (context, state) => HomeScreen(),
    ),
  ],
);
