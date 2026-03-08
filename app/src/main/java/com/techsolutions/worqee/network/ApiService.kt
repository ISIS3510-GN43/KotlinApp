package com.techsolutions.worqee.network

// Service Adapter

import com.techsolutions.worqee.models.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("login")
    suspend fun login(
        @Body body: Map<String, String>
    ): Response<Usuario>

    @POST("register")
    suspend fun register(
        @Body usuario: Usuario
    ): Response<Usuario>

    @GET("usuarios/{id}")
    suspend fun getUsuarioById(
        @Path("id") id: String
    ): Response<Usuario>
}