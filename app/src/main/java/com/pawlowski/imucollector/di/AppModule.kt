package com.pawlowski.imucollector.di

import com.pawlowski.imucollector.data.ModelInfoApi
import com.pawlowski.imucollector.data.ModelTrainingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Named("modelTraining")
    @Provides
    fun modelTrainingRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("http://srv3.enteam.pl:4301")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Named("modelInfo")
    @Provides
    fun modelInfoRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://mateuszwozniak.s3.eu-central-1.amazonaws.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    fun modelTrainingApi(@Named("modelTraining") retrofit: Retrofit): ModelTrainingApi = retrofit
        .create(ModelTrainingApi::class.java)

    @Provides
    fun modelInfoApi(@Named("modelInfo") retrofit: Retrofit): ModelInfoApi = retrofit
        .create(ModelInfoApi::class.java)
}
