package com.example.chatdocuemysi.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chatdocuemysi.model.MessageData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * [CoroutineWorker] encargado de limpiar periódicamente los mensajes de imagen expirados
 * de Firebase Storage y Realtime Database.
 */
class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db      = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance().reference.child("chatImages")
    private val now     = System.currentTimeMillis() // Momento actual de ejecución del worker.
    private val ttl     = 1 * 3600_000L  // Tiempo de vida de las imágenes: 48 horas en milisegundos.(de momento esta a 1 hora para pruebas)

    /**
     * Define la tarea principal del worker. Limpia los chats privados y grupales.
     * @return [Result.success] si la operación se completa exitosamente.
     */
    override suspend fun doWork(): Result {
        cleanPrivateChats()
        cleanGroupChats()
        return Result.success()
    }

    /**
     * Recorre todos los chats privados y procesa sus mensajes para limpieza.
     */
    private suspend fun cleanPrivateChats() {
        val root = db.getReference("MensajesIndividuales")
        val users = root.get().await().children // Obtiene todos los UIDs de usuarios raíz.
        for (u1 in users) {
            for (u2 in u1.children) { // Itera sobre los pares de UIDs en cada chat privado.
                val path = "MensajesIndividuales/${u1.key}/${u2.key}/messages"
                // Procesa los mensajes para este chat privado.
                processMessages(path, isGroup = false, pair = Pair(u1.key!!, u2.key!!))
            }
        }
    }

    /**
     * Recorre todos los chats grupales y procesa sus mensajes para limpieza.
     */
    private suspend fun cleanGroupChats() {
        val root = db.getReference("ChatsGrupales")
        val groups = root.get().await().children // Obtiene todos los IDs de grupo.
        for (g in groups) {
            val path = "ChatsGrupales/${g.key}/messages"
            // Procesa los mensajes para este chat grupal.
            processMessages(path, isGroup = true, groupId = g.key!!)
        }
    }

    /**
     * Procesa los mensajes de una ruta de chat específica para eliminar imágenes expiradas.
     * @param path La ruta de los mensajes en la base de datos (e.g., "MensajesIndividuales/u1/u2/messages").
     * @param isGroup Indica si el chat es grupal (true) o privado (false).
     * @param pair Par de UIDs para chats privados (nullable).
     * @param groupId ID del grupo para chats grupales (nullable).
     */
    private suspend fun processMessages(
        path: String,
        isGroup: Boolean,
        pair: Pair<String, String>? = null,
        groupId: String? = null
    ) {
        val snaps = db.getReference(path).get().await() // Obtiene todos los mensajes de la ruta.
        for (msgSnap in snaps.children) {
            val msg         = msgSnap.getValue(MessageData::class.java) ?: continue
            val key         = msgSnap.key ?: continue
            val storagePath = msg.storagePath ?: continue // Ruta de la imagen en Storage.

            // Calcula la expiración: usa 'expiresAt' si está definido, si no, lo infiere de 'timestamp + ttl'.
            val expiration = msg.expiresAt ?: (msg.timestamp + ttl)
            // Si el mensaje es una imagen y ha expirado:
            if (msg.type == "image" && expiration < now) {
                // 1) Elimina el archivo de imagen de Firebase Storage.
                storage.child(storagePath).delete().await()

                // 2) Actualiza la Realtime Database para reflejar la eliminación de la imagen.
                if (!isGroup && pair != null) {
                    // Para chats privados, elimina el mensaje de los nodos de ambos usuarios.
                    val (u1, u2) = pair
                    val base = db.getReference("MensajesIndividuales")
                    base.child(u1).child(u2).child("messages").child(key).removeValue().await()
                    base.child(u2).child(u1).child("messages").child(key).removeValue().await()
                } else if (isGroup && groupId != null) {
                    // Para chats grupales, actualiza el mensaje para indicar que la imagen ha expirado.
                    val msgRef = db.getReference("ChatsGrupales/$groupId/messages/$key")
                    msgRef.child("type").setValue("text").await() // Cambia el tipo a texto.
                    msgRef.child("imageUrl").removeValue().await() // Elimina la URL de la imagen.
                    msgRef.child("storagePath").removeValue().await() // Elimina la ruta de Storage.
                    msgRef.child("text").setValue("🖼️ Imagen expirada").await() // Actualiza el texto.
                    msgRef.child("expiresAt").removeValue().await() // Elimina la marca de expiración.
                }
            }
        }
    }
}




