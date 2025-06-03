package com.example.chatdocuemysi.model

data class MessageUiModel(
    val id: String,
    val text: String,
    val imageUrl: String?,
    val type: String,
    val timestamp: Long,
    val senderId: String,
    val senderName: String,
    val senderImageUrl: String
)

