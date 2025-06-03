package com.example.chatdocuemysi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.chatdocuemysi.navigation.NavigationScreen
import com.example.chatdocuemysi.ui.theme.ChatDocuEmysiTheme
import com.example.chatdocuemysi.utils.CleanupWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Ejecución inmediata
        val immediate = OneTimeWorkRequestBuilder<CleanupWorker>().build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "chat_cleanup_now",
                ExistingWorkPolicy.REPLACE,
                immediate
            )

        // 2) Programación cada 1 hora
        val periodic = PeriodicWorkRequestBuilder<CleanupWorker>(
            1, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "chat_cleanup_hourly",
                ExistingPeriodicWorkPolicy.KEEP,
                periodic
            )

        setContent {
            ChatDocuEmysiTheme {
                NavigationScreen()
            }
        }
    }
}








