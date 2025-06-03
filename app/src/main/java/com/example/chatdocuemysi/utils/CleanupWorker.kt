package com.example.chatdocuemysi.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chatdocuemysi.model.MessageData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db      = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance().reference.child("chatImages")
    private val now     = System.currentTimeMillis()
    private val ttl     = 48 * 3600_000L  // 48 horas en milisegundos

    override suspend fun doWork(): Result {
        cleanPrivateChats()
        cleanGroupChats()
        return Result.success()
    }

    private suspend fun cleanPrivateChats() {
        val root = db.getReference("MensajesIndividuales")
        val users = root.get().await().children
        for (u1 in users) {
            for (u2 in u1.children) {
                val path = "MensajesIndividuales/${u1.key}/${u2.key}/messages"
                processMessages(path, isGroup = false, pair = Pair(u1.key!!, u2.key!!))
            }
        }
    }

    private suspend fun cleanGroupChats() {
        val root = db.getReference("ChatsGrupales")
        val groups = root.get().await().children
        for (g in groups) {
            val path = "ChatsGrupales/${g.key}/messages"
            processMessages(path, isGroup = true, groupId = g.key!!)
        }
    }

    private suspend fun processMessages(
        path: String,
        isGroup: Boolean,
        pair: Pair<String, String>? = null,
        groupId: String? = null
    ) {
        val snaps = db.getReference(path).get().await()
        for (msgSnap in snaps.children) {
            val msg         = msgSnap.getValue(MessageData::class.java) ?: continue
            val key         = msgSnap.key ?: continue
            val storagePath = msg.storagePath ?: continue

            // calculamos expiración: si hay expiresAt lo usamos,
            // si no, lo inferimos desde timestamp + TTL
            val expiration = msg.expiresAt ?: (msg.timestamp + ttl)
            if (msg.type == "image" && expiration < now) {
                // 1) borrar fichero de Storage
                storage.child(storagePath).delete().await()

                // 2) actualizar RTDB
                if (!isGroup && pair != null) {
                    val (u1, u2) = pair
                    val base = db.getReference("MensajesIndividuales")
                    base.child(u1).child(u2).child("messages").child(key).removeValue().await()
                    base.child(u2).child(u1).child("messages").child(key).removeValue().await()
                } else if (isGroup && groupId != null) {
                    val msgRef = db.getReference("ChatsGrupales/$groupId/messages/$key")
                    // convertimos el nodo a un texto indicativo
                    msgRef.child("type").setValue("text").await()
                    msgRef.child("imageUrl").removeValue().await()
                    msgRef.child("storagePath").removeValue().await()
                    msgRef.child("text").setValue("🖼️ Imagen expirada").await()
                    msgRef.child("expiresAt").removeValue().await()
                }
            }
        }
    }
}




