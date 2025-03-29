package com.example.snapquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.example.snapquest.repositories.FirestoreQuestRepository
import com.example.snapquest.repositories.FirestoreUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestViewModel(
    private val firestoreUserRepository: FirestoreUserRepository,
    private val questRepository: FirestoreQuestRepository
) : ViewModel() {
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests

    val currentUser = firestoreUserRepository.currentUser

    init {
        fetchQuests()
    }

    fun fetchQuests() {
        viewModelScope.launch {
            questRepository.getActiveQuests().collect { result ->
                if (result.isSuccess) {
                    _quests.value = result.getOrNull() ?: emptyList()
                } else {
                    _quests.value = emptyList()
                }
            }
        }
    }

    fun createQuest(quest: Quest, challenges: List<Challenge>) {
        viewModelScope.launch {
            questRepository.createQuest(quest, challenges)
        }
    }

    fun joinQuest(userId: String?, questId: String) {
        if (userId == null) {
            return
        }
        viewModelScope.launch {
            questRepository.joinQuest(userId, questId)
        }
    }
}

class QuestViewModelFactory(
    private val firestoreUserRepository: FirestoreUserRepository,
    private val questRepository: FirestoreQuestRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestViewModel::class.java)) {
            return QuestViewModel(firestoreUserRepository, questRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}