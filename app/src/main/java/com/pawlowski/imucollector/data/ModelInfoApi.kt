package com.pawlowski.imucollector.data

import com.pawlowski.imucollector.domain.model.ModelInfo
import retrofit2.http.GET

interface ModelInfoApi {

    @GET("/info.json")
    suspend fun getLastModel(): ModelInfo
}
