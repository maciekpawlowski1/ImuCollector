package com.pawlowski.imucollector.di

import com.pawlowski.imucollector.data.API
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun retrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("http://srv3.enteam.pl:4301")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    fun api(retrofit: Retrofit): API = retrofit
        .create(API::class.java)
}