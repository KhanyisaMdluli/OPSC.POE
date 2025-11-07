package com.opsc.solowork_1.api

import com.google.gson.annotations.SerializedName

data class MockEvent(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("eventType")
    val eventType: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("startTime")
    val startTime: Long,

    @SerializedName("endTime")
    val endTime: Long,

    @SerializedName("isAllDay")
    val isAllDay: Boolean,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("createdAt")
    val createdAt: String? = null
)