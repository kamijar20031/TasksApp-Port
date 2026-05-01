package com.example.rogaltasksapp

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

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

data class Day(
    val id: Int,
    val hour : Int,
    val minute: Int
)

data class dniD(
    val interval : Int,
    val type: String,
    val time: String?,
    val date: String?,
    val days: List<Day>,
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

data class HarmoPOST(
    val nazwa : String,
    val dniD : String,
)

data class ResponseFromServer(
    val message: String? = null,
    val response : String? = null,
    val dane: Int? = null
)

data class Harmonogram(
    val ID : Int,
    val nazwa : String,
    val dni : dniD? = null,

)

data class HarmonogramResponse(
    val harmonogram : List<Harmonogram>
)

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
    @GET(value="harmonogram/{id}")
    suspend fun getHarmo(@Path(value="id") id:Int) : HarmonogramResponse
    @POST(value="harmonogramCreate/{id}")
    suspend fun addHarmo(@Path("id") id: Int, @Body request : HarmoPOST)
    @PATCH(value="harmonogramEdit/{id}")
    suspend fun editHarmo(@Path("id") id: Int, @Body request : HarmoPOST)
}

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