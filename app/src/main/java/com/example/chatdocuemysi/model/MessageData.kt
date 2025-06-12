package com.example.chatdocuemysi.model

/**
 * Data class que representa un mensaje individual en un chat.
 * Puede ser un mensaje de texto o de imagen.
 *
 * @property id El identificador único del mensaje, generado por Firebase.
 * @property senderId El UID del usuario que envió el mensaje.
 * @property text El contenido del mensaje de texto (vacío si es un mensaje de imagen).
 * @property imageUrl La URL de descarga de la imagen desde Firebase Storage (solo para mensajes de imagen).
 * @property storagePath La ruta dentro de Firebase Storage donde se guarda la imagen (solo para mensajes de imagen).
 * @property expiresAt La marca de tiempo (en milisegundos) en la que expira la imagen (para mensajes de imagen, 48 horas después del envío).
 * @property type El tipo de mensaje ("text" o "image").
 * @property timestamp La marca de tiempo (en milisegundos) cuando el mensaje fue enviado.
 */
data class MessageData(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val storagePath: String? = null,
    val expiresAt: Long? = null,
    val type: String = "text",
    val timestamp: Long = 0L
)
