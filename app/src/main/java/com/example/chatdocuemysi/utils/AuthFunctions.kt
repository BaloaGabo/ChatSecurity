package com.example.chatdocuemysi.utils

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.example.chatdocuemysi.utils.Constantes.obtenerTiempoD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Autentica al usuario con una credencial de Google.
 * Si es un nuevo usuario, guarda su información en la base de datos.
 *
 * @param idToken Token de identificación de Google.
 * @param context Contexto de la aplicación.
 * @param firebaseAuth Instancia de FirebaseAuth.
 * @param progressDialog Diálogo de progreso para mostrar durante la operación.
 * @param onNewUser Callback que se ejecuta cuando el usuario es nuevo o ya existente.
 */
fun autenticarCuentaGoogle(
    idToken: String?,
    context: Context,
    firebaseAuth: FirebaseAuth,
    progressDialog: ProgressDialog,
    onNewUser: () -> Unit
) {
    // Crea una credencial de autenticación de Google.
    val credencial = GoogleAuthProvider.getCredential(idToken, null)
    firebaseAuth.signInWithCredential(credencial)
        .addOnSuccessListener { authResultado ->
            // Guarda el token FCM del usuario.
            guardarFcmToken(firebaseAuth.currentUser?.uid)

            // Verifica si el usuario es nuevo.
            if (authResultado.additionalUserInfo?.isNewUser == true) {
                // Si es un nuevo usuario, actualiza su información.
                actualizarInfoUsuarioGoogle(context, firebaseAuth, progressDialog, onNewUser)
            } else {
                // Si el usuario ya existe, muestra un mensaje y ejecuta el callback.
                Toast.makeText(context, "Inicio de sesión exitoso con Google", Toast.LENGTH_SHORT).show()
                onNewUser()
            }
        }
        .addOnFailureListener { e ->
            // Maneja fallos en la autenticación.
            Toast.makeText(context, "Fallo la autenticación con Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

/**
 * Guarda el token de Firebase Cloud Messaging (FCM) del usuario en la base de datos.
 * Esto es necesario para enviar notificaciones push al dispositivo del usuario.
 *
 * @param uid El UID del usuario.
 */
private fun guardarFcmToken(uid: String?) {
    val userId = uid ?: return
    // Obtiene el token FCM y lo guarda en el nodo del usuario en Firebase Realtime Database.
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            FirebaseDatabase.getInstance()
                .getReference("Usuarios/$userId/fcmToken")
                .setValue(token)
        }
}

/**
 * Actualiza la información del usuario de Google en la base de datos.
 * Se utiliza para registrar nuevos usuarios que inician sesión con Google.
 *
 * @param context Contexto de la aplicación.
 * @param firebaseAuth Instancia de FirebaseAuth.
 * @param progressDialog Diálogo de progreso.
 * @param onSuccess Callback que se ejecuta al guardar la información exitosamente.
 */
fun actualizarInfoUsuarioGoogle(
    context: Context,
    firebaseAuth: FirebaseAuth,
    progressDialog: ProgressDialog,
    onSuccess: () -> Unit
) {
    progressDialog.setMessage("Guardando Información")
    progressDialog.show() // Muestra el diálogo de progreso.

    // Obtiene los datos del usuario actual.
    val uidU = firebaseAuth.uid
    val nombresU = firebaseAuth.currentUser?.displayName ?: ""
    val emailU = firebaseAuth.currentUser?.email ?: ""
    val tiempoR = obtenerTiempoD() // Asume una función para obtener la fecha/hora.

    // Crea un mapa con los datos del usuario a guardar.
    val datosUsuario = hashMapOf(
        "uid" to uidU,
        "nombres" to nombresU,
        "email" to emailU,
        "tiempoR" to tiempoR,
        "proveedor" to "Google", // Indica que el proveedor es Google.
        "estado" to "Online",
        "imagen" to "" // La imagen puede ser actualizada posteriormente.
    )

    val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
    if (uidU != null) {
        // Guarda los datos del usuario en la base de datos bajo su UID.
        reference.child(uidU)
            .setValue(datosUsuario)
            .addOnSuccessListener {
                progressDialog.dismiss() // Cierra el diálogo de progreso.
                Toast.makeText(context, "Información del usuario guardada", Toast.LENGTH_SHORT).show()
                onSuccess() // Ejecuta el callback de éxito.
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss() // Cierra el diálogo de progreso.
                // Muestra un mensaje de error si falla al guardar la información.
                Toast.makeText(context, "Fallo al guardar la información del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    } else {
        progressDialog.dismiss()// Cierra el diálogo de progreso.
        // Muestra un mensaje de error si el UID del usuario es nulo.
        Toast.makeText(context, "Error: UID de usuario es nulo.", Toast.LENGTH_SHORT).show()
    }
}
