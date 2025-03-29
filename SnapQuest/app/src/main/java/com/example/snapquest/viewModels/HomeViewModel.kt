package com.example.snapquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snapquest.models.User
import com.example.snapquest.repositories.FirestoreUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModel() {
    private val _userName = MutableStateFlow("")
    private val userName: StateFlow<String> = _userName

    init {
        viewModelScope.launch {
            firestoreUserRepository.currentUser.collect { user ->
                _userName.value = user?.name ?: "Visitor"
            }
        }
    }

    fun getName(): String {
        return userName.value.split(" ")[0]
    }
}

class HomeViewModelFactory(
    private val firestoreUserRepository: FirestoreUserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(firestoreUserRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}