package com.example.rogaltasksapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rogaltasksapp.ui.theme.RogalTasksAppTheme
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.compose.navigation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import retrofit2.http.DELETE
import retrofit2.http.PATCH

data class Task(
    val ID: Int,
    val nazwa: String,
    val data: String?,
    val children: String?,
    val ratio: Double?,
    val parentID: Int,
)

data class Child(
    val ID : Int,
    val data: String?,
    val nazwa : String?,
    val status: String?,
)

data class TasksResponse(
    val zadania: List<Task>
)
data class AddTaskPOST(
    val nazwa : String,
    val dataTemp : String,
    val rodzic : String
)
data class LoginPOST(
    val login : String,
    val haslo : String,
)
data class ResponseFromServer(
    val message: String? = null,
    val response : String? = null,
    val dane: Int? = null
)

sealed class Screen(val route: String)
{
    object Zadania: Screen("zadania")
    object AddZad: Screen("zadNowe")
    object Harmonogram: Screen("harmonogram")
    object Ustawienia: Screen("ustawienia")
    object Logowanie : Screen("login")
    object Rejestrowanie : Screen("register")
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
    data object Logowanie : NavigationScreens(Screen.Logowanie.route, "Loguj", Icons.AutoMirrored.Default.ExitToApp)
    data object Rejestrowanie : NavigationScreens(Screen.Logowanie.route, "Zarejestruj się", Icons.AutoMirrored.Default.ExitToApp)
}

interface ApiService
{
    @GET("zadania/{id}/any")
    suspend fun getTasks(@Path("id") id: Int) : TasksResponse
    @POST(value="noweZadanie/{id}")
    suspend fun addTask(@Path("id") id: Int, @Body request : AddTaskPOST) : Response<ResponseFromServer>
    @DELETE(value="usunZadanie/{idZad}")
    suspend fun deleteTask(@Path("idZad") id:Int) : Response<ResponseFromServer>
    @PATCH(value="wykonajZadanie/{idZad}")
    suspend fun finishTask(@Path("idZad") id:Int) : Response<ResponseFromServer>
    @POST(value="login")
    suspend fun login(@Body request : LoginPOST) : Response<ResponseFromServer>

    @POST(value="register")
    suspend fun register(@Body request : LoginPOST) : Response<ResponseFromServer>
}
data class UiState(
    val isLoading: Boolean = true,
    val zadania: List<Pair<Task, List<Child>>> = emptyList(),
    val errors: String? = null,
    val info: String? = null,
    val ID: Int? = null
)


object RetroFitInstance {
    private const val BASE_URL = "https://tasks-backend.rogalrogalrogalrogal.online/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
// Test
class ZadaniaRepository(private val apiService: ApiService) {
    suspend fun getTasks(id:Int,data:String): List<Task> = apiService.getTasks(id).zadania
    suspend fun addTask(id:Int, req : AddTaskPOST) = apiService.addTask(id, req)
    suspend fun deleteTask(id:Int) = apiService.deleteTask(id)
    suspend fun finishTask(id:Int) = apiService.finishTask(id)
    suspend fun login(request : LoginPOST) = apiService.login(request)
    suspend fun register(request : LoginPOST) = apiService.register(request)
}
class TaskViewModel(val repository: ZadaniaRepository) : ViewModel()
{
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private var pollingJob: Job? = null

