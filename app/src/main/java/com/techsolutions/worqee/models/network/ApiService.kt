package com.techsolutions.worqee.models.network

// Service Adapter

import com.techsolutions.worqee.models.clases.Metrica
import com.techsolutions.worqee.models.clases.Usuario
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("usuarios/{userId}")
        suspend fun getUsuario(
            @Path("userId") userId: String
        ): Response<Map<String, Any?>>

    @POST("login")
    suspend fun login(
        @Body body: Map<String, String>
    ): Response<Usuario>

    @POST("usuarios/registrar")
    suspend fun register(
        @Body usuario: Usuario
    ): Response<Usuario>

    @GET("usuarios/{id}")
    suspend fun getUsuarioById(
        @Path("id") id: String
    ): Response<Usuario>

    @GET("usuarios/codigo/username/{username}")
    suspend fun getUidByUsername(
        @Path("username") username: String
    ): Response<ResponseBody>

    @GET("usuarios/{userId}/amigos")
    suspend fun getAmigos(
        @Path("userId") userId: String
    ): Response<List<Map<String, Any?>>>

    @POST("usuarios/{userId}/solicitudes/{amigoid}")
    suspend fun enviarSolicitud(
        @Path("userId") userId: String,
        @Path("amigoid") amigoid: String
    ): Response<ResponseBody>


    //Pipeline
    @POST("MetricaFriends/nuevo")
    suspend fun crearNuevaMetrica(
        @Body request: Metrica
    ): Response<ResponseBody>


}