package com.example.rogaltasksapp

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dodaj(nav: NavHostController, viewModel : TaskViewModel)
{
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTime = Calendar.getInstance()
    var selectExpanded by remember {mutableStateOf(false)}
    var selectedName by rememberSaveable {mutableStateOf("-")}
    var selectedID by rememberSaveable {mutableStateOf(0)}
    var showDialog by remember { mutableStateOf(false) }
    var showDialogTime by remember { mutableStateOf(false) }
    var disableTime by remember { mutableStateOf(true) }
    var text by rememberSaveable {mutableStateOf("")}
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentTime.timeInMillis + 1000*60*60*24)
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = true,
    )
    val rodzice = viewModel.uiState.collectAsState().value.zadania.filter{it.first.parentID==0}
    val scope = rememberCoroutineScope()

    var nazwaError by remember {mutableStateOf(text.isBlank())}



    Scaffold(
        Modifier.fillMaxWidth(),
        bottomBar={DolnePrzyciski(nav, viewModel)},
        topBar = {InternetBar(uiState.internet)}
    )
    {
            padding ->
        LazyColumn(
            modifier=Modifier.padding(padding).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            item {
                Spacer(Modifier.height(16.dp))
                Text("Nowe Zadanie",  fontSize = 32.sp)
                Spacer(Modifier.height(32.dp))
                Text("Nazwa", fontSize = 22.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    placeholder = {Text("Nakarm psa")},
                    onValueChange = {text=it; nazwaError= text.isBlank()},
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = Color(0xffeaeaea)
                    ),
                    isError = nazwaError
                )
            }
            item{
                Spacer(Modifier.height(16.dp))
                Text("Czas", fontSize = 22.sp)
                Spacer(Modifier.height(16.dp))
                Checkbox(
                    checked = !disableTime,
                    onCheckedChange = { disableTime = !it }
                )
                AnimatedVisibility(
                    visible = !disableTime,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                )
                {
                    Column(
                        Modifier.padding(horizontal = 40.dp).border(1.dp, Color(0xff303030), shape = RoundedCornerShape(16.dp)).padding(start=30.dp, end=30.dp, top=18.dp, bottom=36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally)
                    {
                        Text("Data", fontSize = 16.sp)
                        Box() {
                            OutlinedTextField(
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = Color(0xffeaeaea)
                                ),
                                value = selectedDate,
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showDialog = !showDialog }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Select date"
                                        )
                                    }
                                },
                            )

                            if (showDialog) {
                                Popup(
                                    onDismissRequest = { showDialog = false },
                                    alignment = Alignment.TopStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = 64.dp)
                                            .padding(16.dp)
                                    ) {
                                        DatePicker(
                                            state = datePickerState,
                                            showModeToggle = false
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Godzina", fontSize = 16.sp)
                        Box() {
                            OutlinedTextField(
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = Color(0xffeaeaea)
                                ),
                                value = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour,timePickerState.minute),
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showDialogTime = !showDialogTime }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Select date"
                                        )
                                    }
                                },
                            )

                            if (showDialogTime) {
                                Popup(
                                    onDismissRequest = { showDialogTime = false },
                                    alignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xfd303030), shape= RoundedCornerShape(16.dp))
                                            .padding(16.dp)
                                    ) {

                                        TimePicker(
                                            state = timePickerState,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
            item{
                Spacer(Modifier.height(16.dp))
                Text("Rodzic", fontSize = 22.sp)
                ExposedDropdownMenuBox(
                    expanded = selectExpanded,
                    onExpandedChange = { selectExpanded = !selectExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wybierz opcję") },
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
                    ) {
                        rodzice.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.first.nazwa) },
                                onClick = {
                                    selectedName = item.first.nazwa
                                    selectedID = item.first.ID
                                    selectExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick={
                    scope.launch{
                        if (disableTime)
                        {
                            val dane = AddTaskPOST(text,"NULL", selectedID.toString())
                            viewModel.addTask(dane)
                        }
                        else
                        {
                            val dane = AddTaskPOST(text,String.format("%s %02d:%02d", selectedDate, timePickerState.hour, timePickerState.minute, Locale.getDefault()), selectedID.toString())
                            viewModel.addTask(dane)
                        }
                        delay(400)
                        nav.navigate(Screen.Zadania.route)
                    }

                },
                    enabled = !nazwaError)
                {Text("Dodaj zadanie")}
                Spacer(Modifier.height(16.dp))
            }


        }
    }

}