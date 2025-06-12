package com.example.chatdocuemysi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.EditarInformacionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * # EditarInformacionViewModel: Gestión del Perfil del Usuario
 *
 * Este ViewModel se encarga de la lógica y el estado de la pantalla de edición de información del usuario.
 * Permite cargar la información actual del usuario, actualizar su nombre y cambiar su imagen de perfil.
 */
class EditarInformacionViewModel : ViewModel() {

    // Instancia del modelo que interactúa con la capa de datos (ej. Firebase).
    private val model = EditarInformacionModel()

    // --- Estados de la UI ---
    // MutableStateFlow para el nombre del usuario.
    private val _nombres = MutableStateFlow("")
    val nombres: StateFlow<String> = _nombres

    // MutableStateFlow para la URL de la imagen de perfil del usuario.
    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl

    /**
     * Bloque de inicialización que se ejecuta cuando el ViewModel es creado.
     * Aquí se inicia la carga de la información del usuario.
     */
    init {
        cargarInformacion()
    }

    /**
     * Carga la información del usuario (nombre e imagen de perfil) desde el modelo.
     * Los datos cargados actualizan los StateFlows correspondientes.
     */
    private fun cargarInformacion() {
        viewModelScope.launch { // Lanza una corrutina en el ámbito del ViewModel.
            model.cargarInformacion { nombres, imageUrl, success, errorMessage ->
                if (success) {
                    _nombres.value = nombres // Actualiza el nombre si la carga fue exitosa.
                    _imageUrl.value = imageUrl // Actualiza la URL de la imagen si la carga fue exitosa.
                } else {
                    // Solo se imprime el mensaje de error en la consola.
                    println("Error al cargar información: $errorMessage")
                }
            }
        }
    }

    /**
     * Actualiza el valor del nombre en el StateFlow local.
     * Esta función es llamada desde la UI cuando el usuario edita el campo de texto del nombre.
     *
     * @param nombres El nuevo nombre introducido por el usuario.
     */
    fun actualizarNombres(nombres: String) {
        _nombres.value = nombres
    }

    /**
     * Actualiza la imagen de perfil del usuario.
     * Esta función maneja la lógica para subir la nueva imagen y actualizar la URL.
     *
     * @param imagenUri La [Uri] local de la nueva imagen seleccionada por el usuario.
     * @param onResult Un callback que se invoca con un [Boolean] indicando el éxito y un [String] opcional para mensajes de error.
     */
    fun actualizarImagen(imagenUri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch { // Lanza una corrutina para la operación asíncrona de actualización de imagen.
            model.actualizarImagen(imagenUri) { imageUrl, success, errorMessage ->
                _imageUrl.value = imageUrl ?: "" // Actualiza la URL de la imagen en el StateFlow.
                onResult(success, errorMessage) // Notifica el resultado de la operación a la UI.
            }
        }
    }

    /**
     * Guarda la información actualizada del usuario (solo el nombre en este caso) en el modelo.
     *
     * @param onResult Un callback que se invoca con un [Boolean] indicando el éxito y un [String] opcional para mensajes de error.
     */
    fun actualizarInfo(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch { // Lanza una corrutina para la operación asíncrona de actualización de información.
            model.actualizarInfo(nombres.value) { success, errorMessage ->
                onResult(success, errorMessage) // Notifica el resultado de la operación a la UI.
            }
        }
    }
}