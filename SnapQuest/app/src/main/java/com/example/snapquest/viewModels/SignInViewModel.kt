package com.example.snapquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snapquest.models.User
import com.example.snapquest.repositories.FirestoreUserRepository
import com.example.snapquest.signin.SignInResult
import com.example.snapquest.signin.SignInState
import com.example.snapquest.signin.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult) {
        _state.update { it.copy(isLoading = true) }
        result.data?.let { userData ->
            viewModelScope.launch {
                try {
                    val userId = userData.userId ?: throw Exception("User ID is null")
                    val existingUser = firestoreUserRepository.fetchCurrentUser(userId)

                    if (existingUser == null) {
                        val newUser = User(
                            uid = userId,
                            name = userData.username ?: "",
                            email = userData.email ?: "",
                            photoUrl = userData.profilePictureURL ?: "",
                            isAdmin = true,
                            questsCompleted = 0,
                            challengesCompleted = 0
                        )
                        firestoreUserRepository.registerUser(newUser).onSuccess {
                            _state.update { it.copy(isSignInSuccessful = true, isLoading = false) }
                        }.onFailure { e ->
                            _state.update { it.copy(signInError = "Erro ao registrar: ${e.message}", isLoading = false) }
                        }
                    } else {
                        firestoreUserRepository.refreshUserData(userId)
                        _state.update {
                            it.copy(
                                isSignInSuccessful = true,
                                isLoading = false
                            )
                        }

                    }
                } catch (e: Exception) {
                    _state.update { it.copy(signInError = "Erro: ${e.message}", isLoading = false) }
                }
            }
        }
    }

    fun resetState() {
        _state.update { SignInState() }
    }

    fun loginUser(userData: UserData) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (userData.userId.isNullOrBlank()) {
                _state.update { it.copy(signInError = "Invalid user data", isLoading = false) }
                return@launch
            }

            val existingUser = firestoreUserRepository.fetchCurrentUser(userData.userId)
            try {
                val user = User(
                    uid = existingUser?.uid ?: userData.userId,
                    name = existingUser?.name ?: userData.username ?: "",
                    email = existingUser?.email ?: userData.email ?: "",
                    photoUrl = existingUser?.photoUrl ?: userData.profilePictureURL ?: "",
                    isAdmin = existingUser?.isAdmin?.equals(false) ?: true,
                    questsCompleted = existingUser?.questsCompleted ?: 0,
                    challengesCompleted = existingUser?.challengesCompleted ?: 0
                )
                firestoreUserRepository.registerUser(user)
                _state.update { it.copy(isSignInSuccessful = true, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(signInError = "Failed to save user data: ${e.message}", isLoading = false) }
            }
        }
    }
}

// Factory para o ViewModel
class SignInViewModelFactory(
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignInViewModel(firestoreUserRepository) as T
    }
}