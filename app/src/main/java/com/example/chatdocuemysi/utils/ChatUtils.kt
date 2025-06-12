package com.example.chatdocuemysi.utils

/**
 * Objeto utilitario para funciones relacionadas con los chats.
 */
object ChatUtils {
    /**
     * Genera un ID de chat único y consistente para una conversación entre dos usuarios.
     * El ID se forma concatenando los UIDs de los dos usuarios, ordenados lexicográficamente
     * para asegurar que siempre sea el mismo independientemente del orden de los argumentos.
     *
     * @param user1 El UID del primer usuario.
     * @param user2 El UID del segundo usuario.
     * @return Un String que representa el ID del chat (e.g., "UID_MENOR_UID_MAYOR").
     */
    fun generateChatId(user1: String, user2: String): String {
        // Ordena los UIDs lexicográficamente para generar un ID consistente.
        return if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
    }
}