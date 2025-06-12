package com.example.chatdocuemysi.model

/**
 * Data class que representa los detalles de un grupo de chat.
 *
 * @property groupId El identificador único del grupo.
 * @property groupName El nombre del grupo.
 * @property photoUrl La URL de la foto de perfil del grupo.
 * @property members Una lista de los IDs de usuario de todos los miembros del grupo.
 * @property admins Una lista de los IDs de usuario de los administradores del grupo.
 */
data class GroupDetail(
    val groupId: String,
    val groupName: String,
    val photoUrl: String,
    val members: List<String>,
    val admins: List<String>
)