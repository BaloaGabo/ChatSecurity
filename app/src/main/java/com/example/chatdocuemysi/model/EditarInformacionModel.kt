package com.example.chatdocuemysi.model

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Modelo para la pantalla de edición de información del usuario.
 * Gestiona la carga, actualización de nombre y foto de perfil del usuario en Firebase.
 */
class EditarInformacionModel {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database     = FirebaseDatabase.getInstance().getReference("Usuarios")
    // Instancia de Firebase Storage apuntando a la ruta de imágenes de perfil.
    private val storageRoot  = FirebaseStorage
        .getInstance("gs://chatdocu-emysi-a7687.firebasestorage.app")
        .getReference("imagenesPerfil")

    /**
     * Carga la información del usuario autenticado (nombre y URL de imagen).
     * @param onResult Callback que retorna el nombre, URL de imagen, estado de éxito y mensaje de error.
     */
    suspend fun cargarInformacion(onResult: (String, String, Boolean, String?) -> Unit) {
        // Obtiene el ID del usuario actual; si no hay, retorna error.
        val userId = firebaseAuth.uid ?: return onResult("", "", false, "Usuario no autenticado")
        try {
            // Consulta los datos del usuario en la base de datos de Firebase.
            val snap     = database.child(userId).get().await()
            // Extrae el nombre y la URL de la imagen del snapshot.
            val nombres  = snap.child("nombres").getValue(String::class.java) ?: ""
            val imageUrl = snap.child("imagen" ).getValue(String::class.java) ?: ""
            // Retorna los datos y éxito.
            onResult(nombres, imageUrl, true, null)
        } catch (e: Exception) {
            // En caso de excepción, retorna un mensaje de error.
            onResult("", "", false, e.localizedMessage)
        }
    }
    /**
     * Actualiza la imagen de perfil del usuario.
     * Sube la nueva imagen a Firebase Storage y guarda su URL en Firebase Realtime Database.
     * @param imagenUri URI local de la nueva imagen.
     * @param onResult Callback que retorna la nueva URL de la imagen, estado de éxito y mensaje de error.
     */
    suspend fun actualizarImagen(imagenUri: Uri, onResult: (String?, Boolean, String?) -> Unit) {
        // Obtiene el ID del usuario; si no hay, retorna error.
        val userId = firebaseAuth.uid ?: return onResult(null, false, "Usuario no autenticado")
        try {
            // Define la referencia para la imagen de perfil (uid/profile.jpg).
            val ref      = storageRoot.child(userId).child("profile.jpg")
            ref.putFile(imagenUri).await() // Sube el archivo.
            val imageUrl = ref.downloadUrl.await().toString()// Obtiene la URL de descarga.
            // Guarda la URL de la imagen en la base de datos del usuario.
            database.child(userId).child("imagen").setValue(imageUrl).await()
            onResult(imageUrl, true, null)
        } catch (e: Exception) {
            onResult(null, false, e.localizedMessage)
        }
    }

    /**
     * Actualiza el nombre del usuario.
     * @param nombres El nuevo nombre a establecer.
     * @param onResult Callback que retorna el estado de éxito y un mensaje de error.
     */
    suspend fun actualizarInfo(nombres: String, onResult: (Boolean, String?) -> Unit) {
        // Obtiene el ID del usuario; si no hay, retorna error
        val userId = firebaseAuth.uid ?: return onResult(false, "Usuario no autenticado")
        try {
            // Actualiza el campo "nombres" en la base de datos del usuario.
            database.child(userId).child("nombres").setValue(nombres).await()
            // Retorna éxito
            onResult(true, null)
        } catch (e: Exception) {
            // En caso de excepción, retorna un mensaje de error.
            onResult(false, e.localizedMessage)
        }
    }
}

