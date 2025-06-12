package com.example.chatdocuemysi.model

/**
 * Data class que representa un mensaje para ser mostrado en la interfaz de usuario.
 * Contiene información adicional del remitente para una fácil visualización.
 *
 * @property id El identificador único del mensaje, generado por Firebase.
 * @property text El contenido del mensaje de texto (vacío si es un mensaje de imagen).
 * @property imageUrl La URL de descarga de la imagen desde Firebase Storage (solo para mensajes de imagen).
 * @property type El tipo de mensaje ("text" o "image").
 * @property timestamp La marca de tiempo (en milisegundos) cuando el mensaje fue enviado.
 * @property senderId El UID del usuario que envió el mensaje.
 * @property senderName El nombre del usuario que envió el mensaje.
 * @property senderImageUrl La URL de la imagen de perfil del usuario que envió el mensaje.
 */
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

