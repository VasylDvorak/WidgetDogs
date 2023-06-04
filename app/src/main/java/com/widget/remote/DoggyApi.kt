package com.widget.remote

import retrofit2.http.GET

interface DoggyApi {
    @GET("random")
    suspend fun getRandoDog():DoggyResponse
}