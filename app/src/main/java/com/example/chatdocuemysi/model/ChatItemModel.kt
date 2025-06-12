package com.example.chatdocuemysi.model

import java.text.DateFormat
import java.util.*
/**
 * Data class que representa un elemento de la lista de chats.
 * Este modelo se utiliza para mostrar un resumen de cada chat en la interfaz principal.
 *
 * @property chatId El identificador único del chat (puede ser ID de grupo o combinación de UIDs).
 * @property title El título del chat (nombre del grupo o nombre del contacto en chat privado).
 * @property photoUrl La URL de la foto de perfil del chat (foto del grupo o del contacto).
 * @property lastMessage El texto del último mensaje enviado en este chat.
 * @property lastSenderName El nombre del usuario que envió el último mensaje.
 * @property timestamp La marca de tiempo (en milisegundos) del último mensaje.
 * @property isGroup Un booleano que indica si el chat es grupal (true) o privado (false).
 */
data class ChatItemModel(
    val chatId: String,
    val title: String,
    val photoUrl: String,
    val lastMessage: String,
    val lastSenderName: String,
    val timestamp: Long,
    val isGroup: Boolean
) {
    /**
     * Propiedad computada que devuelve la marca de tiempo del último mensaje formateada como hora corta.
     * Por ejemplo, "10:30 AM" o "14:45".
     */
    val formattedTime: String
        get() = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestamp)) //Formato de la hora
}






