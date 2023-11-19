package com.pawlowski.imucollector.data

import com.pawlowski.imucollector.domain.model.ModelInfo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ModelInfoApi {

    @GET("/info.json")
    suspend fun getLastModel(): ModelInfo

    @GET
    suspend fun downloadModel(@Url fileUrl: String): Response<ResponseBody>
}
