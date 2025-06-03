// src/main/java/com/example/chatdocuemysi/navigation/NavigationScreen.kt
package com.example.chatdocuemysi.navigation

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatdocuemysi.R
import com.example.chatdocuemysi.ui.menu.TopBarMenu
import com.example.chatdocuemysi.utils.ChatUtils
import com.example.chatdocuemysi.utils.autenticarCuentaGoogle
import com.example.chatdocuemysi.view.AdminGroupScreen
import com.example.chatdocuemysi.view.ChatListScreen
import com.example.chatdocuemysi.view.ChatScreen
import com.example.chatdocuemysi.view.ContactListScreen
import com.example.chatdocuemysi.view.CreateGroupScreen
import com.example.chatdocuemysi.view.EditarInformacionScreen
import com.example.chatdocuemysi.view.ForgotPasswordScreen
import com.example.chatdocuemysi.view.LoginEmailScreen
import com.example.chatdocuemysi.view.OpcionesLoginScreen
import com.example.chatdocuemysi.view.RegistroEmailScreen
import com.example.chatdocuemysi.viewmodel.EditarInformacionViewModel
import com.example.chatdocuemysi.viewmodel.LoginEmailViewModel
import com.example.chatdocuemysi.viewmodel.RegistroEmailViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationScreen() {
    val navController = rememberNavController()
    val firebaseAuth = FirebaseAuth.getInstance()
    var isUserAuthenticated by remember { mutableStateOf(firebaseAuth.currentUser != null) }

    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val now = auth.currentUser != null
            if (now && !isUserAuthenticated) {
                navController.popBackStack(navController.graph.startDestinationId, false)
            }
            isUserAuthenticated = now
        }
        firebaseAuth.addAuthStateListener(listener)
        onDispose { firebaseAuth.removeAuthStateListener(listener) }
    }

    Scaffold(
        topBar = {
            TopBarMenu(
                navController = navController,
                isUserAuthenticated = isUserAuthenticated
            )
        }
    ) { padding ->
        if (!isUserAuthenticated) {
            AuthNavGraph(
                navController = navController,
                modifier      = Modifier.padding(padding),
                firebaseAuth  = firebaseAuth
            )
        } else {
            MainNavGraph(
                navController = navController,
                myUid         = firebaseAuth.uid!!,
                modifier      = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun AuthNavGraph(
    navController: NavHostController,
    modifier: Modifier,
    firebaseAuth: FirebaseAuth
) {
    NavHost(navController, startDestination = "opcionesLogin", modifier = modifier) {
        composable("opcionesLogin") {
            val context = LocalContext.current
            val googleOptions = remember {
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            }
            val googleClient = remember {
                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, googleOptions)
            }
            val progressDialog = remember { ProgressDialog(context) }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val task = com.google.android.gms.auth.api.signin.GoogleSignIn
                        .getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(
                            com.google.android.gms.common.api.ApiException::class.java
                        )
                        autenticarCuentaGoogle(
                            idToken        = account.idToken,
                            context        = context,
                            firebaseAuth   = firebaseAuth,
                            progressDialog = progressDialog
                        ) {
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error Google SignIn: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Google SignIn cancelado", Toast.LENGTH_SHORT).show()
                }
            }

            OpcionesLoginScreen(
                onEmailLogin  = { navController.navigate("loginEmail") },
                onGoogleLogin = { launcher.launch(googleClient.signInIntent) }
            )
        }

        composable("loginEmail") {
            val vm = remember { LoginEmailViewModel() }
            LoginEmailScreen(navController = navController, viewModel = vm)
        }

        composable("registroEmail") {
            val vm = remember { RegistroEmailViewModel() }
            RegistroEmailScreen(navController = navController, viewModel = vm,onBack= { navController.popBackStack("loginEmail", false)})
        }

        composable("olvidePassword") {
            ForgotPasswordScreen(navController = navController,onSent = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainNavGraph(
    navController: NavHostController,
    myUid: String,
    modifier: Modifier
) {
    NavHost(navController, startDestination = "chatList", modifier = modifier) {

        composable("chatList") {
            ChatListScreen(navController = navController, myUid = myUid)
        }

        composable(
            route = "privateChat/{peerId}/{peerName}",
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("peerName") { type = NavType.StringType }
            )
        ) { backStack ->
            val peerId   = backStack.arguments!!.getString("peerId")!!
            val peerName = backStack.arguments!!.getString("peerName")!!
            val chatId   = ChatUtils.generateChatId(myUid, peerId)
            val parts    = chatId.split("_")
            val chatPath = "MensajesIndividuales/${parts[0]}/${parts[1]}"
            ChatScreen(
                navController  = navController,
                chatPath       = chatPath,
                senderId       = myUid,
                receiverName   = peerName,
                onBack         = { navController.popBackStack("chatList", false) }
            )
        }

        composable(
            route = "groupChat/{groupId}/{groupName}",
            arguments = listOf(
                navArgument("groupId")   { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStack ->
            val groupId   = backStack.arguments!!.getString("groupId")!!
            val groupName = backStack.arguments!!.getString("groupName")!!
            ChatScreen(
                navController  = navController,
                chatPath       = "ChatsGrupales/$groupId",
                senderId       = myUid,
                receiverName   = groupName,
                onBack         = { navController.popBackStack("chatList", false) }
            )
        }

        composable(
            route = "adminGroup/{groupId}/{groupName}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { back ->
            val groupId   = back.arguments!!.getString("groupId")!!
            AdminGroupScreen(
                groupId   = groupId,
                myUid     = myUid,
                onBack    = { navController.popBackStack() }
            )
        }


        composable("newChat") {
            ContactListScreen(navController = navController)
        }

        composable("createGroup") {
            CreateGroupScreen(navController = navController, myUid = myUid)
        }

        composable("editarInformacion") {
            val vm = remember { EditarInformacionViewModel() }
            EditarInformacionScreen(navController = navController, viewModel = vm)
        }
    }
}

