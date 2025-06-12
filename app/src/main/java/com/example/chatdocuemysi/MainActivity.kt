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
    /**
     * Se llama cuando la actividad es creada por primera vez.
     * Aquí se realiza la configuración inicial de la aplicación.
     *
     * @param savedInstanceState Si la actividad se está recreando después de un cambio de configuración
     * o porque su proceso fue terminado, este Bundle contiene los datos que suministró más recientemente
     * en [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Configuración de WorkManager para Tareas de Limpieza ---

        // WorkManager es una API de Android Jetpack que facilita la programación de tareas
        // diferibles y garantizadas que deben ejecutarse. Es adecuado para tareas que no necesitan
        // ejecutarse inmediatamente y que se deben ejecutar incluso si la aplicación se cierra
        // o el dispositivo se reinicia.

        // 1) Ejecución Inmediata de Tarea de Limpieza:
        // Se crea una solicitud de trabajo de una sola vez para CleanupWorker.
        val immediateCleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>().build()

        // Se encola esta solicitud de trabajo.
        // "chat_cleanup_now" es un nombre único para esta tarea, asegurando que si se encola de nuevo,
        // la política `ExistingWorkPolicy.REPLACE` reemplazará cualquier tarea pendiente con el mismo nombre.
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "chat_cleanup_now",             // Nombre único para esta tarea.
                ExistingWorkPolicy.REPLACE,     // Si ya existe una tarea con este nombre, la reemplaza.
                immediateCleanupRequest         // La solicitud de trabajo a encolar.
            )

        // 2) Programación Periódica de Tarea de Limpieza (cada 1 hora):
        // Se crea una solicitud de trabajo periódica para CleanupWorker.
        // La tarea se ejecutará cada 1 hora.
        val periodicCleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            1, TimeUnit.HOURS // Intervalo de repetición: 1 hora.
        ).build()

        // Se encola esta solicitud de trabajo periódica.
        // "chat_cleanup_hourly" es un nombre único para la tarea periódica.
        // `ExistingPeriodicWorkPolicy.KEEP` asegura que si la tarea ya está encolada,
        // la nueva solicitud no la reemplace, manteniendo la existente.
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "chat_cleanup_hourly",           // Nombre único para esta tarea periódica.
                ExistingPeriodicWorkPolicy.KEEP, // Si ya existe una tarea periódica con este nombre, la mantiene.
                periodicCleanupRequest           // La solicitud de trabajo periódica a encolar.
            )

        // --- Configuración de la Interfaz de Usuario con Jetpack Compose ---

        // `setContent` es una función de extensión de `ComponentActivity` que permite
        // definir el diseño de la UI utilizando funciones Composable de Jetpack Compose.
        setContent {
            // Aplica el tema de la aplicación definido en `ui.theme`.
            ChatDocuEmysiTheme {
                // Navegación principal de la aplicación, que contiene la estructura de pantallas.
                NavigationScreen()
            }
        }
    }
}








