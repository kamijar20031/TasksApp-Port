package com.example.rogaltasksapp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService
{
    @GET("zadaniaBasic/{id}")
    suspend fun getTasksBasic(@Path("id") id: Int) : TasksBasicResponse
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
    @PATCH(value="updateTaskInfo/{id}")
    suspend fun editTask(@Path("id") id: Int, @Body request : TaskEditPOST)
    @PATCH(value="updateFCM/{id}")
    suspend fun updateFCM(@Path("id") id: Int, @Body request : String)
    @GET(value="userData/{id}")
    suspend fun getUserData(@Path("id") id : Int) : UserResponse

    @PATCH(value="userChange/{id}")
    suspend fun changeUserData(@Path("id") id : Int, @Body request: UserPOST)

    @POST(value="noweZadanieDone/{id}")
    suspend fun addTaskDone(@Path("id") id: Int, @Body request : AddTaskPOST) : Response<ResponseFromServer>
}