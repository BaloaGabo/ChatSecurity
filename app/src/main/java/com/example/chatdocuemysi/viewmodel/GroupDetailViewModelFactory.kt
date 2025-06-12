package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatdocuemysi.repository.ChatGroupRepository

class GroupDetailViewModelFactory(
    // El ID del grupo para el que se cargarán los detalles.
    private val groupId: String,
    // El UID del usuario actual que está viendo los detalles del grupo.
    private val myUid: String
) : ViewModelProvider.Factory { // Implementa la interfaz de fábrica de ViewModels.

    /**
     * Crea y devuelve una nueva instancia del ViewModel solicitado.
     *
     * @param c La clase del ViewModel que se solicita crear.
     * @param <T> El tipo genérico del ViewModel.
     * @return Una nueva instancia del ViewModel.
     * @throws IllegalArgumentException Si la clase del ViewModel solicitada no es compatible.
     */
    override fun <T : ViewModel> create(c: Class<T>): T {
        // Verifica si la clase del ViewModel solicitada es GroupDetailViewModel.
        if (c.isAssignableFrom(GroupDetailViewModel::class.java)) {
            // Si es así, crea una nueva instancia de GroupDetailViewModel.
            // Se le pasa una nueva instancia de ChatGroupRepository y los parámetros `groupId` y `myUid`.
            @Suppress("UNCHECKED_CAST") // Suprime la advertencia de 'unchecked cast' ya que la verificación de tipo se ha realizado.
            return GroupDetailViewModel(
                repo    = ChatGroupRepository(), // Se crea una nueva instancia del repositorio.
                groupId = groupId, // Se pasa el ID del grupo.
                myUid   = myUid    // Se pasa el UID del usuario actual.
            ) as T
        }
        // Si la clase del ViewModel solicitada no es GroupDetailViewModel, lanza una excepción.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

