package com.example.rogaltasksapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun Ustawienia(nav: NavHostController, viewModel : TaskViewModel)
{
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        Modifier.fillMaxWidth(),
        bottomBar={DolnePrzyciski(nav, viewModel)},
        topBar = {InternetBar(uiState.internet)}
    )
    { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text("Ustawienia",  fontSize = 32.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick={
                viewModel.logout()

            })
            {Text("Wyloguj się")}
            Spacer(Modifier.height(24.dp))
            Card(                modifier = Modifier.fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF181818)
                ),)
            {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,)
                {
                    if (uiState.internet)
                    {
                        var userName by remember {mutableStateOf(viewModel.uiState.value.userName)}
                        var showPassword by remember {mutableStateOf(false)}
                        var password by remember {mutableStateOf("")}
                        var notifications by remember {mutableStateOf(viewModel.uiState.value.ilePowiadomien.toString())}
                        Text("Zaawansowane",  fontSize = 24.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp))
                        {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = userName,
                                label = {Text("Nazwa użytkownika")},
                                onValueChange = {userName=it},
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = Color(0xffeaeaea)
                                )
                            )
                            Spacer(Modifier.width(24.dp))
                            Button(onClick={
                                viewModel.changeUserData(UserPOST("login", userName))

                            })
                            {Text("Zmień")}
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp))
                        {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                label = {Text("Hasło")},
                                value = password,
                                onValueChange = {password=it;},
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = Color(0xffeaeaea)
                                ),
                                visualTransformation = if (showPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Filled.Visibility
                                            else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                            )

                            Spacer(Modifier.width(24.dp))
                            Button(onClick={
                                viewModel.changeUserData(UserPOST("haslo", password))

                            })
                            {Text("Zmień")}
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp))
                        {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = notifications,
                                label = {Text("Ilość powiadomień")},
                                onValueChange = {

                                    notifications=it
                                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = Color(0xffeaeaea)
                                ),
                                isError = notifications.isBlank() || notifications.toIntOrNull() == null
                            )
                            Spacer(Modifier.width(24.dp))
                            Button(onClick={
                                viewModel.changeUserData(UserPOST("ilePowiadomien", notifications))

                            })
                            {Text("Zmień")}
                        }
                    }
                    else
                    {
                        Text("Reszta ustawień jest dostępna tylko w trybie online",  fontSize = 24.sp)

                    }
                }
            }

        }
    }
}