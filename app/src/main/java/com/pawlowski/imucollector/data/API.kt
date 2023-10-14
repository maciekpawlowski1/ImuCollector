package com.pawlowski.imucollector.data

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface API {

    @POST("/submit/{klasa}")
    suspend fun sendIMU(@Body body: RequestBody, @Path("klasa") activityType: String): Response<Unit>
}
