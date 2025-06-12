package com.example.chatdocuemysi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.RegistroEmailModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistroEmailViewModel : ViewModel() {
    // Instancia del modelo que contiene la lógica de negocio para el registro.
    private val model = RegistroEmailModel()

    // --- Campos del Formulario de Registro ---

    // `MutableStateFlow` para el campo de correo electrónico.
    // `StateFlow` expone el valor a la UI de forma reactiva y es inmutable desde fuera.
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    // `MutableStateFlow` para el campo de contraseña.
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    // `MutableStateFlow` para el campo de repetición de contraseña.
    private val _repeatPassword = MutableStateFlow("")
    val repeatPassword: StateFlow<String> = _repeatPassword

    // `MutableStateFlow` para el campo del nombre a mostrar del usuario.
    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName

    // `MutableStateFlow` para la URI de la imagen de perfil seleccionada (puede ser nula).
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    // --- Funciones para Actualizar el Estado de los Campos ---

    /**
     * Actualiza el valor del correo electrónico.
     * @param v El nuevo valor del correo electrónico.
     */
    fun actualizarEmail(v: String) {
        _email.value = v
    }

    /**
     * Actualiza el valor de la contraseña.
     * @param v El nuevo valor de la contraseña.
     */
    fun actualizarPassword(v: String) {
        _password.value = v
    }

    /**
     * Actualiza el valor de la repetición de contraseña.
     * @param v El nuevo valor de la repetición de contraseña.
     */
    fun actualizarRepeatPassword(v: String) {
        _repeatPassword.value = v
    }

    /**
     * Actualiza el valor del nombre a mostrar.
     * @param v El nuevo valor del nombre a mostrar.
     */
    fun actualizarDisplayName(v: String) {
        _displayName.value = v
    }

    /**
     * Actualiza la URI de la imagen de perfil.
     * @param uri La nueva URI de la imagen.
     */
    fun actualizarImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    /**
     * Inicia el proceso de registro de un nuevo usuario.
     *
     * Esta función recoge los valores actuales de los `StateFlow` y llama al metodo `registrarUsuario`
     * del modelo. La operación se lanza en el `viewModelScope` porque es asíncrona y podría implicar
     * llamadas a la red (ej. Firebase).
     *
     * @param onResult Un callback que se invoca con un [Boolean] indicando si el registro fue exitoso,
     * y un [String] opcional con un mensaje de error en caso de fallo.
     */
    fun registrarUsuario(onResult: (Boolean, String?) -> Unit) {
        val e = email.value            // Obtenemos el email actual.
        val p = password.value         // Obtenemos la contraseña actual.
        val nm = displayName.value     // Obtenemos el nombre a mostrar actual.
        val uri = imageUri.value       // Obtenemos la URI de la imagen actual.

        viewModelScope.launch { // Lanza una corrutina en el ámbito del ViewModel.
            // Llama al modelo para intentar registrar al usuario.
            model.registrarUsuario(e, p, nm, uri) { success, err ->
                // Pasa el resultado (éxito/fallo y mensaje de error) al callback de la UI.
                onResult(success, err)
            }
        }
    }
}
