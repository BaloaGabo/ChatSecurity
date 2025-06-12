package com.example.chatdocuemysi.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chatdocuemysi.R

/**
 * ## Pantalla de Opciones de Inicio de Sesión
 *
 * Esta función Composable presenta al usuario las opciones para iniciar sesión en la aplicación.
 * Los usuarios pueden elegir iniciar sesión usando su correo electrónico y contraseña o con su cuenta de Google.
 *
 * @param onEmailLogin Una función lambda que se invoca cuando se hace clic en el botón "Iniciar sesión con correo electrónico".
 * @param onGoogleLogin Una función lambda que se invoca cuando se hace clic en el botón "Iniciar sesión con Google".
 */
@Composable
fun OpcionesLoginScreen(onEmailLogin: () -> Unit, onGoogleLogin: () -> Unit) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center, // Centra los elementos verticalmente en la columna.
        horizontalAlignment = Alignment.CenterHorizontally // Centra los elementos horizontalmente en la columna.
    ) {
        // Muestra el icono o banner de la aplicación.
        Image(
            painter = painterResource(id = R.drawable.icon_banner), // Carga la imagen desde los recursos.
            contentDescription = "Icono de Chat", // Descripción para accesibilidad.
            modifier = Modifier
                .padding(16.dp) // Añade un padding alrededor de la imagen.
                .width(350.dp) // Establece un ancho fijo para la imagen.
        )

        // Botón para iniciar sesión con correo electrónico y contraseña.
        Button(
            onClick = onEmailLogin, // Define la acción al hacer clic.
            modifier = Modifier
                .padding(top = 35.dp) // Añade un espacio superior desde la imagen.
                .width(250.dp)       // Establece un ancho fijo para el botón.
        ) {
            Text(stringResource(R.string.opcionEmail)) // Muestra el texto del botón desde los recursos de cadena.
        }

        // Botón para iniciar sesión con Google.
        Button(
            onClick = onGoogleLogin, // Define la acción al hacer clic.
            modifier = Modifier.width(250.dp) // Establece un ancho fijo para el botón.
        ) {
            Text(stringResource(R.string.opcionGoogle)) // Muestra el texto del botón desde los recursos de cadena.
        }
    }
}