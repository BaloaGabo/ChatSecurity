package com.example.chatdocuemysi.utils

object ChatUtils {
    /** Genera siempre la misma clave “A_B” o “B_A” ordenada lexicográficamente */
    fun generateChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
    }
}