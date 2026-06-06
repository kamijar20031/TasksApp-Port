package com.example.rogaltasksapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


fun dateToInt(date : String) : Long
{
    return ZonedDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().epochSecond
}


class Converters {

    @TypeConverter
    fun fromDni(value: dniD?): String? {
        return value?.let {
            Json.encodeToString(it)
        }
    }

    @TypeConverter
    fun toDni(value: String?): dniD? {
        return value?.let {
            Json.decodeFromString<dniD>(it)
        }
    }
}

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
    val status: Int?,
)

data class TasksResponse(
    val zadania: List<Task>
)

data class UserInfo(
    val ilePowiadomien : Int,
    val login : String
)

data class UserResponse(
    val dane : List<UserInfo>
)

@Serializable
data class Day(
    val id: Int?,
    val hour : Int?,
    val minute: Int?
)

@Serializable
data class dniD(
    val interval : Int?,
    val type: String?,
    val time: String?,
    val date: String?,
    val days: List<Day>? = null,
)


data class AddTaskPOST(
    val nazwa: String,
    val dataTemp: String?,
    val rodzic: String
)
data class LoginPOST(
    val login : String,
    val haslo : String,
)

data class HarmoPOST(
    val nazwa : String,
    val dniD : String,
)
data class TaskEditPOST(
    val data: String?,
    val nazwa: String,
)

data class UserPOST(
    val what : String,
    val value : String,
)
data class ResponseFromServer(
    val message: String? = null,
    val response : String? = null,
    val dane: Int? = null
)

data class TasksBasicResponse(
    val zadania: List<ZadaniaEntity>
)

data class HarmonogramResponse(
    val harmonogram : List<Harmonogram>
)

@Entity(tableName = "zadania")
data class ZadaniaEntity(
    @PrimaryKey (autoGenerate = true)
    val ID: Int=0,
    val status: Int,
    val uzytkownik: Int,
    val nazwa: String,
    val data: String?,
    val parentID: Int,
    val lastModified: String,
)

@Entity(tableName = "deletions")
data class TaskDeletionEntity(
    @PrimaryKey (autoGenerate = true)
    val ID: Int=0,
    val affectedID: Int
)
@Entity(tableName = "additions")
data class TaskAdditionEntity(
    @PrimaryKey (autoGenerate = true)
    val ID: Int=0,
    val affectedID: Int
)
data class Harmonogram(
    val ID : Int,
    val nazwa : String,
    val dni : dniD? = null,
    val uzytkownik :Int,
)
