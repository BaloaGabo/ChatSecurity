package com.example.chatdocuemysi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.repository.ChatGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * # CreateGroupViewModel: Lógica para la Creación de Grupos
 *
 * Este ViewModel se encarga de la lógica y el estado de la pantalla de creación de grupos.
 * Gestiona el nombre del grupo, los miembros seleccionados, la foto del grupo y el proceso de creación en sí.
 *
 * @param myUid El UID del usuario actual que está creando el grupo.
 */
class CreateGroupViewModel(private val myUid: String) : ViewModel() {

    // Instancia del repositorio para interactuar con los datos de los grupos de chat.
    private val repo = ChatGroupRepository()

    // --- Estados de la UI ---
    // MutableStateFlow para el nombre del grupo.
    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName

    // MutableStateFlow para la lista de UIDs de los miembros seleccionados.
    private val _selectedMembers = MutableStateFlow<List<String>>(emptyList())
    val selectedMembers: StateFlow<List<String>> = _selectedMembers

    // MutableStateFlow para la URI de la foto del grupo seleccionada.
    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    // MutableStateFlow para indicar si el proceso de creación está en curso.
    private val _creating = MutableStateFlow(false)
    val creating: StateFlow<Boolean> = _creating

    // --- Funciones para actualizar el estado ---
    /**
     * Actualiza el nombre del grupo.
     * @param name El nuevo nombre del grupo.
     */
    fun onNameChange(name: String) { _groupName.value = name }

    /**
     * Actualiza la lista de miembros seleccionados.
     * @param list La nueva lista de UIDs de miembros.
     */
    fun onMembersChange(list: List<String>) { _selectedMembers.value = list }

    /**
     * Actualiza la URI de la foto del grupo.
     * @param uri La nueva URI de la foto.
     */
    fun onPhotoChange(uri: Uri) { _photoUri.value = uri }

    /**
     * Inicia el proceso de creación del grupo.
     *
     * @param onResult Una función lambda que se llama al finalizar la creación,
     * indicando si fue exitosa (Boolean) y el ID del grupo (String?) en caso de éxito,
     * o un mensaje de error en caso de fallo.
     */
    fun create(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch { // Se lanza una corrutina en el ámbito del ViewModel.
            _creating.value = true // Se establece el estado 'creating' a true para indicar que el proceso ha comenzado.
            try {
                // Se llama al repositorio para crear el grupo, pasando todos los datos necesarios
                // incluyendo el `myUid` como `creatorUid` explícito.
                val id = repo.createGroup(
                    _groupName.value,
                    _photoUri.value,
                    _selectedMembers.value,
                    myUid
                )
                onResult(true, id) // Se notifica el éxito y el ID del grupo creado.
            } catch(e: Exception) {
                // Si ocurre un error, se notifica el fallo y el mensaje de error.
                onResult(false, e.localizedMessage)
            } finally {
                // Finalmente, se restablece el estado 'creating' a false, independientemente del resultado.
                _creating.value = false
            }
        }
    }
}
