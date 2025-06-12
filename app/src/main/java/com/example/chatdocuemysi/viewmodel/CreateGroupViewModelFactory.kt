package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * # CreateGroupViewModelFactory: Fábrica para Crear `CreateGroupViewModel`
 *
 * Esta clase es una fábrica personalizada que permite instanciar [CreateGroupViewModel].
 * Es necesaria porque [CreateGroupViewModel] requiere el `myUid` (UID del usuario actual)
 * en su constructor, y el sistema de Android no puede proporcionar esto directamente.
 *
 * Con esta fábrica, puedes crear instancias de [CreateGroupViewModel]
 * pasando el `myUid` necesario.
 *
 * @param myUid El UID del usuario actual que está creando el grupo.
 */
class CreateGroupViewModelFactory(private val myUid: String): ViewModelProvider.Factory {

    /**
     * Crea una nueva instancia de [ViewModel] de la clase especificada.
     *
     * @param c La clase del [ViewModel] que se desea crear.
     * @param <T> El tipo del [ViewModel].
     * @return Una nueva instancia del [ViewModel].
     * @throws IllegalArgumentException Si la clase del [ViewModel] no es [CreateGroupViewModel].
     */
    override fun <T : ViewModel> create(c: Class<T>): T {
        // Verifica si la clase solicitada es CreateGroupViewModel.
        if (c.isAssignableFrom(CreateGroupViewModel::class.java)) {
            // Si lo es, crea una instancia de CreateGroupViewModel pasándole el 'myUid'.
            // Se suprime la advertencia de 'unchecked cast' porque sabemos que el tipo es correcto
            // debido a la comprobación de 'isAssignableFrom'.
            @Suppress("UNCHECKED_CAST")
            return CreateGroupViewModel(myUid) as T
        }
        // Si la clase solicitada no es CreateGroupViewModel, lanza una excepción.
        throw IllegalArgumentException("Unknown VM")
    }
}
