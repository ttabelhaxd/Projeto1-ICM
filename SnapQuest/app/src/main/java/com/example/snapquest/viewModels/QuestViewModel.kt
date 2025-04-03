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
import com.example.snapquest.services.NotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class QuestUiState {
    object Idle : QuestUiState()
    object Loading : QuestUiState()
    object Success : QuestUiState()
    object QuestCreated : QuestUiState()
    object QuestDeleted : QuestUiState()
    object QuestCompleted : QuestUiState()
    object ChallengeCompleted : QuestUiState()
    data class QuestJoined(val questId: String) : QuestUiState()
    data class Error(val message: String) : QuestUiState()
}

class QuestViewModel(
    private val firestoreUserRepository: FirestoreUserRepository,
    private val questRepository: FirestoreQuestRepository,
    private val storageRepository: FirestoreStorageRepository,
    private val notificationService: NotificationService
) : ViewModel() {
    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests

    private val _uiState = MutableStateFlow<QuestUiState>(QuestUiState.Idle)
    val uiState: StateFlow<QuestUiState> = _uiState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userQuests = MutableStateFlow<List<UserQuest>>(emptyList())
    val userQuests: StateFlow<List<UserQuest>> = _userQuests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            firestoreUserRepository.currentUser.collect { user ->
                _currentUser.value = user
                if (user != null) {
                    fetchUserQuests()
                }
            }
        }
        fetchQuests()
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

                val quest = _quests.value.find { it.id == questId }
                quest?.let {
                    notificationService.showQuestJoinedNotification(it.name)
                }
                firestoreUserRepository.addNotification(
                    userId = userId,
                    title = "Quest Joined!",
                    message = "You've successfully joined ${quest?.name}",
                    type = "quest_joined",
                    relatedId = questId
                )

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
            val users = firestoreUserRepository.getUsersByIds(participantIds)
            emit(users)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun completeChallengeWithPhoto(
        userId: String,
        questId: String,
        challengeId: String,
        photoPath: String
    ) {
        viewModelScope.launch {
            _uiState.value = QuestUiState.Loading
            try {
                val photoUrl = withContext(Dispatchers.IO) {
                    storageRepository.uploadChallengeUserPhoto(photoPath)
                }

                questRepository.completeChallengeWithPhoto(
                    userId = userId,
                    questId = questId,
                    challengeId = challengeId,
                    photoUrl = photoUrl
                ).getOrThrow()

                fetchUserQuests()
                fetchQuests()

                val challenge = questRepository.getChallengeById(questId, challengeId)
                if (challenge != null) {
                    notificationService.showChallengeCompletedNotification(challenge.name)

                    try {
                        firestoreUserRepository.addNotification(
                            userId = userId,
                            title = "Challenge Completed!",
                            message = "You completed: ${challenge.name}",
                            type = "challenge_completed",
                            relatedId = questId + "_" + challengeId
                        )
                        Log.d("Notifications", "Notification added for challenge")
                    } catch (e: Exception) {
                        Log.e("Notifications", "Failed to add notification", e)
                    }
                }

                checkQuestCompletion(userId, questId)

                _uiState.value = QuestUiState.ChallengeCompleted
            } catch (e: Exception) {
                Log.e("QuestViewModel", "Error completing challenge", e)
                _uiState.value = QuestUiState.Error(e.message ?: "Failed to complete challenge")
            }
        }
    }

    private suspend fun checkQuestCompletion(userId: String, questId: String) {
        try {
            fetchUserQuests()
            val userQuest = userQuests.value.firstOrNull { it.questId == questId }
            val quest = quests.value.firstOrNull { it.id == questId }

            if (userQuest != null && quest != null &&
                userQuest.completedChallenges.size >= quest.totalChallenges) {
                notificationService.showQuestCompletedNotification(quest.name)

                firestoreUserRepository.addNotification(
                    userId = userId,
                    title = "Quest Completed! 🏆",
                    message = "Congratulations! You completed ${quest.name}",
                    type = "quest_completed",
                    relatedId = questId
                )

                _uiState.value = QuestUiState.QuestCompleted
                Log.d("Notifications", "Quest completion notification sent")
            }
        } catch (e: Exception) {
            Log.e("Notifications", "Error checking quest completion", e)
        }
    }
}

class QuestViewModelFactory(
    private val firestoreUserRepository: FirestoreUserRepository,
    private val firestoreQuestRepository: FirestoreQuestRepository,
    private val firestoreStorageRepository: FirestoreStorageRepository,
    private val notificationService: NotificationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestViewModel::class.java)) {
            return QuestViewModel(firestoreUserRepository, firestoreQuestRepository, firestoreStorageRepository, notificationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}