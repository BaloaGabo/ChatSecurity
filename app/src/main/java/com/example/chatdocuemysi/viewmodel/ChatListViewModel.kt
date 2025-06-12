package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.ChatItemModel
import com.example.chatdocuemysi.repository.ChatListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ## ViewModel para la Lista de Chats
 *
 * `ChatListViewModel` es responsable de proporcionar los datos de la lista de chats a la interfaz de usuario
 * y de manejar la lógica de negocio relacionada. Interactúa con `ChatListRepository`
 * para obtener los datos de los chats.
 *
 * @param repository Una instancia de [ChatListRepository] que proporciona los datos de los chats.
 */
class ChatListViewModel(repository: ChatListRepository) : ViewModel() {

    /**
     * `chatItems` es un [StateFlow] que emite una lista de [ChatItemModel].
     * Representa el estado actual de la lista de chats que se mostrará en la UI.
     *
     * Se inicializa recolectando el flujo de todos los chats desde el [repository].
     *
     * - **`stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())`**:
     * Convierte un `Flow` frío en un `StateFlow` caliente.
     * - `viewModelScope`: El ámbito de corrutinas donde se ejecutará la recolección del flujo.
     * Asegura que la recolección se detenga cuando el ViewModel se destruya.
     * - `SharingStarted.WhileSubscribed(5000)`: Define la estrategia de compartición del flujo.
     * El flujo se mantendrá activo mientras haya suscriptores y seguirá emitiendo elementos
     * durante 5000 milisegundos después de que el último suscriptor desaparezca,
     * lo que ayuda a evitar reconexiones innecesarias si la UI se va y vuelve rápidamente.
     * - `emptyList()`: El valor inicial que emitirá el `StateFlow` antes de que se reciban
     * los primeros datos del repositorio.
     */
    val chatItems: StateFlow<List<ChatItemModel>> =
        repository.getAllChats()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
