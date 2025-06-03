// src/main/java/com/example/chatdocuemysi/repository/UserRepository.kt
package com.example.chatdocuemysi.repository

import com.example.chatdocuemysi.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository {
    private val usersRef = FirebaseDatabase.getInstance().getReference("Usuarios")
    private val auth     = FirebaseAuth.getInstance()

    /**
     * Devuelve un Flow que emite la lista de usuarios cada vez que cambia la base de datos,
     * o una lista vacía si el usuario no está autenticado.
     */
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        // Si no hay usuario logueado, emitimos vacío y terminamos
        if (auth.currentUser == null) {
            trySend(emptyList()).isSuccess
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                snapshot.children.forEach { child ->
                    child.getValue(User::class.java)?.let { user ->
                        userList.add(user.copy(uid = child.key ?: ""))
                    }
                }
                trySend(userList).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                // En caso de error (p.ej. permiso denegado al cerrar sesión),
                // en vez de cerrar con excepción, enviamos lista vacía.
                trySend(emptyList()).isSuccess
            }
        }

        usersRef.addValueEventListener(listener)
        awaitClose { usersRef.removeEventListener(listener) }
    }
}