    init {
        pollingJob = viewModelScope.launch{
            while (isActive)
            {
                if (uiState.value.ID!=null)
                    getTasks("any")
                delay(5 * 60 * 1000L)
            }

        }
    }
    private suspend fun getTasks(data:String)
    {
        _uiState.update{it.copy(isLoading = true)}
        try
        {
            val gson = Gson()
            val response = repository.getTasks(uiState.value.ID?:0, data)
            val tasks = response.map { task ->
                val childrenList: List<Child> =
                    gson.fromJson(task.children, Array<Child>::class.java).toList()

                task to childrenList
            }
            delay(500)
            Log.d("VIEWMODEL", "Tasks size: ${tasks.size}")
            _uiState.update{it.copy(isLoading = false, zadania = tasks)}
        }
        catch(e: Exception)
        {
            _uiState.update{it.copy(isLoading = false, errors = "Błąd: ${e.message}")}
        }
    }
    // Pozniej mozna dodac taka opcje ze na ekranie bedzie napis tego co zwraca api
    fun addTask(req: AddTaskPOST)
    {
        viewModelScope.launch {
            try {
                val response = repository.addTask(uiState.value.ID?:0, req)
                delay(200)
                getTasks("any")

            } catch (e: Exception) {
                Log.e("API", "Exception: ${e.message}")
            }
        }
    }
    fun deleteTaskLocal(taskId: Int) {
        _uiState.update { state ->
            val updated = state.zadania.filter { it.first.ID != taskId }
            state.copy(zadania = updated)
        }
    }
    fun removeChildFromParentLocal(taskId: Int, parentID:Int) {
        _uiState.update { state ->
            val updated = state.zadania.map { (task, children) ->
                if (task.ID == parentID) {
                    Log.e("API", children.size.toString())
                    val newChildren = children.filter { it.ID != taskId }
                    Log.e("API", newChildren.size.toString())
                    task to newChildren
                } else {
                    task to children
                }
            }
            state.copy(zadania = updated)
        }
    }
    fun deleteTask(id:Int, par:Int=0)
    {
        viewModelScope.launch{
            try {
                val response = repository.deleteTask(id)
                delay(200)
                if (par!=0)
                    removeChildFromParentLocal(id, par)
                else
                    deleteTaskLocal(id)

            } catch (e: Exception) {
                Log.e("API", "Exception: ${e.message}")
            }
        }
    }
    fun finishTask(id:Int, par:Int=0)
    {
        viewModelScope.launch{
            try {
                val response = repository.finishTask(id)
                delay(200)
                if (par!=0)
                    removeChildFromParentLocal(id, par)
                else
                    deleteTaskLocal(id)

            } catch (e: Exception) {
                Log.e("API", "Exception: ${e.message}")
            }
        }
    }

    fun login(login : String, haslo:String)
    {
        viewModelScope.launch{
            val post = LoginPOST(login, haslo)
            try{
                val response = repository.login(post)
                if (response.isSuccessful)
                {
                    _uiState.update{state -> state.copy(ID = response.body()?.dane)}
                    getTasks( "any")
                }
                else
                {
                    val err = response.errorBody()?.string()
                    val gson = Gson()
                    val error = gson.fromJson(err, ResponseFromServer::class.java)
                    _uiState.update{state -> state.copy(info = error?.message)}
                }

            }
            catch (e: Exception)
            {
                Log.e("API", "Exception: ${e.message}")
            }

        }


    }
    fun logout()
    {
        _uiState.update{state -> state.copy(ID = null)}
    }

