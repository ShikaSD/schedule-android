package ru.shika.app.common.api

import retrofit2.http.GET
import retrofit2.http.Header
import ru.shika.app.main.data.model.Group
import ru.shika.app.main.data.model.Room
import ru.shika.app.main.data.model.Teacher
import rx.Observable

/**
 * Rest api initialization
 */
interface ScheduleRestApi {

    @GET("/groups/")
    fun getGroups(@Header("X-Schedule-Id") token: String): Observable<List<Group>>

    @GET("/teachers/")
    fun getTeachers(@Header("X-Schedule-Id") token: String): Observable<List<Teacher>>

    @GET("/rooms")
    fun getRooms(@Header("X-Schedule-Id") token: String): Observable<List<Room>>
}
