// src/main/java/com/example/chatdocuemysi/model/MessageData.kt
package com.example.chatdocuemysi.model

data class MessageData(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",            // para mensajes de texto
    val imageUrl: String? = null,     // URL en Storage para mensajes de imagen
    val storagePath: String? = null,  // ruta interna en Storage (chatImages/<chatId>/<archivo>.jpg)
    val expiresAt: Long? = null,      // timestamp en ms de expiración (ahora + 48 h)
    val type: String = "text",        // "text" o "image"
    val timestamp: Long = 0L          // cuándo fue enviado
)
