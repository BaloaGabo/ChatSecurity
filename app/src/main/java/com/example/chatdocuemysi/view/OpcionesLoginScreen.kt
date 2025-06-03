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

@Composable
fun OpcionesLoginScreen(onEmailLogin: () -> Unit, onGoogleLogin: () -> Unit) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.icon_banner), contentDescription = "Chat Icon", modifier = Modifier.padding(16.dp).width(350.dp))
        Button(onClick = onEmailLogin, modifier = Modifier.padding(top = 35.dp).width(250.dp)) {
            Text(stringResource(R.string.opcionEmail))
        }
        Button(onClick = onGoogleLogin, modifier = Modifier.width(250.dp)) {
            Text(stringResource(R.string.opcionGoogle))
        }
    }
}