package com.example.chatdocuemysi.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.EditarInformacionModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditarInformacionViewModel : ViewModel() {
    private val model = EditarInformacionModel()
    private val _nombres = MutableStateFlow("")
    val nombres: StateFlow<String> = _nombres

    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl

    init {
        cargarInformacion()
    }

    private fun cargarInformacion() {
        viewModelScope.launch {
            model.cargarInformacion { nombres, imageUrl, success, errorMessage ->
                if (success) {
                    _nombres.value = nombres
                    _imageUrl.value = imageUrl
                } else {
                    // TODO: Log the error or handle it in the UI if needed
                    println("Error al cargar información: $errorMessage")
                }
            }
        }
    }

    fun actualizarNombres(nombres: String) {
        _nombres.value = nombres
    }

    fun actualizarImagen(imagenUri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            model.actualizarImagen(imagenUri) { imageUrl, success, errorMessage ->
                _imageUrl.value = imageUrl ?: ""
                onResult(success, errorMessage)
            }
        }
    }

    fun actualizarInfo(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            model.actualizarInfo(nombres.value) { success, errorMessage ->
                onResult(success, errorMessage)
            }
        }
    }
}