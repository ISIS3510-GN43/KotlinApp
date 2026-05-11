package com.techsolutions.worqee.models.network

import com.techsolutions.worqee.models.clases.Metric
import com.techsolutions.worqee.models.clases.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("usuarios/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String
    ): Response<Map<String, Any?>>

    @POST("login")
    suspend fun login(
        @Body body: Map<String, String>
    ): Response<User>

    @POST("usuarios/registrar")
    suspend fun register(
        @Body user: User
    ): Response<User>

    @GET("usuarios/{id}")
    suspend fun getUserById(
        @Path("id") id: String
    ): Response<User>

    @GET("usuarios/codigo/username/{username}")
    suspend fun getUidByUsername(
        @Path("username") username: String
    ): Response<ResponseBody>

    @GET("usuarios/{userId}/amigos")
    suspend fun getFriends(
        @Path("userId") userId: String
    ): Response<List<Map<String, Any?>>>

    @POST("usuarios/{userId}/solicitudes/{amigoid}")
    suspend fun sendFriendRequest(
        @Path("userId") userId: String,
        @Path("amigoid") friendId: String
    ): Response<ResponseBody>

    @POST("MetricaFriends/nuevo")
    suspend fun createNewMetric(
        @Body request: Metric
    ): Response<ResponseBody>
}