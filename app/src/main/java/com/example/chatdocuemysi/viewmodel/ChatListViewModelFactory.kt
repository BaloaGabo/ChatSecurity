// src/main/java/com/example/chatdocuemysi/viewmodel/ChatListViewModelFactory.kt
package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatdocuemysi.repository.ChatListRepository

@Suppress("UNCHECKED_CAST")
class ChatListViewModelFactory(
    private val repository: ChatListRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            ChatListViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
