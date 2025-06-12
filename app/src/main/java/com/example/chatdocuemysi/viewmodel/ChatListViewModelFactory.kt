package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatdocuemysi.repository.ChatListRepository

@Suppress("UNCHECKED_CAST") // Suprime la advertencia de "unchecked cast" ya que la verificación de tipo se realiza explícitamente.
class ChatListViewModelFactory(
    // El repositorio es una dependencia que ChatListViewModel necesita.
    private val repository: ChatListRepository
) : ViewModelProvider.Factory { // Implementa la interfaz ViewModelProvider.Factory.

    /**
     * Este metodo es llamado por el sistema Android para crear una nueva instancia de ViewModel.
     *
     * @param modelClass La clase del ViewModel que se solicita crear.
     * @param <T> El tipo del ViewModel.
     * @return Una nueva instancia del ViewModel solicitado.
     * @throws IllegalArgumentException Si la clase del ViewModel solicitada no es compatible.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica si la clase del ViewModel solicitada es ChatListViewModel.
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            // Si es ChatListViewModel, lo instanciamos pasando el repositorio.
            // Se realiza un 'cast' seguro a T ya que la verificación anterior garantiza la compatibilidad.
            return ChatListViewModel(repository) as T
        } else {
            // Si se solicita una clase de ViewModel diferente, lanzamos una excepción.
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
