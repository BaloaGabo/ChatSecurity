package com.example.chatdocuemysi.model

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class EditarInformacionModel {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database     = FirebaseDatabase.getInstance().getReference("Usuarios")
    // Instancia explícita apuntando al bucket correcto
    private val storageRoot  = FirebaseStorage
        .getInstance("gs://chatdocu-emysi-a7687.firebasestorage.app")
        .getReference("imagenesPerfil")

    suspend fun cargarInformacion(onResult: (String, String, Boolean, String?) -> Unit) {
        val userId = firebaseAuth.uid ?: return onResult("", "", false, "Usuario no autenticado")
        try {
            val snap     = database.child(userId).get().await()
            val nombres  = snap.child("nombres").getValue(String::class.java) ?: ""
            val imageUrl = snap.child("imagen" ).getValue(String::class.java) ?: ""
            onResult(nombres, imageUrl, true, null)
        } catch (e: Exception) {
            onResult("", "", false, e.localizedMessage)
        }
    }

    suspend fun actualizarImagen(imagenUri: Uri, onResult: (String?, Boolean, String?) -> Unit) {
        val userId = firebaseAuth.uid ?: return onResult(null, false, "Usuario no autenticado")
        try {
            // sube como "imagenesPerfil/{uid}/profile.jpg"
            val ref      = storageRoot.child(userId).child("profile.jpg")
            ref.putFile(imagenUri).await()
            val imageUrl = ref.downloadUrl.await().toString()
            // guarda URL en la DB
            database.child(userId).child("imagen").setValue(imageUrl).await()
            onResult(imageUrl, true, null)
        } catch (e: Exception) {
            onResult(null, false, e.localizedMessage)
        }
    }

    suspend fun actualizarInfo(nombres: String, onResult: (Boolean, String?) -> Unit) {
        val userId = firebaseAuth.uid ?: return onResult(false, "Usuario no autenticado")
        try {
            database.child(userId).child("nombres").setValue(nombres).await()
            onResult(true, null)
        } catch (e: Exception) {
            onResult(false, e.localizedMessage)
        }
    }
}

