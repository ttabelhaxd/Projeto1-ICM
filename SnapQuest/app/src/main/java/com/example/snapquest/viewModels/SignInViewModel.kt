package com.example.snapquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snapquest.models.User
import com.example.snapquest.repositories.UserRepository
import com.example.snapquest.signin.SignInResult
import com.example.snapquest.signin.SignInState
import com.example.snapquest.signin.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }

        result.data?.let { userData ->
            viewModelScope.launch {
                userRepository.registerUser(
                    User(
                        uid = userData.userId ?: "",
                        name = userData.username ?: "",
                        photoUrl = userData.profilePictureURL ?: ""
                    )
                )
            }
        }
    }

    fun resetState() {
        _state.update { SignInState() }
    }

    fun loginUser(userData: UserData) {
        viewModelScope.launch {
            if (userData.userId?.isBlank() == true) return@launch

            userRepository.registerUser(
                User().apply {
                    uid = userData.userId ?: ""
                    name = userData.username ?: ""
                    photoUrl = userData.profilePictureURL ?: ""
                }
            )
        }
    }
}

// Factory para o ViewModel
class SignInViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignInViewModel(userRepository) as T
    }
}