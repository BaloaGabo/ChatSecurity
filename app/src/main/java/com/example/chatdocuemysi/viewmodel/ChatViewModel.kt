package com.example.chatdocuemysi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.MessageUiModel
import com.example.chatdocuemysi.repository.MessagesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    // Inyectamos el MessagesRepository para interactuar con los datos de los mensajes.
    private val messagesRepo: MessagesRepository = MessagesRepository(),
    // Inyectamos UserListViewModel para obtener información de los usuarios (nombres, imágenes de perfil).
    userListVm: UserListViewModel = UserListViewModel()
) : ViewModel() {

    // Flujo que contiene un mapa de UIDs de usuario a objetos User, utilizado para enriquecer los mensajes.
    private val userMapFlow = userListVm.userMap

    /**
     * Proporciona un [StateFlow] de mensajes adaptados para la interfaz de usuario ([MessageUiModel]).
     * Este flujo combina los mensajes crudos del repositorio con la información detallada de los usuarios.
     *
     * @param chatPath La ruta específica del chat (ej. "MensajesIndividuales/uid1/uid2" o "ChatsGrupales/groupId").
     * @return Un [StateFlow] que emite una lista de [MessageUiModel] lista para ser mostrada en la UI.
     */
    fun getMessagesUiFlow(chatPath: String): StateFlow<List<MessageUiModel>> =
        // Utilizamos 'combine' para fusionar dos flujos: los mensajes del chat y el mapa de usuarios.
        combine(messagesRepo.getMessages(chatPath), userMapFlow) { msgs, users ->
            // Mapeamos cada MessageDataModel (md) a un MessageUiModel.
            msgs.map { md ->
                // Buscamos el usuario asociado al senderId del mensaje.
                val u = users[md.senderId]
                MessageUiModel(
                    id = md.id,
                    text = md.text,
                    imageUrl = md.imageUrl,
                    type = md.type,
                    timestamp = md.timestamp,
                    senderId = md.senderId,
                    senderName = u?.nombres.orEmpty(), // Obtenemos el nombre del remitente o cadena vacía si no se encuentra.
                    senderImageUrl = u?.imagen.orEmpty() // Obtenemos la URL de la imagen del remitente o cadena vacía.
                )
            }
        }.stateIn( // Convertimos el Flow resultante en un StateFlow.
            scope = viewModelScope, // El ámbito de corrutinas donde se mantendrá activo el StateFlow.
            started = SharingStarted.WhileSubscribed(5000), // La estrategia de compartición (activo mientras haya suscriptores + 5 segundos de gracia).
            initialValue = emptyList() // El valor inicial antes de que se emitan los primeros datos.
        )

    /**
     * Envía un mensaje de texto al chat especificado.
     *
     * @param chatPath La ruta del chat donde se enviará el mensaje.
     * @param senderId El ID del remitente del mensaje.
     * @param text El contenido de texto del mensaje.
     */
    fun sendText(chatPath: String, senderId: String, text: String) =
        messagesRepo.sendTextMessage(chatPath, senderId, text)

    /**
     * Envía un mensaje de imagen al chat especificado.
     * La operación de envío de imagen puede ser asíncrona y llevar tiempo.
     *
     * @param chatPath La ruta del chat donde se enviará la imagen.
     * @param senderId El ID del remitente del mensaje.
     * @param uri La [Uri] local de la imagen a enviar.
     */
    fun sendImage(chatPath: String, senderId: String, uri: Uri) {
        viewModelScope.launch {
            // Lanzamos una corrutina en el viewModelScope para manejar la operación de envío de imagen.
            messagesRepo.sendImageMessage(chatPath, senderId, uri)
        }
    }
}



