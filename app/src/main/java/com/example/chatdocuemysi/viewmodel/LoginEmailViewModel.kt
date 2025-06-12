package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.LoginEmailModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginEmailViewModel : ViewModel() {
    // Instancia del modelo que maneja la lógica de autenticación con Firebase.
    private val model = LoginEmailModel()

    // --- Estados de la UI ---

    // `MutableStateFlow` para almacenar el valor del correo electrónico.
    // `StateFlow` expone el valor a la UI de forma reactiva.
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    // `MutableStateFlow` para almacenar el valor de la contraseña.
    // `StateFlow` expone el valor a la UI de forma reactiva.
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    // --- Funciones para Actualizar el Estado ---

    /**
     * Actualiza el valor del correo electrónico en el `StateFlow`.
     * Esta función es llamada desde la UI cuando el usuario escribe en el campo de correo.
     * @param email El nuevo valor del correo electrónico.
     */
    fun actualizarEmail(email: String) {
        _email.value = email
    }

    /**
     * Actualiza el valor de la contraseña en el `StateFlow`.
     * Esta función es llamada desde la UI cuando el usuario escribe en el campo de contraseña.
     * @param password El nuevo valor de la contraseña.
     */
    fun actualizarPassword(password: String) {
        _password.value = password
    }

    /**
     * Inicia el proceso de inicio de sesión utilizando el correo y la contraseña actuales.
     *
     * Esta operación se lanza en el `viewModelScope` porque es asíncrona
     * (interactúa con el modelo, que a su vez interactúa con Firebase).
     *
     * @param onResult Un callback que se invoca con un [Boolean] indicando si el inicio de sesión fue exitoso,
     * y un [String] opcional con un mensaje de error en caso de fallo.
     */
    fun iniciarSesion(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch { // Lanza una corrutina en el ámbito del ViewModel.
            // Llama al modelo para intentar iniciar sesión con los valores actuales de correo y contraseña.
            model.iniciarSesion(email.value, password.value) { success, errorMessage ->
                // Pasa el resultado (éxito/fallo y mensaje de error) al callback de la UI.
                onResult(success, errorMessage)
            }
        }
    }
}