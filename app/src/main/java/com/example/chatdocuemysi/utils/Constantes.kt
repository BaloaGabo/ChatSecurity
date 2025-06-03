package com.example.chatdocuemysi.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Constantes {
    fun obtenerTiempoD(): String {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }
}