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

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseDatabase.getInstance()
                .getReference("Usuarios/$uid/fcmToken")
                .setValue(token)
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        // 1) Filtramos notificaciones propias
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val senderId = msg.data["senderId"]
        if (senderId != null && senderId == myUid) {
            // Es un mensaje que yo mismo envié: no mostrar notificación
            return
        }

        // 2) Lectura de título y cuerpo (notification o data)
        val notif = msg.notification
        val title = notif?.title
            ?: msg.data["title"]
            ?: "ChatDocu"
        val body  = notif?.body
            ?: msg.data["body"]
            ?: ""

        // 3) Mostrar notificación
        sendNotification(title, body)
    }

    private fun sendNotification(title: String, body: String) {
        val channelId = "chat_messages"
        val nm = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm?.getNotificationChannel(channelId)==null) {
            nm?.createNotificationChannel(NotificationChannel(
                channelId,
                "Mensajes de chat",
                NotificationManager.IMPORTANCE_HIGH
            ))
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_chat_docu_emysi)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pi)
            .build()
        nm?.notify(System.currentTimeMillis().toInt(), notif)
    }
}

