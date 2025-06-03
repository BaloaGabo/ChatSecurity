package com.example.chatdocuemysi.model

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class LoginEmailModel {
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun iniciarSesion(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error al iniciar sesión")
        }
    }
}