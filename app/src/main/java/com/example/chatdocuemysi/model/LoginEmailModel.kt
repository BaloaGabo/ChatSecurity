package com.example.chatdocuemysi.model

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Modelo para la lógica de inicio de sesión con email y contraseña.
 * Gestiona la autenticación de usuarios utilizando Firebase Authentication.
 */
class LoginEmailModel {
    private val firebaseAuth = FirebaseAuth.getInstance()
    /**
     * Intenta iniciar sesión con el email y la contraseña proporcionados.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @param onResult Un callback que recibe un booleano indicando si el inicio de sesión fue exitoso
     * y un mensaje de error (String?) si falló.
     */
    suspend fun iniciarSesion(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        try {
            // Realiza el intento de inicio de sesión con Firebase Authentication.
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            // Si la operación es exitosa, llama al callback con 'true'.
            onResult(true, null)
        } catch (e: Exception) {
            // Si ocurre una excepción, llama al callback con 'false' y el mensaje de error.
            onResult(false, e.localizedMessage ?: "Error al iniciar sesión")
        }
    }
}