// src/main/java/com/example/chatdocuemysi/viewmodel/CreateGroupViewModel.kt
package com.example.chatdocuemysi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.repository.ChatGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateGroupViewModel(private val myUid: String) : ViewModel() {
    private val repo = ChatGroupRepository()

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName

    private val _selectedMembers = MutableStateFlow<List<String>>(emptyList())
    val selectedMembers: StateFlow<List<String>> = _selectedMembers

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    private val _creating = MutableStateFlow(false)
    val creating: StateFlow<Boolean> = _creating

    fun onNameChange(name: String) { _groupName.value = name }
    fun onMembersChange(list: List<String>) { _selectedMembers.value = list }
    fun onPhotoChange(uri: Uri) { _photoUri.value = uri }

    fun create(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _creating.value = true
            try {
                // pasamos myUid como creatorUid explícito
                val id = repo.createGroup(
                    _groupName.value,
                    _photoUri.value,
                    _selectedMembers.value,
                    myUid
                )
                onResult(true, id)
            } catch(e: Exception) {
                onResult(false, e.localizedMessage)
            } finally {
                _creating.value = false
            }
        }
    }
}
