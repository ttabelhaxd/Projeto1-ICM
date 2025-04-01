package com.example.snapquest.navigation

sealed class Screens(val route: String) {
    object SignIn : Screens("signIn")
    object Home : Screens("home")
    object Quests : Screens("quests")
    object DailyQuest : Screens("dailyQuest")
    object Notifications : Screens("notifications")
    object Scan : Screens("scan")
    object Settings : Screens("settings")
    object EditProfile : Screens("editProfile")
    object CreateQuest : Screens("createQuest")
    object QuestDetails : Screens("questDetails/{questId}") {
        fun createRoute(questId: String) = "questDetails/$questId"
    }
    object ChallengeDetails : Screens("challengeDetails/{questId}/{challengeId}") {
        fun createRoute(questId: String, challengeId: String) = "challengeDetails/$questId/$challengeId"
    }
    object Participants : Screens("participants/{questId}") {
        fun createRoute(questId: String) = "participants/$questId"
    }
}