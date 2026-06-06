package com.example.rogaltasksapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


data class UiState(
    val isLoading: Boolean = true,
    val isHarmoLoading: Boolean = true,
    val areSettingsLoading: Boolean = true,
    val zadania: List<Pair<Task, List<Child>>> = emptyList(),
    var wpisyHarmo: List<Harmonogram> = emptyList(),
    val errors: String? = null,
    val info: String? = null,
    val ID: Int = 0,
    val internet: Boolean = true,
    val zadaniaAll: List<ZadaniaEntity> = emptyList(),

    val streak: Int = 0,
    val maxStreak: Int = 0,
    val days: Int = 1,
    val daysW: Int =0,

    val userName: String = "",
    val ilePowiadomien: Int = 0
)

@HiltViewModel
class TaskViewModel @Inject constructor(val repository: ZadaniaRepository, val settingsRepo: SettingsRepository, private val internetConnection: InternetConnection, val daoRepo: DaoRepository, private val mergeTasks: DAOMergeTasksUseCase, private val getStreak :  StatCountDayStreaks, private val getMaxStreak : StatMaxDayStreaks, private val getDays: StatDays, private val getWorkingDays : StatDaysWorked) : ViewModel()
{


    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private var pollingJob: Job? = null
    private var prevNetState = false;
    init {

        viewModelScope.launch{
            internetConnection.connected.collect{
                    value ->
                    _uiState.update{it.copy(internet=value)}
                    delay(20)
                    if (!prevNetState && value) updateDAO()
                    prevNetState=value

            }
        }
        viewModelScope.launch{
            settingsRepo.loginFlow.collect {id -> _uiState.update {it.copy(ID=id)}
                if (id!=0)
                {
                    getTasks("any")
                    getHarmo()
                    updateFCM(id)
                }

            }
        }
        pollingJob = viewModelScope.launch{
            while (isActive)
            {
                if (uiState.value.ID!=0)
                    getTasks("any")
                delay(5 * 60 * 1000L)
            }

        }
    }

