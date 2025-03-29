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
        result.data?.let {
            viewModelScope.launch {
                try {
                    _state.update { it.copy(isSignInSuccessful = true) }
                } catch (e: Exception) {
                    _state.update { it.copy(signInError = "Erro: ${e.message}") }
                }
            }
        }
    }

    fun resetState() {
        _state.update { SignInState() }
    }

    fun loginUser(userData: UserData) {
        viewModelScope.launch {
            if (userData.userId.isNullOrBlank()) {
                _state.update { it.copy(signInError = "Invalid user data") }
                return@launch
            }

            try {
                val user = User(
                    uid = userData.userId ?: "",
                    name = userData.username ?: "",
                    email = userData.email ?: "",
                    photoUrl = userData.profilePictureURL ?: "",
                    isAdmin = false
                )
                firestoreUserRepository.registerUser(user)
                _state.update { it.copy(isSignInSuccessful = true) }
            } catch (e: Exception) {
                _state.update { it.copy(signInError = "Failed to save user data: ${e.message}") }
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