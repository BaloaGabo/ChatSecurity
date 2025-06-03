// src/main/java/com/example/chatdocuemysi/repository/MessagesRepository.kt
package com.example.chatdocuemysi.repository

import android.net.Uri
import com.example.chatdocuemysi.model.MessageData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

class MessagesRepository {
    private val db      = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance().reference.child("chatImages")

    /** Flujo de mensajes (texto e imagen) para cualquier chatPath */
    fun getMessages(chatPath: String): Flow<List<MessageData>> = callbackFlow {
        val ref = db.getReference("$chatPath/messages")
        var last: List<MessageData>? = null

        val listener = ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = snapshot.children
                    .mapNotNull { it.getValue(MessageData::class.java)?.copy(id = it.key ?: "") }
                    .sortedBy { it.timestamp }
                if (list != last) {
                    last = list
                    trySend(list)
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Envía un mensaje de texto, distinguiendo privado vs. grupal */
    fun sendTextMessage(chatPath: String, senderId: String, text: String) {
        if (text.isBlank()) return
        val ts  = System.currentTimeMillis()
        val key = db.getReference("$chatPath/messages").push().key ?: return
        val msg = MessageData(
            id         = key,
            senderId   = senderId,
            text       = text,
            imageUrl   = null,
            storagePath= null,
            expiresAt  = null,
            type       = "text",
            timestamp  = ts
        )

        if (chatPath.startsWith("MensajesIndividuales/")) {
            // Chat privado: duplicar en ambos nodos
            val parts = chatPath.removePrefix("MensajesIndividuales/").split("/")
            if (parts.size == 2) {
                val (u1, u2) = parts
                db.getReference("MensajesIndividuales/$u1/$u2/messages/$key").setValue(msg)
                db.getReference("MensajesIndividuales/$u2/$u1/messages/$key").setValue(msg)
            }
        } else {
            // Chat grupal
            db.getReference("$chatPath/messages/$key").setValue(msg)
        }
    }

    /** Envía un mensaje de imagen, distinguiendo privado vs. grupal y poniendo expiresAt en ambos casos */
    suspend fun sendImageMessage(chatPath: String, senderId: String, imageUri: Uri) {
        val ts = System.currentTimeMillis()
        val expiresAt = ts + 48 * 3600_000L  // 48 horas en ms
        val imageName = "${UUID.randomUUID()}.jpg"

        // Definir storagePath distinto para privado vs. grupo
        val storagePath = if (chatPath.startsWith("MensajesIndividuales/")) {
            // p.ej. "uid1_uid2/uuid.jpg"
            chatPath.removePrefix("MensajesIndividuales/").replace("/","_") + "/$imageName"
        } else {
            // p.ej. "groupId/uuid.jpg"
            chatPath.removePrefix("ChatsGrupales/") + "/$imageName"
        }

        // 1) Subir a Storage
        val imageRef = storage.child(storagePath)
        imageRef.putFile(imageUri).await()
        val url = imageRef.downloadUrl.await().toString()

        // 2) Construir MessageData con expiresAt siempre
        val key = db.getReference("$chatPath/messages").push().key ?: return
        val msg = MessageData(
            id          = key,
            senderId    = senderId,
            text        = "",
            imageUrl    = url,
            storagePath = storagePath,
            expiresAt   = expiresAt,
            type        = "image",
            timestamp   = ts
        )

        // 3) Guardar en Firebase (duplicar para privado)
        if (chatPath.startsWith("MensajesIndividuales/")) {
            val parts = chatPath.removePrefix("MensajesIndividuales/").split("/")
            if (parts.size == 2) {
                val (u1, u2) = parts
                db.getReference("MensajesIndividuales/$u1/$u2/messages/$key").setValue(msg)
                db.getReference("MensajesIndividuales/$u2/$u1/messages/$key").setValue(msg)
            }
        } else {
            db.getReference("$chatPath/messages/$key").setValue(msg)
        }
    }
}
