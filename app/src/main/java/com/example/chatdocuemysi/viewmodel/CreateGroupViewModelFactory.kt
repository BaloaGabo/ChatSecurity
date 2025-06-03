package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CreateGroupViewModelFactory(private val myUid: String): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(c: Class<T>): T {
        if (c.isAssignableFrom(CreateGroupViewModel::class.java))
            @Suppress("UNCHECKED_CAST") return CreateGroupViewModel(myUid) as T
        throw IllegalArgumentException("Unknown VM")
    }
}
