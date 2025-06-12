package com.example.chatdocuemysi.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.chatdocuemysi.model.MessageUiModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Función Composable que muestra un único mensaje de chat. La apariencia del mensaje
 * cambia según si fue enviado por el usuario actual (`isMine`) y su tipo (texto o imagen).
 *
 * @param message El [MessageUiModel] que contiene los datos del mensaje a mostrar.
 * @param isMine Un booleano que indica si el mensaje fue enviado por el usuario actual.
 * @param onImageClick Una función lambda que se invoca cuando se hace clic en un mensaje de imagen,
 * pasando la URL de la imagen como un [String].
 */
@Composable
fun MessageCard(
    message: MessageUiModel,
    isMine: Boolean,
    onImageClick: (String) -> Unit
) {
    // Se memoriza una instancia de SimpleDateFormat para un formato consistente de fecha/hora.
    val dateTimeFormat = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }
    // Formatea la marca de tiempo del mensaje en una cadena de fecha y hora legible.
    val dateTime = dateTimeFormat.format(Date(message.timestamp))

    Column(
        // Alinea los mensajes al final (derecha) si son del usuario actual, de lo contrario al inicio (izquierda).
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        // Muestra el nombre del remitente.
        Text(
            text = message.senderName,
            style = MaterialTheme.typography.labelSmall
        )

        // La burbuja del mensaje en sí, con estilo basado en si es un mensaje del usuario actual.
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                when {
                    // 1) Maneja los mensajes de imagen normales:
                    // Si el tipo de mensaje es "image" y hay una URL de imagen válida,
                    // muestra la imagen.
                    message.type == "image" && !message.imageUrl.isNullOrBlank() -> {
                        Image(
                            painter = rememberAsyncImagePainter(message.imageUrl),
                            contentDescription = null, // Se puede añadir una descripción de contenido para accesibilidad.
                            modifier = Modifier
                                .fillMaxWidth(0.6f) // La imagen ocupa el 60% del ancho disponible.
                                .height(180.dp) // Altura fija para la imagen.
                                .clickable { onImageClick(message.imageUrl) }, // Hace que la imagen sea clickeable.
                            contentScale = ContentScale.Crop // Recorta la imagen para llenar sus límites.
                        )
                    }
                    // 2) Maneja los marcadores de posición de imagen caducada:
                    // Si el tipo de mensaje es "text" y el texto indica explícitamente "Imagen expirada",
                    // muestra un texto de marcador de posición.
                    message.type == "text" && message.text == "🖼️ Imagen expirada" -> {
                        Text(
                            text = "🖼️ Imagen expirada",
                            style = MaterialTheme.typography.bodyMedium // Usa la tipografía apropiada.
                        )
                    }
                    // 3) Maneja los mensajes de texto normales:
                    // Si el texto del mensaje no está en blanco, muestra el contenido del texto.
                    message.text.isNotBlank() -> {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(top = 4.dp),
                            // Establece el color del texto según si es un mensaje del usuario actual.
                            color = if (isMine)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Muestra la marca de tiempo del mensaje.
        Text(
            text = dateTime,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

