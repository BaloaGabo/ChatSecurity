package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.GroupDetail
import com.example.chatdocuemysi.repository.ChatGroupRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val repo: ChatGroupRepository,
    private val groupId: String,
    private val myUid: String
): ViewModel() {
    /** Flujo con datos del grupo */
    val detail: StateFlow<GroupDetail> =
        repo.getGroupDetail(groupId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
                GroupDetail(groupId,"","", emptyList(), emptyList()))

    /** Soy admin? */
    val isAdmin: StateFlow<Boolean> = detail
        .map { myUid in it.admins }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun promote(uid: String) = viewModelScope.launch {
        repo.promoteToAdmin(groupId, uid)
    }
    fun demote(uid: String) = viewModelScope.launch {
        repo.demoteAdmin(groupId, uid)
    }
    fun addMember(uid: String) = viewModelScope.launch {
        repo.addMember(groupId, uid)
    }
    fun removeMember(uid: String) = viewModelScope.launch {
        repo.removeMember(groupId, uid)
    }
}
