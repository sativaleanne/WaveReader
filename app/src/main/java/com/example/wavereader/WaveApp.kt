package com.example.wavereader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wavereader.ui.main.HistoryScreen
import com.example.wavereader.ui.auth.LoginScreen
import com.example.wavereader.ui.auth.RegisterScreen
import com.example.wavereader.ui.auth.StartScreen
import com.example.wavereader.ui.main.MainScreen
import com.example.wavereader.viewmodels.SensorViewModel
import com.google.firebase.auth.FirebaseAuth

/*
* App navigation and flow to handle screens outside of scaffolding
* such as authentication screens and history.
 */
sealed class AppRoute(val route: String) {
    data object Start : AppRoute("start")
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Main : AppRoute("main")
    data object History : AppRoute("history")
}

sealed class AppFlow {
    data object Authenticated : AppFlow()
    data object Unauthenticated : AppFlow()
    data object Guest : AppFlow()
}

@Composable
fun WaveApp(viewModel: SensorViewModel) {
    val auth = FirebaseAuth.getInstance()
    val navController = rememberNavController()
    val appFlow = remember { mutableStateOf<AppFlow>(AppFlow.Unauthenticated) }

    // Check for user
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        appFlow.value = if (user != null && user.isEmailVerified) {
            AppFlow.Authenticated
        } else {
            AppFlow.Unauthenticated
        }
    }

    NavHost(
        navController = navController,
        startDestination = when (appFlow.value) {
            AppFlow.Unauthenticated -> AppRoute.Start.route
            AppFlow.Authenticated, AppFlow.Guest -> AppRoute.Main.route
        },
        modifier = Modifier
    ) {
        // Authentication Flow
        composable(AppRoute.Start.route) {
            StartScreen(
                onLogin = { navController.navigate(AppRoute.Login.route) },
                onRegister = { navController.navigate(AppRoute.Register.route) },
                onGuest = {
                    appFlow.value = AppFlow.Guest
                    navController.navigate(AppRoute.Main.route) {
                        popUpTo(AppRoute.Start.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Login.route) {
            LoginScreen(
                auth = auth,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    appFlow.value = AppFlow.Authenticated
                    navController.navigate(AppRoute.Main.route) {
                        popUpTo(AppRoute.Start.route) { inclusive = true }
                    }
                },
                onRegisterNavigate = { navController.navigate(AppRoute.Register.route) }
            )
        }

        composable(AppRoute.Register.route) {
            RegisterScreen(
                auth = auth,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    appFlow.value = AppFlow.Authenticated
                    navController.navigate(AppRoute.Main.route) {
                        popUpTo(AppRoute.Start.route) { inclusive = true }
                    }
                },
                onLoginNavigate = { navController.navigate(AppRoute.Login.route) }
            )
        }

        // Main App Flow
        composable(AppRoute.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onSignOut = {
                    auth.signOut()
                    appFlow.value = AppFlow.Unauthenticated
                    navController.navigate(AppRoute.Start.route) {
                        popUpTo(AppRoute.Main.route) { inclusive = true }
                    }
                },
                onHistoryNavigate = {
                    navController.navigate(AppRoute.History.route)
                },
                isGuest = appFlow.value is AppFlow.Guest
            )
        }

        // History Page
        composable(AppRoute.History.route) {
            HistoryScreen(navController = navController)
        }
    }
}






