package com.kfe419.pruebaale.ui.theme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kfe419.pruebaale.ui.theme.auth.InitialScreen
import com.kfe419.pruebaale.ui.theme.auth.LoginScreen
import com.kfe419.pruebaale.ui.theme.auth.SignUpScreen
import com.kfe419.pruebaale.ui.theme.screens.NotasScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = Firebase.auth

    val startDestination = if (auth.currentUser != null) {
        AppScreens.NotasScreen.route
    } else {
        AppScreens.InitialScreen.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppScreens.InitialScreen.route) {
            InitialScreen(navController, auth)
        }
        composable(AppScreens.LoginScreen.route) {
            LoginScreen(navController, auth)
        }
        composable(AppScreens.SignUpScreen.route) {
            SignUpScreen(navController, auth)
        }
        composable(AppScreens.NotasScreen.route) {
            NotasScreen(
                navController = navController,
                auth = auth
            )
        }
    }
}
