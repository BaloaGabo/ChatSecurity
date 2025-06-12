package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.User
import com.example.chatdocuemysi.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UserListViewModel(
    // Inyectamos el UserRepository por defecto, que es el encargado de obtener los datos de los usuarios.
    private val repo: UserRepository = UserRepository()
) : ViewModel() {

    /**
     * `users` es un [StateFlow] que contiene una lista de todos los [User] disponibles.
     * Este flujo es ideal para observar cambios en la lista de usuarios desde la UI.
     *
     * - **`repo.getAllUsers()`**: Obtiene un `Flow` de listas de usuarios desde el repositorio.
     * - **`.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())`**:
     * Convierte el `Flow` en un `StateFlow`.
     * - `viewModelScope`: El ámbito de corrutinas donde se mantendrá activo el `StateFlow`.
     * - `SharingStarted.WhileSubscribed(5000)`: El flujo se mantiene activo mientras haya suscriptores
     * y durante 5 segundos adicionales después de que el último suscriptor desaparezca,
     * lo que ayuda a evitar reconexiones rápidas innecesarias.
     * - `emptyList()`: El valor inicial que emitirá el `StateFlow` antes de que se carguen los datos.
     */
    val users: StateFlow<List<User>> = repo.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * `userMap` es un [StateFlow] que contiene un mapa donde la clave es el UID del usuario (String)
     * y el valor es el objeto [User] completo. Este formato es muy útil para buscar usuarios
     * rápidamente por su ID, por ejemplo, al mostrar mensajes y necesitar el nombre del remitente.
     *
     * - **`users.map { list -> list.associateBy { it.uid } }`**:
     * Transforma la `List<User>` del flujo `users` en un `Map<String, User>`. `associateBy`
     * crea el mapa utilizando el `uid` de cada usuario como clave.
     * - **`.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())`**:
     * Convierte el `Flow` resultante en un `StateFlow`.
     * - `SharingStarted.Eagerly`: El flujo se inicia inmediatamente y se mantiene activo
     * incluso si no hay suscriptores. Esto asegura que el `userMap` esté siempre listo.
     * - `emptyMap()`: El valor inicial del mapa antes de que se carguen los datos.
     */
    val userMap: StateFlow<Map<String,User>> = users
        .map { list -> list.associateBy { it.uid } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
}
