package com.opsc.solowork_1.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    companion object {
        const val BASE_URL = "https://68ee427df2025af78030075.mockapi.io/api/v1/"
    }

    @GET("events")
    suspend fun getEvents(): Response<List<MockEvent>>

    @POST("events")
    suspend fun createEvent(@Body event: MockEvent): Response<MockEvent>

    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") id: String, @Body event: MockEvent): Response<MockEvent>

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String): Response<Unit>
}