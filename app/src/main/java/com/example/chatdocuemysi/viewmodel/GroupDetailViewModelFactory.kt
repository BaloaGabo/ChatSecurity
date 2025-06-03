// src/main/java/com/example/chatdocuemysi/viewmodel/GroupDetailViewModelFactory.kt
package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatdocuemysi.repository.ChatGroupRepository

class GroupDetailViewModelFactory(
    private val groupId: String,
    private val myUid: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(c: Class<T>): T {
        if (c.isAssignableFrom(GroupDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupDetailViewModel(
                repo    = ChatGroupRepository(),
                groupId = groupId,
                myUid   = myUid
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

