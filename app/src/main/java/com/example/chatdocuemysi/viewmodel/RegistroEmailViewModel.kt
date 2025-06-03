// src/main/java/com/example/chatdocuemysi/viewmodel/RegistroEmailViewModel.kt
package com.example.chatdocuemysi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.RegistroEmailModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistroEmailViewModel : ViewModel() {
    private val model = RegistroEmailModel()

    // Campos del formulario
    private val _email         = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password      = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _repeatPassword = MutableStateFlow("")
    val repeatPassword: StateFlow<String> = _repeatPassword

    private val _displayName   = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName

    private val _imageUri      = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    fun actualizarEmail(v: String)          { _email.value = v }
    fun actualizarPassword(v: String)       { _password.value = v }
    fun actualizarRepeatPassword(v: String) { _repeatPassword.value = v }
    fun actualizarDisplayName(v: String)    { _displayName.value = v }
    fun actualizarImageUri(uri: Uri)        { _imageUri.value = uri }

    fun registrarUsuario(onResult: (Boolean, String?) -> Unit) {
        val e  = email.value
        val p  = password.value
        val nm = displayName.value
        val uri= imageUri.value
        viewModelScope.launch {
            model.registrarUsuario(e, p, nm, uri) { success, err ->
                onResult(success, err)
            }
        }
    }
}
