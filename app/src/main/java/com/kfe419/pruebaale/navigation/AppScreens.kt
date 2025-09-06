package com.kfe419.pruebaale.navigation

sealed class AppScreens(val route: String){
    object InitialScreen: AppScreens("initial_screen")
    object LoginScreen: AppScreens("login_screen")
    object SignUpScreen: AppScreens("signup_screen")
    object NotasScreen: AppScreens("notas_screen")
}
