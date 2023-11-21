package com.pawlowski.imucollector.data

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface InterferenceApi {

    @POST("interference")
    suspend fun getPredictions(@Body body: RequestBody): Response<List<List<Float>>>
}
