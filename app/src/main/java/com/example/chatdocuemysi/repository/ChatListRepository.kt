package com.example.chatdocuemysi.repository

import com.example.chatdocuemysi.model.ChatItemModel
import com.example.chatdocuemysi.utils.ChatUtils
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
/**
 * Repositorio para gestionar la lista de chats (privados y grupales) de un usuario.
 * @param myUid El UID del usuario actual.
 */
class ChatListRepository(private val myUid: String) {
    private val db = FirebaseDatabase.getInstance()
    private val userRef  = db.getReference("Usuarios")
    private val groupRef = db.getReference("ChatsGrupales")
    // Referencia a los chats privados del usuario actual.
    private val privateRef = db.getReference("MensajesIndividuales").child(myUid)

    /**
     * Obtiene una lista de elementos de chat grupal a los que pertenece el usuario.
     * @return Un flujo de lista de [ChatItemModel] para chats grupales.
     */
    fun getGroupChatItems(): Flow<List<ChatItemModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                val out = mutableListOf<ChatItemModel>()
                for (snap in snapshots.children) {
                    // Verifica si el usuario actual es miembro del grupo.
                    val members = snap.child("members").children.mapNotNull { it.key }
                    if (!members.contains(myUid)) continue

                    val chatId = snap.key!!
                    val title  = snap.child("groupName").getValue<String>() ?: "Grupo"
                    val photo  = snap.child("photoUrl").getValue<String>() ?: ""
                    // Obtiene el último mensaje del grupo.
                    val lastSnap = snap.child("messages").children
                        .maxByOrNull { it.child("timestamp").getValue<Long>() ?: 0L }

                    val lastMsg      = lastSnap?.child("text")?.getValue<String>() ?: ""
                    val lastSenderId = lastSnap?.child("senderId")?.getValue<String>() ?: ""
                    val ts           = lastSnap?.child("timestamp")?.getValue<Long>() ?: 0L

                    // Si hay un remitente, busca su nombre.
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
                                    // Envía la lista cuando todos los datos de remitentes se hayan cargado.
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
                        // Si no hay remitente de último mensaje.
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
                // Si no hay chats grupales, envía una lista vacía.
                if (snapshots.childrenCount == 0L) trySend(emptyList())
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        groupRef.addValueEventListener(listener)
        awaitClose { groupRef.removeEventListener(listener) }
    }

    /**
     * Obtiene una lista de elementos de chat privado para el usuario.
     * @return Un flujo de lista de [ChatItemModel] para chats privados.
     */
    fun getPrivateChatItems(): Flow<List<ChatItemModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                val out = mutableListOf<ChatItemModel>()
                // Itera sobre cada chat privado del usuario.
                for (snap in snapshots.children) {
                    val peerId = snap.key ?: continue
                    // Obtiene el último mensaje del chat privado.
                    val lastSnap = snap.child("messages").children
                        .maxByOrNull { it.child("timestamp").getValue<Long>() ?: 0L }

                    val lastMsg      = lastSnap?.child("text")?.getValue<String>() ?: ""
                    val lastSenderId = lastSnap?.child("senderId")?.getValue<String>() ?: ""
                    val ts           = lastSnap?.child("timestamp")?.getValue<Long>() ?: 0L

                    // Carga los datos del otro usuario (peer).
                    userRef.child(peerId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(peerSnap: DataSnapshot) {
                                val name  = peerSnap.child("nombres").getValue<String>() ?: ""
                                val photo = peerSnap.child("imagen").getValue<String>() ?: ""

                                // El nombre del remitente en chats privados es el nombre del peer
                                val senderNameFlow = if (lastSenderId.isNotBlank()) {
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
                                // Envía la lista cuando todos los datos de los peers se hayan cargado.
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
                // Si no hay chats privados, envía una lista vacía.
                if (snapshots.childrenCount == 0L) trySend(emptyList())
            }
            override fun onCancelled(e: DatabaseError) { close(e.toException()) }
        }
        privateRef.addValueEventListener(listener)
        awaitClose { privateRef.removeEventListener(listener) }
    }

    /**
     * Combina los flujos de chats grupales y privados en un único flujo.
     * Los chats se ordenan por la marca de tiempo del último mensaje.
     * @return Un flujo de lista de [ChatItemModel] que contiene todos los chats del usuario.
     */
    fun getAllChats(): Flow<List<ChatItemModel>> =
        getGroupChatItems().combine(getPrivateChatItems()) { g, p ->
            (g + p).sortedByDescending { it.timestamp }
        }
}
