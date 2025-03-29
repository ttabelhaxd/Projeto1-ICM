package com.example.snapquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.example.snapquest.repositories.FirestoreQuestRepository
import com.example.snapquest.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestViewModel(
    private val userRepository: UserRepository,
    private val questRepository: FirestoreQuestRepository
) : ViewModel() {
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests
    private val _user = MutableStateFlow<String>("")
    val user: StateFlow<String> = _user

    init {
        fetchQuests()
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { usersList ->
                val currentUser = userRepository.currentUser
                if (currentUser != null) {
                    _user.value = currentUser.uid
                }
            }
        }
    }

    fun fetchQuests() {
        viewModelScope.launch {
            questRepository.getActiveQuests().collect { questsList ->
                _quests.value = questsList
            }
        }
    }

    fun createQuest(quest: Quest, challenges: List<Challenge>) {
        viewModelScope.launch {
            questRepository.createQuest(quest, challenges)
        }
    }

    fun joinQuest(userId: String, questId: String) {
        viewModelScope.launch {
            questRepository.joinQuest(userId, questId)
        }
    }
}

class QuestViewModelFactory(
    private val userRepository: UserRepository,
    private val questRepository: FirestoreQuestRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestViewModel::class.java)) {
            return QuestViewModel(userRepository, questRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}