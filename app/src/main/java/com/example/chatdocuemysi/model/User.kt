package com.example.chatdocuemysi.model

/**
 * Data class que representa un usuario dentro de la aplicación.
 * Contiene información básica del perfil del usuario.
 *
 * @property uid El identificador único del usuario, generado por Firebase.
 * @property nombres El nombre completo o nombre de visualización del usuario.
 * @property email El correo electrónico del usuario, utilizado para el login y notificaciones.
 * @property imagen El enlace (URL) a la foto de perfil del usuario.
 */
data class User(
    val uid: String = "",
    val nombres: String = "",
    val email: String = "",
    val imagen: String = ""
)
