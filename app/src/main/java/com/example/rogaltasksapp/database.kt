package com.example.rogaltasksapp

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


data class ParentWithChildren(
    @Embedded val parent: ZadaniaEntity,
    @Relation(
        parentColumn = "ID",
        entityColumn = "parentID"
    )
    val children: List<ZadaniaEntity>
)

@Dao
interface RogalDao
{

    @Transaction
    @Query("SELECT * FROM zadania WHERE status != 100 AND parentID = 0 AND uzytkownik = :ID")
    suspend fun getParentsWithChildren(ID: Int): List<ParentWithChildren>
    @Query("SELECT * FROM zadania WHERE uzytkownik=:ID")
    suspend fun getTasksRaw(ID:Int): List<ZadaniaEntity>
    @Upsert
    suspend fun addTask(task: ZadaniaEntity) : Long
    @Query("UPDATE zadania SET status=100 WHERE ID= :ID")
    suspend fun finishTask(ID:Int)
    @Query("DELETE FROM zadania WHERE id= :ID")
    suspend fun deleteTask(ID:Int)
    @Upsert
    suspend fun addDeletion(deletion: TaskDeletionEntity)
    @Query("DELETE FROM deletions WHERE affectedID= :ID")
    suspend fun deleteDeletion(ID:Int)
    @Upsert
    suspend fun addAddition(addition: TaskAdditionEntity)
    @Query("DELETE FROM additions WHERE affectedID= :ID")
    suspend fun deleteAddition(ID:Int)
    @Query("SELECT * FROM deletions WHERE affectedID= :ID")
    suspend fun findDeletions(ID:Int) : List<TaskDeletionEntity>
    @Query("SELECT * FROM additions WHERE affectedID= :ID")
    suspend fun findAdditions(ID:Int) : List<TaskAdditionEntity>
    @Query("UPDATE zadania SET data=:data, nazwa=:nazwa, lastModified=:lastModified  WHERE ID= :ID")
    suspend fun editTask(ID:Int, data:String, nazwa:String, lastModified:String= ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME))
}

@Database(entities = [ZadaniaEntity::class, TaskDeletionEntity::class, TaskAdditionEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zadaniaDao(): RogalDao
}
