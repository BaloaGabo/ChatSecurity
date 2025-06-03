// src/main/java/com/example/chatdocuemysi/model/ChatItemModel.kt
package com.example.chatdocuemysi.model

import java.text.DateFormat
import java.util.*

data class ChatItemModel(
    val chatId: String,         // id del chat (grupo o "uidA_uidB")
    val title: String,          // nombre del grupo o del peer
    val photoUrl: String,       // foto del grupo o del peer
    val lastMessage: String,    // texto del último mensaje
    val lastSenderName: String, // nombre de quien envió ese mensaje
    val timestamp: Long,        // timestamp del último mensaje
    val isGroup: Boolean        // true = chat grupal, false = privado
) {
    val formattedTime: String
        get() = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestamp))
}






