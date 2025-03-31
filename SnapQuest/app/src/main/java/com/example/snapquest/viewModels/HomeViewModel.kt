package com.example.snapquest.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snapquest.models.User
import com.example.snapquest.repositories.FirestoreUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            firestoreUserRepository.currentUser.collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun getName(): String {
        return (currentUser.value?.name?.split (" ")?.get(0)) ?: "Visitor"
    }

    fun updateUserProfile(name: String, email: String, photoUrl: String) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val updatedUser = user.copy(
                        name = name,
                        email = email,
                        photoUrl = photoUrl
                    )

                    firestoreUserRepository.updateUser(updatedUser).onSuccess {
                        _currentUser.value = updatedUser
                    }.onFailure { e ->
                        Log.e("HomeViewModel", "Error updating profile", e)
                        throw e
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error in updateUserProfile", e)
                throw e
            }
        }
    }
}

class HomeViewModelFactory(
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel>create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(firestoreUserRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}