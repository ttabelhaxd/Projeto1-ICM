package com.example.snapquest.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapquest.SnapQuestApp
import com.example.snapquest.models.Quest
import com.example.snapquest.models.Challenge
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel : ViewModel() {
    private val questDao = SnapQuestApp.getDatabase().questDao()

    init {
        createSampleQuest()
    }

    private fun createSampleQuest() {
        viewModelScope.launch {
            val defaultQuest = Quest(
                name = "Default Quest",
                description = "This is a default quest",
                photoUrl = "",
                startDate = Date(),
                endDate = Date(),
                isActive = true,
                challenges = listOf(
                    Challenge(
                        name = "Default Challenge",
                        description = "This is a default challenge",
                        hint = "Default hint",
                        hintPhotoUrl = "",
                        latitude = 0.0,
                        longitude = 0.0,
                        isCompleted = false
                    )
                ),
                latitude = 0.0,
                longitude = 0.0,
                participants = listOf(),
                creator = SnapQuestApp.userRepository.currentUser
            )

            questDao.insertQuest(defaultQuest)
            Log.d("HomeViewModel", "Default quest added: ${defaultQuest.id}")
        }
    }
}