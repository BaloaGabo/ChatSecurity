package com.example.chatdocuemysi.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Objeto que contiene constantes y funciones de utilidad para la aplicación.
 */
object Constantes {
    /**
     * Obtiene la fecha y hora actual formateada como una cadena de texto.
     * El formato es "yyyy-MM-dd HH:mm:ss".
     *
     * @return Un [String] con la fecha y hora actual formateada.
     */
    fun obtenerTiempoD(): String {
        val calendar = Calendar.getInstance() // Obtiene una instancia del calendario.
        // Define el formato de fecha y hora.
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return simpleDateFormat.format(calendar.time) // Formatea y retorna la fecha y hora.
    }
}