    private suspend fun updateFCM(id:Int)
    {
        if (uiState.value.internet)
            repository.updateFCM(id, Firebase.messaging.token.await())
    }
    private suspend fun updateDAO()
    {
        try
        {
            _uiState.update{it.copy(isLoading = true)}

            val api = repository.getTasksBasic(uiState.value.ID)
            val dao = daoRepo.getTasksRaw(uiState.value.ID)
            mergeTasks(dao, api)
            delay(200)
            val results = daoRepo.getTasks(uiState.value.ID).map{ item-> val children = item.children
                val ratio = if (children.isNotEmpty()){
                    children.map {it.status}.average()
                } else 0.0
                Task( item.parent.ID, item.parent.nazwa, item.parent.data,"", ratio, item.parent.parentID) to children.map {
                    Child(it.ID, it.data, it.nazwa, it.status)
                }
            }

            _uiState.update{it.copy(isLoading = false, zadania=results)}

        }
        catch (e:Exception)
        {
            _uiState.update{it.copy(errors = "Błąd: ${e.message}")}
        }
    }
    private suspend fun getTasks(data:String)
    {

        if (_uiState.value.internet)
            try
            {
                getUserSettings()
                updateDAO()
            }
            catch(e: Exception)
            {
                _uiState.update{it.copy(errors = "Błąd: ${e.message}")}
            }
        else
        {
            _uiState.update{it.copy(isLoading = true)}
            val results = daoRepo.getTasks(uiState.value.ID).map{ item-> val children = item.children
                val ratio = if (children.isNotEmpty()){
                    children.map {it.status}.average()
                } else 0.0
                Task( item.parent.ID, item.parent.nazwa, item.parent.data,"", ratio, item.parent.parentID) to children.map {
                    Child(it.ID, it.data, it.nazwa, it.status)
                }
            }

            _uiState.update{it.copy(isLoading = false, zadania=results)}
        }
    }
    // Pozniej mozna dodac taka opcje ze na ekranie bedzie napis tego co zwraca api
    fun addTask(req: AddTaskPOST)
    {
        viewModelScope.launch {
            if (uiState.value.internet)
            {
                try {
                    Log.d("TESTAPI", "Dodaje zadanie")
                    val response = repository.addTask(uiState.value.ID, req)
                    Log.d("TESTAPI", response.toString())
                    delay(200)
                    getTasks("any")

                } catch (e: Exception) {
                    Log.e("API", "Exception: ${e.message}")
                }
            }
            else
            {
                val temp = ZadaniaEntity(status = 0, uzytkownik = uiState.value.ID, nazwa=req.nazwa, data = req.dataTemp, parentID = req.rodzic.toInt(), lastModified = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME))
                val ID = daoRepo.addTask(temp).toInt()
                val temp1 = TaskAdditionEntity(affectedID = ID)
                daoRepo.addAddition(temp1)
                getTasks("any")
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
                if (uiState.value.internet)
                    repository.deleteTask(id)
                else
                {
                    daoRepo.deleteTask(id)
                    daoRepo.addDeletion(TaskDeletionEntity(affectedID = id))
                }

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
                if (uiState.value.internet)
                    repository.finishTask(id)
                else
                    daoRepo.finishTask(id)

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
                    _uiState.update{state -> state.copy(ID = response.body()?.dane?:0)}
                    settingsRepo.setLogin(uiState.value.ID)
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
        viewModelScope.launch {
            if (uiState.value.internet)
                repository.updateFCM(_uiState.value.ID, "")
            _uiState.update { state -> state.copy(ID = 0) }
            settingsRepo.setLogin(0)
        }
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
                    _uiState.update{state -> state.copy(ID = responseLog.body()?.dane?:0)}
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

    fun getHarmo()
    {
        viewModelScope.launch{
            if (uiState.value.internet)
            {
                _uiState.update{it.copy(isHarmoLoading = true)}
                try{

                    val response = repository.getHarmo(uiState.value.ID)
                    _uiState.update { state-> state.copy(wpisyHarmo = response.harmonogram) }
                }
                catch (e: Exception)
                {
                    Log.e("HARMONOGRAM", "Exception: ${e.message}")
                }
            }
            _uiState.update{it.copy(isHarmoLoading = false)}
        }
    }

    fun addHarmo(request: HarmoPOST)
    {
        viewModelScope.launch{
            _uiState.update{it.copy(isHarmoLoading = true)}
            try{
                val response = repository.addHarmo(uiState.value.ID, request)
                getHarmo()
            }
            catch (e: Exception)
            {
                Log.e("HARMONOGRAM", "Exception: ${e.message}")
            }
            _uiState.update{it.copy(isHarmoLoading = false)}
        }
    }
    fun editHarmo(request: HarmoPOST, harmoID: Int)
    {
        viewModelScope.launch{
            _uiState.update{it.copy(isHarmoLoading = true)}
            try{
                val response = repository.editHarmo(harmoID, request)
                getHarmo()
            }
            catch (e: Exception)
            {
                Log.e("HARMONOGRAM", "Exception: ${e.message}")
            }
            _uiState.update{it.copy(isHarmoLoading = false)}
        }
    }

    fun editTask(ID : Int, request : TaskEditPOST)
    {
        viewModelScope.launch {
            if (uiState.value.internet)
            {
                repository.editTask(ID, request)
            }
            else
            {
                daoRepo.editTask(ID, request.data?:"", request.nazwa)
            }
            getTasks("")
        }
    }

    fun getUserSettings()
    {
        viewModelScope.launch {
            if (uiState.value.internet)
            {
                val re = repository.getUserInfo(uiState.value.ID).dane[0]
                _uiState.update { it.copy(ilePowiadomien = re.ilePowiadomien, userName = re.login) }
            }
        }
    }

    fun getTasksRawAll()
    {
        viewModelScope.launch {
            var re : List<ZadaniaEntity> = emptyList()
            if (uiState.value.internet)
            {
                re = repository.getTasksBasic(uiState.value.ID)
            }
            else
            {
                re = daoRepo.getTasksRaw(uiState.value.ID)

            }
            val days = getDays(re)
            val streak = getStreak(re)
            val streakM = getMaxStreak(re)
            val daysW = getWorkingDays(re)
            _uiState.update{it.copy(zadaniaAll = re, streak = streak, maxStreak = streakM, days = days, daysW = daysW)}

        }
    }

    fun changeUserData(req : UserPOST)
    {
        viewModelScope.launch{
            repository.changeUserData(uiState.value.ID, req)
            getUserSettings()
        }
    }

}