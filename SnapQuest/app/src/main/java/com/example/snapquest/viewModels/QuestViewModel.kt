package com.example.snapquest.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.example.snapquest.models.User
import com.example.snapquest.models.UserQuest
import com.example.snapquest.repositories.FirestoreQuestRepository
import com.example.snapquest.repositories.FirestoreStorageRepository
import com.example.snapquest.repositories.FirestoreUserRepository
import com.google.firestore.v1.FirestoreGrpc.FirestoreStub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

sealed class QuestUiState {
    object Idle : QuestUiState()
    object Loading : QuestUiState()
    object Success : QuestUiState()
    object QuestCreated : QuestUiState()
    object QuestDeleted : QuestUiState()
    object ChallengeCompleted : QuestUiState()
    data class QuestJoined(val questId: String) : QuestUiState()
    data class Error(val message: String) : QuestUiState()
}

class QuestViewModel(
    private val firestoreUserRepository: FirestoreUserRepository,
    private val questRepository: FirestoreQuestRepository,
    private val storageRepository: FirestoreStorageRepository
) : ViewModel() {
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests

    private val _uiState = MutableStateFlow<QuestUiState>(QuestUiState.Idle)
    val uiState: StateFlow<QuestUiState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userQuests = MutableStateFlow<List<UserQuest>>(emptyList())
    val userQuests: StateFlow<List<UserQuest>> = _userQuests

    init {
        viewModelScope.launch {
            firestoreUserRepository.currentUser.collect { user ->
                _currentUser.value = user
            }
        }
        fetchQuests()
        fetchUserQuests()
    }

    fun fetchQuests() {
        viewModelScope.launch {
            _uiState.value = QuestUiState.Loading
            questRepository.getActiveQuests().collect { result ->
                _uiState.value = when {
                    result.isSuccess -> {
                        _quests.value = result.getOrNull() ?: emptyList()
                        QuestUiState.Success
                    }
                    else -> QuestUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            }
        }
    }

    fun createQuest(quest: Quest, challenges: List<Challenge>) {
        viewModelScope.launch {
            _uiState.value = QuestUiState.Loading
            try {
                val userId = currentUser.value?.uid ?: throw Exception("User not logged in")
                val questWithCreator = quest.copy(creatorId = userId)
                questRepository.createQuest(questWithCreator, challenges)
                _uiState.value = QuestUiState.QuestCreated
                fetchQuests()
            } catch (e: Exception) {
                _uiState.value = QuestUiState.Error(e.message ?: "Failed to create quest")
            }
        }
    }

    fun deleteQuest(questId: String) {
        viewModelScope.launch {
            _uiState.value = QuestUiState.Loading
            try {
                questRepository.deleteQuest(questId)
                fetchQuests()
                _uiState.value = QuestUiState.QuestDeleted
            } catch (e: Exception) {
                _uiState.value = QuestUiState.Error(e.message ?: "Failed to delete quest")
            }
        }
    }

    fun fetchUserQuests() {
        viewModelScope.launch {
            currentUser.value?.uid?.let { userId ->
                questRepository.getUserQuests(userId).collect { result ->
                    if (result.isSuccess) {
                        _userQuests.value = result.getOrNull() ?: emptyList()
                    }
                }
            }
        }
    }

    fun joinQuest(userId: String, questId: String) {
        viewModelScope.launch {
            _uiState.value = QuestUiState.Loading
            try {
                questRepository.joinQuest(userId, questId)
                fetchUserQuests()
                _uiState.value = QuestUiState.QuestJoined(questId)
            } catch (e: Exception) {
                _uiState.value = QuestUiState.Error(e.message ?: "Failed to join quest")
            }
        }
    }

    fun getQuestById(questId: String): Flow<Quest?> = flow {
        questRepository.getQuestById(questId).collect { result ->
            emit(result.getOrNull())
        }
    }

   fun getQuestChallenges(questId: String): Flow<List<Challenge>> =
        flow {
            try {
                val challenges = questRepository.getQuestChallenges(questId)
                emit(challenges)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }

    fun getUserQuest(userId: String, questId: String): Flow<UserQuest?> = flow {
        questRepository.getUserQuest(userId, questId).collect { result ->
            emit(result.getOrNull())
        }
    }

    fun completeChallenge(userId: String, questId: String, challengeId: String) {
        viewModelScope.launch {
            _uiState.value = QuestUiState.Loading
            try {
                questRepository.completeChallenge(userId, questId, challengeId)
                firestoreUserRepository.refreshUserData(userId)
                _uiState.value = QuestUiState.ChallengeCompleted
            } catch (e: Exception) {
                _uiState.value = QuestUiState.Error(e.message ?: "Failed to complete challenge")
            }
        }
    }

    fun getChallengeById(questId: String, challengeId: String): Flow<Challenge?> = flow {
        try {
            val challenge = questRepository.getChallengeById(questId, challengeId)
            emit(challenge)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun uploadQuestPhoto(localPath: String): String {
        return storageRepository.uploadQuestPhoto(localPath)
    }

    suspend fun uploadChallengePhoto(localPath: String): String {
        return storageRepository.uploadChallengePhoto(localPath)
    }

    fun getParticipantDetails(participantIds: List<String>): Flow<List<User>> = flow {
        try {
            Log.d("QuestViewModel", "Fetching ${participantIds.size} participants")
            val users = firestoreUserRepository.getUsersByIds(participantIds)
            Log.d("QuestViewModel", "Fetched ${users.size} users")
            emit(users)
        } catch (e: Exception) {
            Log.e("QuestViewModel", "Error fetching participants", e)
            emit(emptyList())
        }
    }
}

class QuestViewModelFactory(
    private val firestoreUserRepository: FirestoreUserRepository,
    private val firestoreQuestRepository: FirestoreQuestRepository,
    private val firestoreStorageRepository: FirestoreStorageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestViewModel::class.java)) {
            return QuestViewModel(firestoreUserRepository, firestoreQuestRepository, firestoreStorageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}