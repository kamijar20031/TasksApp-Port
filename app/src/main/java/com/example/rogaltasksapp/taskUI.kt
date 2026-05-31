package com.example.rogaltasksapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun InternetBar(internet: Boolean)
{
  if (!internet)
    Box(modifier=Modifier.fillMaxWidth().background(Color(0xFF0162CB)).statusBarsPadding().height(32.dp).padding(all=4.dp), contentAlignment = Alignment.Center) {
        Text("Tryb offline")
    }
}
@Composable
fun DrawInfoRow(nazwa:String?, data: String?)
{
    Row(verticalAlignment = Alignment.CenterVertically)
    {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (nazwa != null)
                Text(
                    nazwa,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xffffffde)
                )
            if (data != null) {
                Text(
                    data,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xffffffde)
                )
            }

        }

    }
}

@Composable
fun DrawElementChild(child: Child, parentID: Int, viewModel: TaskViewModel)
{
    var deleting by remember {mutableStateOf(false)}
    var edit by remember {mutableStateOf(false)}
    AnimatedVisibility(
        visible = !deleting,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    )
    {
        Card(
            Modifier.fillMaxWidth(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFb5731c)
            )
        )
        {
            Column(
                modifier=Modifier.padding(8.dp)
            ) {
                DrawInfoRow(child.nazwa, child.data)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
                {
                    Button(
                        onClick = { edit= !edit },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff634f23))
                    ) {
                        Icon(Icons.Default.Edit, "")
                    }
                    Spacer(modifier=Modifier.width(24.dp))
                    if (edit)
                        Button(
                            onClick = { deleting = true; viewModel.deleteTask(child.ID, par=parentID) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff4e2727))
                        ) {
                            Icon(Icons.Default.Delete, "")
                        }
                    else
                        Button(
                            onClick = { deleting = true; viewModel.finishTask(child.ID, par=parentID) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff314836))
                        ) {
                            Icon(Icons.Default.Check, "")
                        }

                }

            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Zadania(nav: NavHostController, viewModel : TaskViewModel)
{
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        Modifier.fillMaxWidth(),
        bottomBar={DolnePrzyciski(nav)},
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate(NavigationScreens.AddTask.route) },
            )
            {
                Icon(NavigationScreens.AddTask.icon, contentDescription = "",)
            }
        },
        topBar = {InternetBar(uiState.internet)}

    )
    { padding ->
        if (!uiState.isLoading)
            Column(
                modifier = Modifier.padding(padding).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {
                Button(onClick = {nav.navigate(Screen.Random.route)})
                {
                    Text("Losuj zadania")
                }
                LazyColumn(Modifier.fillMaxWidth(0.8f), horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(top = 12.dp))
                {

                    items(uiState.zadania, key={it.first.ID})
                    { task ->
                        if (task.first.parentID == 0) {
                            var closed by rememberSaveable(task.first.ID) { mutableStateOf(true) }
                            val children = task.second
                            val hasChildren = children.isNotEmpty() && children[0].nazwa != null
                            var edit by remember {mutableStateOf(false)}
                            var deleting by remember {mutableStateOf(false)}
                            AnimatedVisibility(
                                visible = !deleting,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            )
                            {
                                Card(
                                    Modifier.fillMaxWidth(0.9f).animateContentSize(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFCE8321)
                                    )
                                )
                                {

                                    Column(modifier=Modifier.padding(8.dp))
                                    {
                                        DrawInfoRow(task.first.nazwa, task.first.data)
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
                                        {
                                            Button(
                                                onClick = { edit=!edit },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff634f23))
                                            ) {
                                                Icon(Icons.Default.Edit, "")
                                            }
                                            Spacer(modifier=Modifier.width(24.dp))
                                            if (!hasChildren)
                                            {
                                                if (!edit)
                                                    Button(
                                                        onClick = { deleting=true;viewModel.finishTask(task.first.ID) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff314836))
                                                    ) {
                                                        Icon(Icons.Default.Check, "")
                                                    }
                                                else
                                                    Button(
                                                        onClick = { deleting = true; viewModel.deleteTask(task.first.ID) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff4e2727))
                                                    ) {
                                                        Icon(Icons.Default.Delete, "")
                                                    }
                                            }
                                            else
                                                Button(
                                                    onClick = { closed = !closed },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff000000))
                                                ) {
                                                    Icon(
                                                        if (closed)                                                    Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, "")
                                                }

                                        }
                                    }

                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            if (hasChildren)
                            {
                                AnimatedVisibility(
                                    visible = !closed,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(0.9f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    )
                                    {
                                        children.forEach { child ->
                                            key(child.ID){DrawElementChild(child, task.first.ID,viewModel)
                                                Spacer(modifier = Modifier.height(16.dp))}
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }

                        }
                    }

                }
            }
        else
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier.padding(padding).fillMaxSize()
            )
            {
                CircularProgressIndicator()
            }


    }
}