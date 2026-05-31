package com.example.rogaltasksapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LosujScreen(nav: NavHostController, viewModel : TaskViewModel)
{
    var names = listOf("Do końca dnia", "Do końca tygodnia")
    var selectExpanded by remember {mutableStateOf(false)}
    var selectedName by remember {mutableStateOf(names[0])}
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        Modifier.fillMaxWidth(),
        bottomBar={DolnePrzyciski(nav)},
        topBar = {InternetBar(uiState.internet)}
    )
    {
        paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("Wylosuj zadanie", fontSize= 22.sp)
            Spacer(Modifier.height(16.dp))
            ExposedDropdownMenuBox(expanded = selectExpanded, onExpandedChange = {selectExpanded = !selectExpanded})
            {
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Czas na wykonanie") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectExpanded)
                    },
                    modifier = Modifier.menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = Color(0xffeaeaea),
                    ),
                )
                ExposedDropdownMenu(
                    expanded = selectExpanded,
                    onDismissRequest = { selectExpanded = false }
                )
                {
                    names.forEach {
                            item -> DropdownMenuItem(text = { Text(item) }, onClick = {selectedName = item; selectExpanded=false})
                        }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {})
            {
                Text("Losuj")
            }
        }


    }


}
