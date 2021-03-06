package eu.sesma.generik.api.services

import eu.sesma.generik.api.model.Code
import eu.sesma.generik.api.model.Login
import retrofit2.Call
import retrofit2.http.*


interface LoginRegisterService {

    @POST("register")
    fun register(@Body user: Login): Call<Login>

    @GET("login")
    fun login(@Header("Authorization") credentials: String): Call<Login>

    @POST("update")
    fun setPass(@Header("Authorization") credentials: String, @Body code: String): Call<Void>

    @GET("logout")
    fun logout(@Query("email") email: String): Call<Void>

    @GET("requestCode")
    fun requestCode(@Query("email") email: String): Call<Code>

}


