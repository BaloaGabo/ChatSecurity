package com.example.chatdocuemysi.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.chatdocuemysi.MainActivity
import com.example.chatdocuemysi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio de Firebase Messaging para manejar notificaciones push.
 * Extiende [FirebaseMessagingService] para recibir y procesar mensajes.
 */
class MyFirebaseMessagingService: FirebaseMessagingService() {

    /**
     * Se llama cuando se genera un nuevo token de registro de dispositivo.
     * Actualiza el token FCM del usuario actual en la base de datos de Firebase.
     * @param token El nuevo token de registro FCM.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Obtiene el UID del usuario actual y guarda el nuevo token FCM.
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseDatabase.getInstance()
                .getReference("Usuarios/$uid/fcmToken")
                .setValue(token)
        }
    }

    /**
     * Se llama cuando se recibe un mensaje de Firebase.
     * Filtra las notificaciones enviadas por el propio usuario y muestra las demás.
     * @param msg El mensaje remoto recibido.
     */
    override fun onMessageReceived(msg: RemoteMessage) {
        // 1) Filtra notificaciones enviadas por el propio usuario para evitar auto-notificaciones.
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val senderId = msg.data["senderId"]
        if (senderId != null && senderId == myUid) {
            // Si la notificación es del propio usuario, no se muestra.
            return
        }

        // 2) Extrae el título y el cuerpo del mensaje de la notificación o de los datos.
        val notif = msg.notification
        val title = notif?.title
            ?: msg.data["title"]
            ?: "ChatDocu" // Título predeterminado si no se encuentra.
        val body  = notif?.body
            ?: msg.data["body"]
            ?: "" // Cuerpo predeterminado vacío si no se encuentra.

        // 3) Muestra la notificación al usuario.
        sendNotification(title, body)
    }

    /**
     * Muestra una notificación al usuario con el título y cuerpo proporcionados.
     * Configura el canal de notificación (para Android 8.0+), el intent y el sonido.
     * @param title El título de la notificación.
     * @param body El cuerpo (contenido) de la notificación.
     */
    private fun sendNotification(title: String, body: String) {
        val channelId = "chat_messages" // ID del canal de notificación.
        val nm = getSystemService(NotificationManager::class.java) // Administrador de notificaciones.

        // Crea el canal de notificación para Android 8.0 (API 26) y versiones posteriores.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm?.getNotificationChannel(channelId)==null) {
            nm?.createNotificationChannel(NotificationChannel(
                channelId,
                "Mensajes de chat", // Nombre visible del canal.
                NotificationManager.IMPORTANCE_HIGH // Importancia alta para notificaciones de chat.
            ))
        }

        // Define el intent que se activará al tocar la notificación (abre MainActivity).
        val intent = Intent(this, MainActivity::class.java).apply {
            // Limpia la pila de actividades y asegura que solo haya una instancia de MainActivity.
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        // Crea un PendingIntent para el intent.
        val pi = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE es requerido en API 23+.
        )

        // Construye la notificación.
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_chat_docu_emysi) // Icono pequeño de la notificación.
            .setContentTitle(title) // Título de la notificación.
            .setContentText(body) // Texto principal de la notificación.
            .setAutoCancel(true) // Cierra la notificación al tocarla.
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Sonido predeterminado.
            .setContentIntent(pi) // Asigna el PendingIntent.
            .build()
        // Muestra la notificación con un ID único basado en el tiempo actual.
        nm?.notify(System.currentTimeMillis().toInt(), notif)
    }
}

