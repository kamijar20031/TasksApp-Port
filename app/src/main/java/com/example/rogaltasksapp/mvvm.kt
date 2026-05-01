package com.example.rogaltasksapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


data class UiState(
    val isLoading: Boolean = true,
    val isHarmoLoading: Boolean = true,
    val zadania: List<Pair<Task, List<Child>>> = emptyList(),
    var wpisyHarmo: List<Harmonogram> = emptyList(),
    val errors: String? = null,
    val info: String? = null,
    val ID: Int? = null
)

class ZadaniaRepository(private val apiService: ApiService) {
    suspend fun getTasks(id:Int,data:String): List<Task> = apiService.getTasks(id).zadania
    suspend fun addTask(id:Int, req : AddTaskPOST) = apiService.addTask(id, req)
    suspend fun deleteTask(id:Int) = apiService.deleteTask(id)
    suspend fun finishTask(id:Int) = apiService.finishTask(id)
    suspend fun login(request : LoginPOST) = apiService.login(request)
    suspend fun register(request : LoginPOST) = apiService.register(request)
    suspend fun getHarmo(id:Int) = apiService.getHarmo(id)
    suspend fun addHarmo(id:Int, request: HarmoPOST) = apiService.addHarmo(id,request)
    suspend fun editHarmo(id:Int, request: HarmoPOST) = apiService.editHarmo(id,request)
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

    fun getHarmo()
    {
        viewModelScope.launch{
            _uiState.update{it.copy(isHarmoLoading = true)}
            try{

                val response = repository.getHarmo(uiState.value.ID?:0)
                _uiState.update { state-> state.copy(wpisyHarmo = response.harmonogram) }
            }
            catch (e: Exception)
            {
                Log.e("HARMONOGRAM", "Exception: ${e.message}")
            }
            _uiState.update{it.copy(isHarmoLoading = false)}
        }
    }

    fun addHarmo(request: HarmoPOST)
    {
        viewModelScope.launch{
            _uiState.update{it.copy(isHarmoLoading = true)}
            try{
                val response = repository.addHarmo(uiState.value.ID?:0, request)
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