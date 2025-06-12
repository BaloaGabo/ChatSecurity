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

/**
 * Repositorio para gestionar el envío y la recuperación de mensajes (texto e imagen) en chats.
 */
class MessagesRepository {
    private val db      = FirebaseDatabase.getInstance()
    // Referencia al almacenamiento de imágenes de chat en Firebase Storage.
    private val storage = FirebaseStorage.getInstance().reference.child("chatImages")

    /**
     * Obtiene un flujo reactivo de mensajes para una ruta de chat específica.
     * Emite una lista de mensajes cada vez que hay un cambio en la base de datos.
     * @param chatPath La ruta del chat (e.g., "MensajesIndividuales/user1/user2" o "ChatsGrupales/groupId").
     * @return Un flujo de lista de [MessageData].
     */
    fun getMessages(chatPath: String): Flow<List<MessageData>> = callbackFlow {
        val ref = db.getReference("$chatPath/messages")
        var last: List<MessageData>? = null

        val listener = ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                // Mapea los DataSnapshots a objetos MessageData y los ordena por timestamp.
                val list = snapshot.children
                    .mapNotNull { it.getValue(MessageData::class.java)?.copy(id = it.key ?: "") }
                    .sortedBy { it.timestamp }
                // Solo emite la lista si ha cambiado.
                if (list != last) {
                    last = list
                    trySend(list)
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        // Elimina el listener cuando el flujo deja de ser observado.
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Envía un mensaje de texto a un chat específico.
     * Maneja la duplicación para chats individuales.
     * @param chatPath La ruta del chat.
     * @param senderId El UID del remitente.
     * @param text El contenido del mensaje de texto.
     */
    fun sendTextMessage(chatPath: String, senderId: String, text: String) {
        if (text.isBlank()) return
        val ts  = System.currentTimeMillis()
        val key = db.getReference("$chatPath/messages").push().key ?: return // Genera una clave única para el mensaje.
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
            // Chat privado: guarda el mensaje en los nodos de ambos usuarios.
            val parts = chatPath.removePrefix("MensajesIndividuales/").split("/")
            if (parts.size == 2) {
                val (u1, u2) = parts
                db.getReference("MensajesIndividuales/$u1/$u2/messages/$key").setValue(msg)
                db.getReference("MensajesIndividuales/$u2/$u1/messages/$key").setValue(msg)
            }
        } else {
            // Chat grupal: guarda el mensaje directamente en el nodo del grupo.
            db.getReference("$chatPath/messages/$key").setValue(msg)
        }
    }

    /**
     * Envía un mensaje de imagen a un chat específico.
     * Sube la imagen a Storage, genera una URL y la guarda en la base de datos con una fecha de caducidad.
     * Maneja la duplicación para chats individuales.
     * @param chatPath La ruta del chat.
     * @param senderId El UID del remitente.
     * @param imageUri La URI local de la imagen a enviar.
     */
    suspend fun sendImageMessage(chatPath: String, senderId: String, imageUri: Uri) {
        val ts = System.currentTimeMillis()
        val expiresAt = ts + 1 * 3600_000L  // Calcula la fecha de caducidad (48 horas).(de momento esta a 1 hora para pruebas)
        val imageName = "${UUID.randomUUID()}.jpg" // Genera un nombre de archivo único para la imagen.

        // Define la ruta de almacenamiento de la imagen en Firebase Storage.
        val storagePath = if (chatPath.startsWith("MensajesIndividuales/")) {
            // Para chats privados: "uid1_uid2/uuid.jpg"
            chatPath.removePrefix("MensajesIndividuales/").replace("/","_") + "/$imageName"
        } else {
            // Para chats grupales: "groupId/uuid.jpg"
            chatPath.removePrefix("ChatsGrupales/") + "/$imageName"
        }

        // 1) Sube la imagen a Firebase Storage.
        val imageRef = storage.child(storagePath)
        imageRef.putFile(imageUri).await()
        val url = imageRef.downloadUrl.await().toString() // Obtiene la URL de descarga de la imagen.

        // 2) Construye el objeto MessageData con los detalles de la imagen.
        val key = db.getReference("$chatPath/messages").push().key ?: return
        val msg = MessageData(
            id          = key,
            senderId    = senderId,
            text        = "",// El texto es vacío para mensajes de imagen.
            imageUrl    = url,
            storagePath = storagePath,
            expiresAt   = expiresAt,
            type        = "image",
            timestamp   = ts
        )

        // 3) Guarda el mensaje en Firebase Realtime Database.
        if (chatPath.startsWith("MensajesIndividuales/")) {
            // Chat privado: duplica el mensaje en los nodos de ambos usuarios.
            val parts = chatPath.removePrefix("MensajesIndividuales/").split("/")
            if (parts.size == 2) {
                val (u1, u2) = parts
                db.getReference("MensajesIndividuales/$u1/$u2/messages/$key").setValue(msg)
                db.getReference("MensajesIndividuales/$u2/$u1/messages/$key").setValue(msg)
            }
        } else {
            // Chat grupal: guarda el mensaje directamente en el nodo del grupo.
            db.getReference("$chatPath/messages/$key").setValue(msg)
        }
    }
}
