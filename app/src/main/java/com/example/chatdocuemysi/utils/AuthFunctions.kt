package com.example.chatdocuemysi.utils

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.example.chatdocuemysi.utils.Constantes.obtenerTiempoD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

fun autenticarCuentaGoogle(
    idToken: String?,
    context: Context,
    firebaseAuth: FirebaseAuth,
    progressDialog: ProgressDialog,
    onNewUser: () -> Unit
) {
    val credencial = GoogleAuthProvider.getCredential(idToken, null)
    firebaseAuth.signInWithCredential(credencial)
        .addOnSuccessListener { authResultado ->
            // ① Guardamos el token FCM
            guardarFcmToken(firebaseAuth.currentUser?.uid)

            if (authResultado.additionalUserInfo?.isNewUser == true) {
                actualizarInfoUsuarioGoogle(context, firebaseAuth, progressDialog, onNewUser)
            } else {
                Toast.makeText(context, "Inicio de sesión exitoso con Google", Toast.LENGTH_SHORT).show()
                onNewUser()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Fallo la autenticación con Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

private fun guardarFcmToken(uid: String?) {
    val userId = uid ?: return
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            FirebaseDatabase.getInstance()
                .getReference("Usuarios/$userId/fcmToken")
                .setValue(token)
        }
}

fun actualizarInfoUsuarioGoogle(
    context: Context,
    firebaseAuth: FirebaseAuth,
    progressDialog: ProgressDialog,
    onSuccess: () -> Unit
) {
    progressDialog.setMessage("Guardando Información")
    progressDialog.show()
    val uidU = firebaseAuth.uid
    val nombresU = firebaseAuth.currentUser?.displayName ?: ""
    val emailU = firebaseAuth.currentUser?.email ?: ""
    val tiempoR = obtenerTiempoD()

    val datosUsuario = hashMapOf(
        "uid" to uidU,
        "nombres" to nombresU,
        "email" to emailU,
        "tiempoR" to tiempoR,
        "proveedor" to "Google",
        "estado" to "Online",
        "imagen" to ""
    )

    val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
    if (uidU != null) {
        reference.child(uidU)
            .setValue(datosUsuario)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(context, "Información del usuario guardada", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, "Fallo al guardar la información del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    } else {
        progressDialog.dismiss()
        Toast.makeText(context, "Error: UID de usuario es nulo.", Toast.LENGTH_SHORT).show()
    }
}
