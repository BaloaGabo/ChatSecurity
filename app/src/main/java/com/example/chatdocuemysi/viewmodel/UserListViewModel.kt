// src/main/java/com/example/chatdocuemysi/viewmodel/UserListViewModel.kt
package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.User
import com.example.chatdocuemysi.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UserListViewModel(
    private val repo: UserRepository = UserRepository()
) : ViewModel() {

    // Flow<List<User>>
    val users = repo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow<Map<uid,User>>
    val userMap: StateFlow<Map<String,User>> = users
        .map { list -> list.associateBy { it.uid } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
}