    fun register(login : String, haslo:String)
    {
        viewModelScope.launch{
            val post = LoginPOST(login, haslo)
            try{
                val response = repository.register(post)
                if (response.isSuccessful)
                {
                    val responseLog = repository.login(post)
                    _uiState.update{state -> state.copy(ID = responseLog.body()?.dane)}
                    getTasks("any")
                }
                else
                {
                    val err = response.errorBody()?.string()
                    val gson = Gson()
                    val error = gson.fromJson(err, ResponseFromServer::class.java)
                    _uiState.update{state -> state.copy(info = error?.message)}
                }

            }
            catch (e: Exception)
            {
                Log.e("API", "Exception: ${e.message}")
            }

        }


    }
}

class TaskViewModelFactory(private val repo : ZadaniaRepository) : ViewModelProvider.Factory
{
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java))
        {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repo) as T
        }
        throw IllegalArgumentException("Nieznany ViewModel")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RogalTasksAppTheme {
                val repo = ZadaniaRepository(RetroFitInstance.api)
                val factory = TaskViewModelFactory(repo)
                MainNav(viewModel(factory=factory))
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

@Composable
fun MainNav(viewModel : TaskViewModel)
{
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.ID)
    {
        val currentRoute = navController.currentDestination?.route
        if (uiState.ID!=null && currentRoute?.startsWith("main") != true)
        {
            navController.navigate("main")
            {
                popUpTo("auth") { inclusive = true }
                launchSingleTop = true
            }
        }
        else if (uiState.ID==null && currentRoute?.startsWith("auth") != true)
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
                Zadania(nav = navController, viewModel)
            }
            composable(route = Screen.Ustawienia.route)
            {
                Ustawienia(nav = navController, viewModel)
            }
            composable(route = Screen.AddZad.route)
            {
                Dodaj(navController, viewModel)
            }
        }
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
        containerColor=Color(0xFF101010),
        bottomBar={DolnePrzyciski(nav)},
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate(NavigationScreens.AddTask.route) },
                containerColor = Color(0xFFeaba7b)
            )
            {
                Icon(NavigationScreens.AddTask.icon, contentDescription = "", tint=Color(0xFF606060))
            }
        },
    )
    { padding ->
        if (!uiState.isLoading)
            Column(
                modifier = Modifier.padding(padding).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {
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

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(millis))
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dodaj(nav: NavHostController, viewModel : TaskViewModel)
{
    val currentTime = Calendar.getInstance()
    var selectExpanded by remember {mutableStateOf(false)}
    var selectedName by rememberSaveable {mutableStateOf("-")}
    var selectedID by rememberSaveable {mutableStateOf(0)}
    var showDialog by remember { mutableStateOf(false) }
    var showDialogTime by remember { mutableStateOf(false) }
    var disableTime by remember { mutableStateOf(false) }
    var text by rememberSaveable {mutableStateOf("")}
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentTime.timeInMillis + 1000*60*60*24)
    var selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = true,
    )
    val rodzice = viewModel.uiState.collectAsState().value.zadania.filter{it -> it.first.parentID==0}
    val scope = rememberCoroutineScope()
    Scaffold(
        Modifier.fillMaxWidth(),
        containerColor=Color(0xFF101010),
        bottomBar={DolnePrzyciski(nav)},
    )
    {
        padding ->
            LazyColumn(
                modifier=Modifier.padding(padding).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                item {Text("Nowe Zadanie", color = Color(0xfffafafa), fontSize = 32.sp)
                    Spacer(Modifier.height(16.dp))}
                item {Text("Nazwa", color = Color(0xfffafafa), fontSize = 22.sp)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = text,
                        placeholder = {Text("Nakarm psa")},
                        onValueChange = {text=it},
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF80571F),
                            focusedTextColor = Color(0xfffafafa),
                            unfocusedTextColor = Color(0xffeaeaea)
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    Text("Czas", color = Color(0xfffafafa), fontSize = 22.sp)
                    Spacer(Modifier.height(8.dp))
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
                            Text("Data", color = Color(0xfffafafa), fontSize = 16.sp)
                            Box() {
                                OutlinedTextField(
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF80571F),
                                        focusedTextColor = Color(0xfffafafa),
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
                            Text("Godzina", color = Color(0xfffafafa), fontSize = 16.sp)
                            Box() {
                                OutlinedTextField(
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF80571F),
                                        focusedTextColor = Color(0xfffafafa),
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
                    Spacer(Modifier.height(16.dp))
                }
                item {
                    Text("Rodzic", color = Color(0xfffafafa), fontSize = 22.sp)
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
                                focusedBorderColor = Color(0xFF80571F),
                                focusedTextColor = Color(0xfffafafa),
                                unfocusedTextColor = Color(0xffeaeaea),
                                focusedLabelColor = Color(0xFF80571F),
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
                }

                item {
                    Button(onClick={
                        scope.launch{
                            val dane = AddTaskPOST(text,String.format("%s %02d:%02d", selectedDate, timePickerState.hour, timePickerState.minute, Locale.getDefault()), selectedID.toString())
                            viewModel.addTask(dane)
                            delay(400)
                            nav.navigate(Screen.Zadania.route)
                        }

                    },colors= ButtonDefaults.buttonColors(containerColor = Color(0xFF80571F)))
                    {Text("Dodaj zadanie")}
                }



            }
    }

}

fun checkVals(login:String,  haslo:String) : String
{
    if (login=="" && haslo=="")
        return "Wpisz login i hasło!"
    else if (login=="")
        return "Wpisz login"
    else if (haslo=="")
        return "Wpisz hasło"
    else
        return ""
}


@Composable
fun Login(nav: NavHostController, viewModel : TaskViewModel)
{
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPassword by remember { mutableStateOf(false) }
    var login by remember {mutableStateOf("")}
    var pass by remember {mutableStateOf("")}
    var check by remember {mutableStateOf("Wpisz login i hasło!")}
    Scaffold(
        Modifier.fillMaxWidth(),
        containerColor=Color(0xFF101010)
    )
    {
        padding->
        Column(modifier = Modifier.padding(padding).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("Zaloguj się", color = Color(0xfffafafa), fontSize = 22.sp)
            Spacer(Modifier.height(24.dp))
            Text("Login", color = Color(0xfffafafa), fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = login,
                placeholder = {Text("Login")},
                onValueChange = {login=it; check=checkVals(login,pass)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF80571F),
                    focusedTextColor = Color(0xfffafafa),
                    unfocusedTextColor = Color(0xffeaeaea)
                )
            )
            Spacer(Modifier.height(24.dp))
            Text("Hasło", color = Color(0xfffafafa), fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = {pass=it; check=checkVals(login,pass)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF80571F),
                    focusedTextColor = Color(0xfffafafa),
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
            Spacer(Modifier.height(16.dp))
            Text(text=if (check!="") check else uiState.info ?: "", color=Color.Red)
            Spacer(Modifier.height(24.dp))
            Button(onClick={
                nav.navigate(Screen.Rejestrowanie.route)
            },colors= ButtonDefaults.buttonColors(containerColor = Color(0xFF80571F)))
            {Text("Zarejestuj się")}
            Button(onClick={
                viewModel.login(login, pass)

            },colors= ButtonDefaults.buttonColors(containerColor = Color(0xFFA67126)))
            {Text("Zaloguj się")}
        }
    }

}

@Composable
fun Rejestruj(nav: NavHostController, viewModel : TaskViewModel)
{
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPassword by remember { mutableStateOf(false) }
    var login by remember {mutableStateOf("")}
    var pass by remember {mutableStateOf("")}
    var check by remember {mutableStateOf("Wpisz login i hasło!")}
    Scaffold(
        Modifier.fillMaxWidth(),
        containerColor=Color(0xFF101010)
    )
    {
            padding->
        Column(modifier = Modifier.padding(padding).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("Załóż konto", color = Color(0xfffafafa), fontSize = 22.sp)
            Spacer(Modifier.height(24.dp))
            Text("Login", color = Color(0xfffafafa), fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = login,
                placeholder = {Text("Login")},
                onValueChange = {login=it; check=checkVals(login,pass)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF80571F),
                    focusedTextColor = Color(0xfffafafa),
                    unfocusedTextColor = Color(0xffeaeaea)
                )
            )
            Spacer(Modifier.height(24.dp))
            Text("Hasło", color = Color(0xfffafafa), fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = {pass=it; check=checkVals(login,pass)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF80571F),
                    focusedTextColor = Color(0xfffafafa),
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
            Spacer(Modifier.height(16.dp))
            Text(text=if (check!="") check else uiState.info ?: "", color=Color.Red)
            Spacer(Modifier.height(24.dp))
            Button(onClick={
                nav.navigate(Screen.Logowanie.route)
            },colors= ButtonDefaults.buttonColors(containerColor = Color(0xFF80571F)))
            {Text("Zaloguj się")}
            Button(onClick={
                viewModel.register(login, pass)

            },colors= ButtonDefaults.buttonColors(containerColor = Color(0xFFA67126)))
            {Text("Zarejestruj się")}
        }
    }

}

@Composable
fun Ustawienia(nav: NavHostController, viewModel : TaskViewModel)
{
    Scaffold(
        Modifier.fillMaxWidth(),
        containerColor=Color(0xFF101010),
        bottomBar={DolnePrzyciski(nav)},
    )
    { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text("Ustawienia", color = Color(0xfffafafa), fontSize = 32.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick={
                viewModel.logout()

            },colors= ButtonDefaults.buttonColors(containerColor = Color(0xFFA67126)))
            {Text("Wyloguj się")}
        }
    }
}