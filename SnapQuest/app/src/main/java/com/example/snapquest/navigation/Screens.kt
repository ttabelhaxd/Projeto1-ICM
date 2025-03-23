package com.example.snapquest.navigation

sealed class Screens(val route: String) {
    object SignIn : Screens("signIn")
    object Home : Screens("home")
    object Quests : Screens("quests")
    object DailyQuest : Screens("dailyQuest")
    object Notifications : Screens("notifications")
    object Scan : Screens("scan")
    object Settings : Screens("settings")
}