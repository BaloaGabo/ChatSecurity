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

/**
 * Repositorio para gestionar operaciones relacionadas con grupos de chat en Firebase.
 */

class ChatGroupRepository {
    private val db        = FirebaseDatabase.getInstance()
    private val groupsRef = db.getReference("ChatsGrupales")
    private val storageRef = FirebaseStorage.getInstance().getReference("imagenesGrupo")

    /**
     * Crea un nuevo grupo con el creador como único administrador.
     * @param name Nombre del grupo.
     * @param photoUri URI de la foto del grupo (opcional).
     * @param memberIds Lista de UIDs de los miembros iniciales.
     * @param creatorUid UID del usuario que crea el grupo.
     * @return El ID del grupo recién creado.
     */
    suspend fun createGroup(
        name: String,
        photoUri: Uri?,
        memberIds: List<String>,
        creatorUid: String
    ): String {
        val groupId = groupsRef.push().key ?: UUID.randomUUID().toString()
        // Sube la foto del grupo si se proporciona.
        val photoUrl = photoUri?.let {
            val ref = storageRef.child(groupId)
            ref.putFile(it).await()
            ref.downloadUrl.await().toString()
        } ?: ""

        // Combina miembros y creador, asegurando unicidad.
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

    /**
     * Obtiene los detalles de un grupo como un flujo reactivo.
     * Emite actualizaciones cada vez que los detalles del grupo cambian en la base de datos.
     * @param groupId ID del grupo.
     * @return Un flujo de [GroupDetail].
     */
    fun getGroupDetail(groupId: String): Flow<GroupDetail> = callbackFlow {
        val node = groupsRef.child(groupId)
        val listener = object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                // Parsea los datos del grupo desde el DataSnapshot.
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

    /**
     * Promociona a un miembro a administrador del grupo.
     * @param groupId ID del grupo.
     * @param uid UID del miembro a promocionar.
     */
    suspend fun promoteToAdmin(groupId: String, uid: String) {
        groupsRef.child(groupId)
            .child("admins")
            .child(uid)
            .setValue(true)
            .await()
    }

    /**
     * Despromueve a un administrador del grupo.
     * Si no quedan administradores, el miembro más antiguo se convierte en admin.
     * @param groupId ID del grupo.
     * @param uid UID del administrador a despromover.
     */
    suspend fun demoteAdmin(groupId: String, uid: String) {
        val base = groupsRef.child(groupId)
        base.child("admins").child(uid).removeValue().await()
        val adminsSnap = base.child("admins").get().await()
        // Si no quedan administradores, asigna el miembro más antiguo.
        if (!adminsSnap.hasChildren()) {
            val membersSnap = base.child("members").get().await()
            val firstMember = membersSnap.children.firstOrNull()?.key
            firstMember?.let { base.child("admins").child(it).setValue(true).await() }
        }
    }

    /**
     * Añade un miembro a un grupo.
     * @param groupId ID del grupo.
     * @param uid UID del miembro a añadir.
     */
    suspend fun addMember(groupId: String, uid: String) {
        groupsRef.child(groupId)
            .child("members")
            .child(uid)
            .setValue(true)
            .await()
    }

    /**
     * Elimina un miembro de un grupo.
     * También lo remueve como admin si lo era. Si no quedan admins, asigna al miembro más antiguo.
     * @param groupId ID del grupo.
     * @param uid UID del miembro a eliminar.
     */
    suspend fun removeMember(groupId: String, uid: String) {
        val base = groupsRef.child(groupId)
        base.child("members").child(uid).removeValue().await()
        base.child("admins").child(uid).removeValue().await()
        val adminsSnap = base.child("admins").get().await()
        // Si no quedan administradores, asigna el miembro más antiguo.
        if (!adminsSnap.hasChildren()) {
            val membersSnap = base.child("members").get().await()
            val firstMember = membersSnap.children.firstOrNull()?.key
            firstMember?.let { base.child("admins").child(it).setValue(true).await() }
        }
    }
}



