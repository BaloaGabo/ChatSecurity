package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.LoginEmailModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginEmailViewModel : ViewModel() {
    private val model = LoginEmailModel()
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun actualizarEmail(email: String) {
        _email.value = email
    }

    fun actualizarPassword(password: String) {
        _password.value = password
    }

    fun iniciarSesion(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            model.iniciarSesion(email.value, password.value) { success, errorMessage ->
                onResult(success, errorMessage)
            }
        }
    }
}