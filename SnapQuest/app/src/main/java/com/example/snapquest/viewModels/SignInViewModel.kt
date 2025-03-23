package com.example.snapquest.viewModels

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.snapquest.SnapQuestApp
import com.example.snapquest.models.User
import com.example.snapquest.signin.SignInResult
import com.example.snapquest.signin.SignInState
import com.example.snapquest.signin.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInViewModel : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()
    private val userRepository = SnapQuestApp.getUserRepository()

    // Update the state with the result of the sign in operation
    suspend fun onSignInResult(result: SignInResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }

    // Reset the state to the initial state
    fun resetState() {
        _state.update { SignInState() }
    }

    // Register the user in the repository
    @OptIn(UnstableApi::class)
    suspend fun loginUser(userData: UserData) {

        if(userData.userId?.isBlank() == true) {
            Log.e("SignInViewModel", "User id cannot be blank")
            return
        }
        userRepository.registerUser(
            User().apply {
                uid = userData.userId ?: ""
                name = userData.username ?: ""
                photoUrl = userData.profilePictureURL ?: ""
            }
        )
    }
}