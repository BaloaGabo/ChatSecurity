// src/main/java/com/example/chatdocuemysi/repository/ChatListRepository.kt
package com.example.chatdocuemysi.repository

import com.example.chatdocuemysi.model.ChatItemModel
import com.example.chatdocuemysi.utils.ChatUtils
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine

class ChatListRepository(private val myUid: String) {
    private val db = FirebaseDatabase.getInstance()
    private val userRef  = db.getReference("Usuarios")
    private val groupRef = db.getReference("ChatsGrupales")
    // <-- ahora sólo bajo mi UID
    private val privateRef = db.getReference("MensajesIndividuales").child(myUid)

    /** Chats grupales en los que participo (sin cambios) */
    fun getGroupChatItems(): Flow<List<ChatItemModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                val out = mutableListOf<ChatItemModel>()
                for (snap in snapshots.children) {
                    val members = snap.child("members").children.mapNotNull { it.key }
                    if (!members.contains(myUid)) continue

                    val chatId = snap.key!!
                    val title  = snap.child("groupName").getValue<String>() ?: "Grupo"
                    val photo  = snap.child("photoUrl").getValue<String>() ?: ""
                    val lastSnap = snap.child("messages").children
                        .maxByOrNull { it.child("timestamp").getValue<Long>() ?: 0L }

                    val lastMsg      = lastSnap?.child("text")?.getValue<String>() ?: ""
                    val lastSenderId = lastSnap?.child("senderId")?.getValue<String>() ?: ""
                    val ts           = lastSnap?.child("timestamp")?.getValue<Long>() ?: 0L

                    if (lastSenderId.isNotBlank()) {
                        userRef.child(lastSenderId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(senderSnap: DataSnapshot) {
                                    val senderName = senderSnap.child("nombres").getValue<String>() ?: ""
                                    out += ChatItemModel(
                                        chatId         = chatId,
                                        title          = title,
                                        photoUrl       = photo,
                                        lastMessage    = lastMsg,
                                        lastSenderName = senderName,
                                        timestamp      = ts,
                                        isGroup        = true
                                    )
                                    if (out.size == snapshots.children.count {
                                            it.child("members").children.mapNotNull { c->c.key }.contains(myUid)
                                        }
                                    ) trySend(out.sortedByDescending { it.timestamp })
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    trySend(out.sortedByDescending { it.timestamp })
                                }
                            })
                    } else {
                        out += ChatItemModel(
                            chatId, title, photo,
                            lastMsg, "", ts, true
                        )
                        if (out.size == snapshots.children.count {
                                it.child("members").children.mapNotNull { c->c.key }.contains(myUid)
                            }
                        ) trySend(out.sortedByDescending { it.timestamp })
                    }
                }
                if (snapshots.childrenCount == 0L) trySend(emptyList())
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        groupRef.addValueEventListener(listener)
        awaitClose { groupRef.removeEventListener(listener) }
    }

    /** Chats privados bajo mi UID */
    fun getPrivateChatItems(): Flow<List<ChatItemModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                val out = mutableListOf<ChatItemModel>()
                // cada hijo es un peerId
                for (snap in snapshots.children) {
                    val peerId = snap.key ?: continue
                    // obtenemos el último mensaje
                    val lastSnap = snap.child("messages").children
                        .maxByOrNull { it.child("timestamp").getValue<Long>() ?: 0L }

                    val lastMsg      = lastSnap?.child("text")?.getValue<String>() ?: ""
                    val lastSenderId = lastSnap?.child("senderId")?.getValue<String>() ?: ""
                    val ts           = lastSnap?.child("timestamp")?.getValue<Long>() ?: 0L

                    // cargamos datos del peer
                    userRef.child(peerId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(peerSnap: DataSnapshot) {
                                val name  = peerSnap.child("nombres").getValue<String>() ?: ""
                                val photo = peerSnap.child("imagen").getValue<String>() ?: ""

                                val senderNameFlow = if (lastSenderId.isNotBlank()) {
                                    // si queremos el nombre del remitente usamos otro listener,
                                    // pero normalmente podemos mostrar sólo el peerName
                                    name
                                } else ""
                                out += ChatItemModel(
                                    chatId         = ChatUtils.generateChatId(myUid, peerId),
                                    title          = name,
                                    photoUrl       = photo,
                                    lastMessage    = lastMsg,
                                    lastSenderName = senderNameFlow,
                                    timestamp      = ts,
                                    isGroup        = false
                                )
                                if (out.size == snapshots.childrenCount.toInt()) {
                                    trySend(out.sortedByDescending { it.timestamp })
                                }
                            }
                            override fun onCancelled(e: DatabaseError) {
                                if (out.size == snapshots.childrenCount.toInt()) {
                                    trySend(out.sortedByDescending { it.timestamp })
                                }
                            }
                        })
                }
                if (snapshots.childrenCount == 0L) trySend(emptyList())
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        privateRef.addValueEventListener(listener)
        awaitClose { privateRef.removeEventListener(listener) }
    }

    /** Combina grupales + privados */
    fun getAllChats(): Flow<List<ChatItemModel>> =
        getGroupChatItems().combine(getPrivateChatItems()) { g, p ->
            (g + p).sortedByDescending { it.timestamp }
        }
}
