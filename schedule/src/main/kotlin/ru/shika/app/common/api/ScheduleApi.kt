package ru.shika.app.common.api

import retrofit2.http.GET
import retrofit2.http.Header

/**
 * Rest api initialization
 */
interface ScheduleApi {

    @GET("/groups/")
    fun getGroups(@Header("X-Schedule-Token") token: String)

    @GET("/teachers/")
    fun getTeachers(@Header("X-Schedule-Token") token: String)
}
