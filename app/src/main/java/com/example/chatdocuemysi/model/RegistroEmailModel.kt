// src/main/java/com/example/chatdocuemysi/model/RegistroEmailModel.kt
package com.example.chatdocuemysi.model

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class RegistroEmailModel {
    private val auth       = FirebaseAuth.getInstance()
    private val usersRef   = FirebaseDatabase.getInstance().getReference("Usuarios")
    private val storageRef = FirebaseStorage
        .getInstance("gs://chatdocu-emysi-a7687.firebasestorage.app")
        .getReference("imagenesPerfil")

    /**
     * Registra usuario, guarda displayName e imagen, y luego el FCM token.
     */
    suspend fun registrarUsuario(
        email: String,
        password: String,
        displayName: String,
        imageUri: Uri?,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            // 1) Crear cuenta
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: throw Exception("UID nulo tras registro")

            // 2) Subir imagen de perfil si existe
            val imageUrl = imageUri?.let { uri ->
                val ref = storageRef.child(uid).child("profile.jpg")
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            } ?: ""

            // 3) Guardar datos base (nombres + imagen)
            val data = mapOf(
                "nombres"  to displayName,
                "imagen"   to imageUrl
            )
            usersRef.child(uid).setValue(data).await()

            // 4) Obtener token FCM y guardarlo
            val token = FirebaseMessaging.getInstance().token.await()
            usersRef.child(uid).child("fcmToken").setValue(token).await()

            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.localizedMessage)
        }
    }
}
