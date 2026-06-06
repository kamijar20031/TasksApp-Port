package com.example.rogaltasksapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.rogaltasksapp.ui.theme.ErrorCol
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LosujScreen(nav: NavHostController, viewModel : TaskViewModel)
{
    val names = listOf("Do końca dnia", "Do końca tygodnia")
    var selectExpanded by remember {mutableStateOf(false)}
    var selectedName by remember {mutableStateOf(names[0])}
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selected by remember {mutableStateOf<Task?>(null)}
    var nowaData by remember {mutableStateOf("")}
    val scope = rememberCoroutineScope()
    var error by remember {mutableStateOf("")}
    Scaffold(
        Modifier.fillMaxWidth(),
        bottomBar={DolnePrzyciski(nav, viewModel)},
        topBar = {InternetBar(uiState.internet)}
    )
    {
        paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
        {
            Spacer(Modifier.height(16.dp))
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
            Button(onClick = {
                val ukryte = viewModel.uiState.value.zadania.filter{it.second.isEmpty() && it.first.data==null}.map{it.first}
                if (ukryte.size>0)
                {
                    selected = ukryte.random()
                    if (selectedName == names[0]) nowaData = ZonedDateTime.now(ZoneOffset.UTC).withHour(23).withMinute(59).withSecond(0).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                    else nowaData = ZonedDateTime.now(ZoneOffset.UTC).withHour(23).withMinute(59).withSecond(0).with(
                        TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).format(DateTimeFormatter.RFC_1123_DATE_TIME)
                }
                else
                {
                    error="Nie ma żadnych zadań do losowania!"
                }

            })
            {
                Text("Losuj")
            }
            Spacer(Modifier.height(16.dp))
            if (error!="")
            {

                Text(error, color= ErrorCol, fontSize = 20.sp)
            }
            if (selected!=null)
            {
                Text(
                    "Wylosowane zadanie",
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xffffffde)
                )
                Spacer(Modifier.height(16.dp))
                Card(
                    Modifier.fillMaxWidth(0.7f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFb5731c)
                    )
                )
                {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically)
                    {
                        Column(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                selected!!.nazwa,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xffffffde)
                            )
                            Text(
                                nowaData,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xffffffde)
                            )


                        }

                    }
                    Spacer(Modifier.height(16.dp))
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    scope.launch{
                        viewModel.editTask(selected!!.ID, TaskEditPOST(nowaData, selected!!.nazwa))
                        delay(400)
                        nav.navigate(Screen.Zadania.route)
                    }

                })
                {
                    Text("Akceptuj")
                }
            }
        }


    }


}
