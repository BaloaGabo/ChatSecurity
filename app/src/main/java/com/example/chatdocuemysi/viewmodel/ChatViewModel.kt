package com.example.chatdocuemysi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.MessageUiModel
import com.example.chatdocuemysi.repository.MessagesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val messagesRepo: MessagesRepository = MessagesRepository(),
    userListVm: UserListViewModel = UserListViewModel()
) : ViewModel() {
    private val userMapFlow = userListVm.userMap

    fun getMessagesUiFlow(chatPath: String): StateFlow<List<MessageUiModel>> =
        combine(messagesRepo.getMessages(chatPath), userMapFlow) { msgs, users ->
            msgs.map { md ->
                val u = users[md.senderId]
                MessageUiModel(
                    id             = md.id,
                    text           = md.text,
                    imageUrl       = md.imageUrl,
                    type           = md.type,
                    timestamp      = md.timestamp,
                    senderId       = md.senderId,
                    senderName     = u?.nombres.orEmpty(),
                    senderImageUrl = u?.imagen.orEmpty()
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendText(chatPath: String, senderId: String, text: String) =
        messagesRepo.sendTextMessage(chatPath, senderId, text)

    fun sendImage(chatPath: String, senderId: String, uri: Uri) {
        viewModelScope.launch { messagesRepo.sendImageMessage(chatPath, senderId, uri) }
    }
}



