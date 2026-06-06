package com.example.rogaltasksapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import jakarta.inject.Inject
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.days

class StatCountDayStreaks @Inject constructor()
{
    operator fun invoke(zadania: List<ZadaniaEntity>): Int
    {
        var max = 0;
        val zads = zadania.filter{it -> it.status==100}.map{it -> dateToInt(it.lastModified)}.sortedDescending()
        if (zads.isNotEmpty())
        {
            var i =0;
            var day1 = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate()
            var start = true;
            var day2 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
            while (i<zads.size && (day1==day2 || day1==day2.plusDays(1)))
            {
                if (day1==day2.plusDays(1))
                {
                    if (!start) max++

                };
                if (start)
                    start = false;
                day1 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
                i++
                if (i<zads.size) day2 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
            }
            if (!start)
                max++;
        }
        return max;
    }
}

class StatMaxDayStreaks @Inject constructor()
{
    operator fun invoke(zadania: List<ZadaniaEntity>): Int
    {
        var maxM = 0;
        var max = 0;
        val zads = zadania.filter{it -> it.status==100}.map{it -> dateToInt(it.lastModified)}.sortedDescending()
        if (zads.isNotEmpty())
        {
            var day1 = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate()
            var i =0;
            var day2 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
            var start = true;

            while (i< zads.size)
            {
                while (i<zads.size && (day1==day2 || day1==day2.plusDays(1)))
                {
                    if (day1==day2.plusDays(1))
                    {
                        if (!start) max++

                    };
                    if (start)
                        start = false;
                    day1 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
                    i++
                    if (i<zads.size)
                        day2 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
                }
                if (i<zads.size)
                    day1 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
                i++
                if (i<zads.size)
                    day2 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
                if (!start) max++
                if (max>maxM)  maxM = max
                max = 0;
            }
        }
        return maxM;
    }
}

class StatDays @Inject constructor()
{
    operator fun invoke(zadania: List<ZadaniaEntity>): Int
    {
        var range = 1;
        val zads = zadania.map{it -> dateToInt(it.lastModified)}.sortedDescending()
        if (zads.isNotEmpty())
        {
            var day1 = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate()
            var i =zads.size-1;
            var day2 = Instant.ofEpochSecond(zads[i]) .atZone(ZoneId.systemDefault()) .toLocalDate()
            range = (day1.toEpochDay()-day2.toEpochDay()).toInt()
        }
        return range;
    }
}

class StatDaysWorked @Inject constructor()
{
    operator fun invoke(zadania: List<ZadaniaEntity>): Int
    {
        val doneZads = zadania.filter{it.status==100}.map{it ->         Instant.ofEpochSecond(dateToInt(it.lastModified))
            .atZone(ZoneId.systemDefault())
            .toLocalDate()}.distinct()
        return doneZads.size;
    }
}

@Composable
fun StatystykiScreen(nav: NavHostController, viewModel : TaskViewModel)
{
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        Modifier.fillMaxWidth(),
        bottomBar={DolnePrzyciski(nav, viewModel)},
        topBar = {InternetBar(uiState.internet)}
    )
    {
        padding ->
        Column(Modifier.fillMaxWidth().padding(padding), horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("Statystyki", fontSize = 28.sp)
            Spacer(Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF181818)
                ),
            )
            {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,)
                {
                    Text("Obecny ciąg dni wykonywania zadań", fontSize = 22.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.streak.toString(), fontSize = 34.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Najdłuższy ciąg dni wykonywania zadań", fontSize = 22.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.maxStreak.toString(), fontSize = 34.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Ilość dni używania aplikacji", fontSize = 22.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.days.toString(), fontSize = 34.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Ilość produktywnych dni w aplikacji", fontSize = 22.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.daysW.toString(), fontSize = 34.sp)
                }

            }
        }

    }

}