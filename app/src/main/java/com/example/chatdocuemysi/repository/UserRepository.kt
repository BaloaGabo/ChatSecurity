package com.example.chatdocuemysi.repository

import com.example.chatdocuemysi.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repositorio para gestionar la obtención de datos de usuarios desde Firebase Realtime Database.
 */
class UserRepository {
    private val usersRef = FirebaseDatabase.getInstance().getReference("Usuarios")
    private val auth     = FirebaseAuth.getInstance()

    /**
     * Obtiene un flujo reactivo de todos los usuarios registrados en la base de datos.
     * Emite una lista de usuarios cada vez que hay un cambio en la base de datos.
     * Si el usuario no está autenticado, emite una lista vacía y cierra el flujo.
     * @return Un flujo de lista de [User].
     */
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        // Si no hay usuario autenticado, emite una lista vacía y cierra el flujo.
        if (auth.currentUser == null) {
            trySend(emptyList()).isSuccess
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                // Itera sobre los hijos del snapshot para construir la lista de usuarios.
                snapshot.children.forEach { child ->
                    child.getValue(User::class.java)?.let { user ->
                        // Añade el UID como parte del objeto User.
                        userList.add(user.copy(uid = child.key ?: ""))
                    }
                }
                trySend(userList).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                // En caso de error (ej. permisos denegados), emite una lista vacía en lugar de una excepción.
                trySend(emptyList()).isSuccess
            }
        }

        usersRef.addValueEventListener(listener)
        // Remueve el listener cuando el flujo ya no es observado.
        awaitClose { usersRef.removeEventListener(listener) }
    }
}
