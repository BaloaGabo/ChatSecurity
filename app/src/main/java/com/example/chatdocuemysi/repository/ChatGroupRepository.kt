// src/main/java/com/example/chatdocuemysi/repository/ChatGroupRepository.kt
package com.example.chatdocuemysi.repository

import android.net.Uri
import com.example.chatdocuemysi.model.GroupDetail
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatGroupRepository {
    private val db        = FirebaseDatabase.getInstance()
    private val groupsRef = db.getReference("ChatsGrupales")
    private val storageRef = FirebaseStorage.getInstance().getReference("imagenesGrupo")

    /** Crea un nuevo grupo con un solo admin (el creador) */
    suspend fun createGroup(
        name: String,
        photoUri: Uri?,
        memberIds: List<String>,  // lista de miembros sin crear
        creatorUid: String        // UID del creador
    ): String {
        val groupId = groupsRef.push().key ?: UUID.randomUUID().toString()
        val photoUrl = photoUri?.let {
            val ref = storageRef.child(groupId)
            ref.putFile(it).await()
            ref.downloadUrl.await().toString()
        } ?: ""

        val membersMap = (memberIds + creatorUid).distinct().associateWith { true }
        val adminsMap  = mapOf(creatorUid to true)

        val data = mapOf<String, Any>(
            "groupName" to name,
            "photoUrl"  to photoUrl,
            "members"   to membersMap,
            "admins"    to adminsMap
        )
        groupsRef.child(groupId).setValue(data).await()
        return groupId
    }

    /** Flujo de detalles del grupo (nombre, foto, lista de miembros y admins) */
    fun getGroupDetail(groupId: String): Flow<GroupDetail> = callbackFlow {
        val node = groupsRef.child(groupId)
        val listener = object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val name    = snap.child("groupName").value as? String ?: ""
                val photo   = snap.child("photoUrl").value as? String ?: ""
                val members = snap.child("members").children.mapNotNull { it.key }
                val admins  = snap.child("admins").children.mapNotNull { it.key }
                trySend(
                    GroupDetail(
                        groupId  = groupId,
                        groupName= name,
                        photoUrl = photo,
                        members  = members,
                        admins   = admins
                    )
                )
            }
            override fun onCancelled(err: com.google.firebase.database.DatabaseError) {
                close(err.toException())
            }
        }
        node.addValueEventListener(listener)
        awaitClose { node.removeEventListener(listener) }
    }

    /** Promociona a admin */
    suspend fun promoteToAdmin(groupId: String, uid: String) {
        groupsRef.child(groupId)
            .child("admins")
            .child(uid)
            .setValue(true)
            .await()
    }

    /** Despromueve a admin; si queda ningún admin, nombra al miembro más antiguo */
    suspend fun demoteAdmin(groupId: String, uid: String) {
        val base = groupsRef.child(groupId)
        base.child("admins").child(uid).removeValue().await()
        val adminsSnap = base.child("admins").get().await()
        if (!adminsSnap.hasChildren()) {
            val membersSnap = base.child("members").get().await()
            val firstMember = membersSnap.children.firstOrNull()?.key
            firstMember?.let { base.child("admins").child(it).setValue(true).await() }
        }
    }

    /** Añade miembro */
    suspend fun addMember(groupId: String, uid: String) {
        groupsRef.child(groupId)
            .child("members")
            .child(uid)
            .setValue(true)
            .await()
    }

    /** Elimina miembro y actualiza admins si es necesario */
    suspend fun removeMember(groupId: String, uid: String) {
        val base = groupsRef.child(groupId)
        base.child("members").child(uid).removeValue().await()
        base.child("admins").child(uid).removeValue().await()
        val adminsSnap = base.child("admins").get().await()
        if (!adminsSnap.hasChildren()) {
            val membersSnap = base.child("members").get().await()
            val firstMember = membersSnap.children.firstOrNull()?.key
            firstMember?.let { base.child("admins").child(it).setValue(true).await() }
        }
    }
}



