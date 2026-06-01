package com.example.rogaltasksapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_pref")
object UserPrefsKeys {val ID = intPreferencesKey("login")}

class SettingsRepository @Inject constructor (@ApplicationContext private val context: Context)
{
    val loginFlow: Flow<Int> = context.dataStore.data.map { preferences -> preferences[UserPrefsKeys.ID]?: 0}

    suspend fun setLogin(id : Int)
    {
        context.dataStore.edit { preferences -> preferences[UserPrefsKeys.ID] = id}
    }
}

class ZadaniaRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getTasksBasic(id:Int): List<ZadaniaEntity> = apiService.getTasksBasic(id).zadania
    suspend fun getTasks(id:Int,data:String): List<Task> = apiService.getTasks(id).zadania
    suspend fun addTask(id:Int, req : AddTaskPOST) = apiService.addTask(id, req)
    suspend fun deleteTask(id:Int) = apiService.deleteTask(id)
    suspend fun finishTask(id:Int) = apiService.finishTask(id)
    suspend fun login(request : LoginPOST) = apiService.login(request)
    suspend fun register(request : LoginPOST) = apiService.register(request)
    suspend fun getHarmo(id:Int) = apiService.getHarmo(id)
    suspend fun addHarmo(id:Int, request: HarmoPOST) = apiService.addHarmo(id,request)
    suspend fun editHarmo(id:Int, request: HarmoPOST) = apiService.editHarmo(id,request)
    suspend fun updateFCM(id: Int, request : String) = apiService.updateFCM(id, request)
    suspend fun editTask(id:Int, request: TaskEditPOST) = apiService.editTask(id, request)
}


class DaoRepository @Inject constructor(
    private val rogalDao: RogalDao
)
{
    suspend fun getTasks(ID:Int): List<ParentWithChildren> = rogalDao.getParentsWithChildren(ID)
    suspend fun getTasksRaw(ID:Int) : List<ZadaniaEntity> = rogalDao.getTasksRaw(ID)
    suspend fun deleteTask(ID: Int) = rogalDao.deleteTask(ID)
    suspend fun finishTask(ID: Int) = rogalDao.finishTask(ID)
    suspend fun addTask(task: ZadaniaEntity) : Long = rogalDao.addTask(task)
    suspend fun addDeletion(type :TaskDeletionEntity) = rogalDao.addDeletion(type)
    suspend fun deleteDeletion(ID:Int) = rogalDao.deleteDeletion(ID)
    suspend fun addAddition(type :TaskAdditionEntity) = rogalDao.addAddition(type)
    suspend fun deleteAddition(ID:Int) = rogalDao.deleteAddition(ID)
    suspend fun getDeletion(ID:Int) = rogalDao.findDeletions(ID)
    suspend fun getAddition(ID:Int) = rogalDao.findAdditions(ID)
    suspend fun editTask(ID:Int, data:String, nazwa:String) = rogalDao.editTask(ID, data, nazwa)

}