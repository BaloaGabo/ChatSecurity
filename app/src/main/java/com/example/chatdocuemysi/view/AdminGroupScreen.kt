package com.example.chatdocuemysi.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatdocuemysi.viewmodel.GroupDetailViewModel
import com.example.chatdocuemysi.viewmodel.GroupDetailViewModelFactory
import com.example.chatdocuemysi.viewmodel.UserListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGroupScreen(
    groupId: String,
    myUid: String,
    onBack: () -> Unit
) {
    val userListVm: UserListViewModel = viewModel()
    val allUsers by userListVm.users.collectAsState()

    val vm: GroupDetailViewModel = viewModel(
        factory = GroupDetailViewModelFactory(groupId, myUid)
    )
    val detail by vm.detail.collectAsState()
    val isAdmin by vm.isAdmin.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                title = { Text(detail.groupName) }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Text("Miembros", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(detail.members) { uid ->
                    val user = allUsers.find { it.uid == uid }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(user?.nombres ?: uid)
                        if (isAdmin) {
                            Row {
                                val isThisAdmin = uid in detail.admins
                                Text(
                                    text = if (isThisAdmin) "🔰 Admin" else "⭐ Hacer admin",
                                    modifier = Modifier
                                        .clickable {
                                            if (isThisAdmin) vm.demote(uid) else vm.promote(uid)
                                        }
                                        .padding(end = 16.dp)
                                )
                                Text(
                                    text = "❌",
                                    modifier = Modifier.clickable { vm.removeMember(uid) }
                                )
                            }
                        }
                    }
                }
            }
            if (isAdmin) {
                Divider()
                Text("Añadir nuevo miembro", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
                LazyColumn(Modifier.weight(1f)) {
                    items(allUsers.filter { it.uid !in detail.members }) { user ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { vm.addMember(user.uid) }
                                .padding(8.dp)
                        ) {
                            Text(user.nombres)
                        }
                    }
                }
            }
        }
    }
}
