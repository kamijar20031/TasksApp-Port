package com.example.rogaltasksapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String)
{
    object Zadania: Screen("zadania")
    object AddZad: Screen("zadNowe")
    object Harmonogram: Screen("harmonogram")
    object Ustawienia: Screen("ustawienia")
    object Logowanie : Screen("login")
    object Rejestrowanie : Screen("register")
    object Random : Screen("randomTask")
}

sealed class NavigationScreens(
    val route: String,
    val title: String,
    val icon: ImageVector
)
{
    data object Zadania : NavigationScreens(Screen.Zadania.route, "Zadania", Icons.AutoMirrored.Filled.List
    )
    data object AddTask : NavigationScreens(Screen.AddZad.route, "", Icons.Default.Add)
    data object Harmonogram : NavigationScreens(Screen.Harmonogram.route, "Harmonogram",
        Icons.Default.DateRange
    )
    data object Ustawienia : NavigationScreens(Screen.Ustawienia.route, "Ustawienia", Icons.Default.Settings
    )
}

@Composable
fun MainNav(viewModel : TaskViewModel)
{
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.ID)
    {
        val currentRoute = navController.currentDestination?.route
        if (uiState.ID!=0 && currentRoute?.startsWith("main") != true)
        {
            navController.navigate("main")
            {
                popUpTo("auth") { inclusive = true }
                launchSingleTop = true
            }
        }
        else if (uiState.ID==0 && currentRoute?.startsWith("auth") != true)
        {
            navController.navigate("auth") {
                popUpTo("main") { inclusive = true }
                launchSingleTop = true
            }
        }

    }
    NavHost(navController=navController, startDestination = "auth")
    {
        navigation(
            startDestination = Screen.Logowanie.route,
            route = "auth"
        ) {
            composable(Screen.Logowanie.route) {
                Login(navController, viewModel)
            }
            composable(Screen.Rejestrowanie.route) {
                Rejestruj(navController, viewModel)
            }
        }
        navigation(
            startDestination = Screen.Zadania.route,
            route = "main"
        )
        {
            composable(route = Screen.Zadania.route)
            {
                Zadania(nav = navController, viewModel)
            }
            composable(route = Screen.Harmonogram.route)
            {
                viewModel.getHarmo()
                Harmonogram(nav = navController, viewModel)
            }
            composable(route = Screen.Ustawienia.route)
            {
                Ustawienia(nav = navController, viewModel)
            }
            composable(route = Screen.AddZad.route)
            {
                Dodaj(navController, viewModel)
            }
            composable(route = Screen.Random.route)
            {
                LosujScreen(navController, viewModel)
            }
        }
    }
}


@Composable
fun DolnePrzyciski(nav: NavHostController)
{
    val screens = listOf(NavigationScreens.Zadania, NavigationScreens.Harmonogram,
        NavigationScreens.Ustawienia)
    val nav1 by nav.currentBackStackEntryAsState()
    val crnt =nav1?.destination
    NavigationBar(containerColor = Color(0xFF202020))
    {
        screens.forEach{
                screen->NavigationBarItem(
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFEFEFE),
                selectedTextColor = Color(0xFFFEFEFE),
                unselectedIconColor = Color(0xFF808080),
                unselectedTextColor = Color(0xFF808080),
                indicatorColor = Color(0x22edd83b)
            ),
            label = {Text(text=screen.title)},
            selected = crnt?.hierarchy?.any{it.route==screen.route}==true,
            onClick = {nav.navigate(screen.route)},
            icon = { Icon(screen.icon, contentDescription = "") }
        )
        }
    }
}

