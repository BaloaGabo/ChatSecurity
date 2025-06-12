package com.example.chatdocuemysi.model

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Modelo para la lógica de registro de nuevos usuarios con email y contraseña.
 * Se encarga de crear la cuenta, subir la imagen de perfil y guardar los datos del usuario,
 * incluyendo el token FCM, en Firebase.
 */
class RegistroEmailModel {
    private val auth       = FirebaseAuth.getInstance()
    private val usersRef   = FirebaseDatabase.getInstance().getReference("Usuarios")
    // Referencia al almacenamiento de imágenes de perfil en Firebase Storage.
    private val storageRef = FirebaseStorage
        .getInstance("gs://chatdocu-emysi-a7687.firebasestorage.app")
        .getReference("imagenesPerfil")

    /**
     * Registra un nuevo usuario con email y contraseña, guarda su nombre de visualización
     * y la imagen de perfil (si se proporciona), y registra su token FCM.
     *
     * @param email El correo electrónico del usuario a registrar.
     * @param password La contraseña del usuario.
     * @param displayName El nombre de visualización del usuario.
     * @param imageUri La URI de la imagen de perfil a subir (opcional).
     * @param onResult Un callback que recibe un booleano indicando si el registro fue exitoso
     * y un mensaje de error (String?) si falló.
     */
    suspend fun registrarUsuario(
        email: String,
        password: String,
        displayName: String,
        imageUri: Uri?,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            // 1) Crea la cuenta de usuario en Firebase Authentication.
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: throw Exception("UID nulo tras registro")// Lanza una excepción si el UID es nulo.

            // 2) Sube la imagen de perfil a Firebase Storage si se proporciona.
            val imageUrl = imageUri?.let { uri ->
                val ref = storageRef.child(uid).child("profile.jpg") // Define la ruta de almacenamiento
                ref.putFile(uri).await() // Sube el archivo.
                ref.downloadUrl.await().toString() // Obtiene la URL de descarga.
            } ?: ""

            // 3) Guarda los datos básicos del usuario (nombre e URL de imagen) en Firebase Realtime Database.
            val data = mapOf(
                "nombres"  to displayName,
                "imagen"   to imageUrl
            )
            usersRef.child(uid).setValue(data).await()

            // 4) Obtiene el token de Firebase Cloud Messaging (FCM) y lo guarda en la base de datos.
            val token = FirebaseMessaging.getInstance().token.await()
            usersRef.child(uid).child("fcmToken").setValue(token).await()

            onResult(true, null)
            // Si todas las operaciones son exitosas, llama al callback con 'true'.
        } catch (e: Exception) {
            // Si ocurre un error en cualquier paso, llama al callback con 'false' y el mensaje de error.
            onResult(false, e.localizedMessage)
        }
    }
}
