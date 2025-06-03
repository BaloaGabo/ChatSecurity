// src/main/java/com/example/chatdocuemysi/viewmodel/ChatListViewModel.kt
package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.ChatItemModel
import com.example.chatdocuemysi.repository.ChatListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ChatListViewModel(repository: ChatListRepository) : ViewModel() {
    val chatItems: StateFlow<List<ChatItemModel>> =
        repository.getAllChats()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
