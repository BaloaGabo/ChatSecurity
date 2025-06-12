package com.example.chatdocuemysi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatdocuemysi.model.GroupDetail
import com.example.chatdocuemysi.repository.ChatGroupRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * # GroupDetailViewModel: Gestión de los Detalles de un Grupo
 *
 * Este ViewModel es el responsable de gestionar y proporcionar los datos y las acciones
 * relacionadas con los detalles de un grupo de chat específico. Permite obtener la información
 * del grupo, verificar si el usuario actual es administrador y realizar operaciones de gestión
 * de miembros y administradores del grupo.
 *
 * @param repo El [ChatGroupRepository] que proporciona acceso a los datos de los grupos de chat.
 * @param groupId El ID único del grupo del que se quieren obtener los detalles.
 * @param myUid El ID único del usuario actual que está viendo los detalles del grupo.
 */
class GroupDetailViewModel(
    private val repo: ChatGroupRepository,
    private val groupId: String,
    private val myUid: String
): ViewModel() {

    /**
     * Flujo que contiene los detalles completos del grupo ([GroupDetail]).
     * Se recolecta del repositorio y se convierte en un [StateFlow] para ser observado por la UI.
     *
     * - **`stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)`**:
     * Convierte el `Flow` de `getGroupDetail` en un `StateFlow`.
     * - `viewModelScope`: El ámbito de corrutinas en el que el flujo se mantendrá activo.
     * - `SharingStarted.WhileSubscribed(5_000)`: La estrategia de compartición. El flujo se mantiene
     * activo mientras haya suscriptores y durante 5 segundos adicionales después de que el
     * último suscriptor desaparezca (para optimizar reconexiones rápidas).
     * - `GroupDetail(...)`: El valor inicial del `StateFlow` antes de que se carguen los datos reales.
     */
    val detail: StateFlow<GroupDetail> =
        repo.getGroupDetail(groupId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
                GroupDetail(groupId,"","", emptyList(), emptyList()))

    /**
     * Flujo que indica si el usuario actual (`myUid`) es un administrador del grupo.
     * Se deriva del flujo `detail` y se actualiza automáticamente si los detalles del grupo cambian.
     *
     * - **`.map { myUid in it.admins }`**: Transforma el `GroupDetail` en un `Boolean`
     * verificando si `myUid` está presente en la lista de IDs de administradores del grupo.
     * - **`stateIn(...)`**: Convierte el `Flow` resultante en un `StateFlow` con la misma
     * estrategia de compartición.
     */
    val isAdmin: StateFlow<Boolean> = detail
        .map { myUid in it.admins }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * Promueve a un miembro a administrador del grupo.
     * Esta operación se lanza en un [viewModelScope] ya que puede ser asíncrona.
     *
     * @param uid El ID del usuario a promover.
     */
    fun promote(uid: String) = viewModelScope.launch {
        repo.promoteToAdmin(groupId, uid)
    }

    /**
     * Degrada a un administrador a miembro del grupo (elimina su rol de administrador).
     * Esta operación se lanza en un [viewModelScope] ya que puede ser asíncrona.
     *
     * @param uid El ID del usuario a degradar.
     */
    fun demote(uid: String) = viewModelScope.launch {
        repo.demoteAdmin(groupId, uid)
    }

    /**
     * Añade un nuevo miembro al grupo.
     * Esta operación se lanza en un [viewModelScope] ya que puede ser asíncrona.
     *
     * @param uid El ID del usuario a añadir.
     */
    fun addMember(uid: String) = viewModelScope.launch {
        repo.addMember(groupId, uid)
    }

    /**
     * Elimina a un miembro del grupo.
     * Esta operación se lanza en un [viewModelScope] ya que puede ser asíncrona.
     *
     * @param uid El ID del usuario a eliminar.
     */
    fun removeMember(uid: String) = viewModelScope.launch {
        repo.removeMember(groupId, uid)
    }
}
