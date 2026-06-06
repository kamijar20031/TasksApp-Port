package com.example.rogaltasksapp
import android.util.Log
import jakarta.inject.Inject
import kotlinx.coroutines.delay

data class TaskPair(var temp: ZadaniaEntity? = null, var second: ZadaniaEntity? = null)

class DAOMergeTasksFinished @Inject constructor(val dao: DaoRepository,  val api : ZadaniaRepository)
{
    suspend operator fun invoke(offline: ZadaniaEntity, online: ZadaniaEntity) : Boolean
    {
        if (offline.status==100)
        {
            api.finishTask(offline.ID)
            return true
        }
        else if (online.status==100)
        {
            dao.finishTask(offline.ID)
            return true
        }
        else return false

    }
}

class DAOMergeTasksUpdate @Inject constructor(private val tasksFinished: DAOMergeTasksFinished, private val dao: DaoRepository,  private val api : ZadaniaRepository)
{
    suspend operator fun invoke(offline: ZadaniaEntity, online: ZadaniaEntity) : ZadaniaEntity
    {
        if (!tasksFinished(offline, online))
        {
            if (offline.data != online.data || offline.nazwa!=online.nazwa)
            {
                if (dateToInt(offline.lastModified)>=dateToInt(online.lastModified))
                {
                    val temp = TaskEditPOST(offline.data, offline.nazwa)
                    api.editTask(offline.ID,temp)
                    return offline
                }
                else
                {
                    dao.editTask(online.ID, online.data?: "", online.nazwa)
                    return online
                }
            }
            else
            {
                return offline
            }
        }
        else
        {
            return ZadaniaEntity(online.ID, 100, online.uzytkownik, online.nazwa, online.data, online.parentID, online.lastModified)
        }


    }
}

class DAOFindDeletions @Inject constructor(val dao: DaoRepository)
{
    suspend operator fun invoke(ID: Int) : Boolean
    {
        val info = dao.getDeletion(ID)
        return info.isNotEmpty()
    }
}

class DAOFindAdditions @Inject constructor(val dao: DaoRepository)
{
    suspend operator fun invoke(ID: Int) : Boolean
    {
        val info = dao.getAddition(ID)
        return info.isNotEmpty()
    }
}

class DAOMergeTasksUseCase @Inject constructor(private val mergeCase : DAOMergeTasksUpdate, private val deleteCase: DAOFindDeletions, private val addCase : DAOFindAdditions, val dao: DaoRepository,  val api : ZadaniaRepository)
{
    suspend operator fun invoke(offline:  List<ZadaniaEntity>, online:   List<ZadaniaEntity>) : List<ZadaniaEntity>
    {
        val taskMap = mutableMapOf<Int, TaskPair>()
        offline.forEach { taskMap[it.ID] = TaskPair(it) }
        online.forEach {
            if (taskMap[it.ID] !=null) {
            try {
                taskMap[it.ID]!!.second=mergeCase(taskMap[it.ID]!!.temp!!, it)
            }
            catch(e: Exception)
            {
                Log.d("MERGE", e.toString())
            }

        }
        else
        {
            if (deleteCase(it.ID))
            {
                try {
                    api.deleteTask(it.ID)
                    dao.deleteDeletion(it.ID)
                }
                catch(e: Exception)
                {
                    Log.d("MERGE", e.toString())
                }

            }
            else
            {
                try {
                    taskMap[it.ID] = TaskPair(null, it)
                    dao.addTask(it)
                }
                catch(e: Exception)
                {
                    Log.d("MERGE", e.toString())
                }
            }
        }}
        taskMap.forEach{ (key, it) -> if (it.second==null && it.temp !=null) {
            if (addCase(key))
            {
                try {

                    val tempo = AddTaskPOST(it.temp!!.nazwa, it.temp!!.data?:"NULL", it.temp!!.parentID.toString())
                    it.second = it.temp
                    if (it.temp!!.status==100)
                    {
                        api.addTaskDone(it.temp!!.uzytkownik, tempo)
                    }
                    else
                    {
                        api.addTask(it.temp!!.uzytkownik, tempo)
                    }
                    dao.deleteAddition(key)
                }
                catch(e: Exception)
                {
                    Log.d("MERGE", e.toString())
                }

            }
            else
            {
                try {
                    dao.deleteTask(key)
                }
                catch(e: Exception)
                {
                    Log.d("MERGE", e.toString())
                }
            }
        }
        }
        return taskMap.values.mapNotNull{it.second}
    }
}