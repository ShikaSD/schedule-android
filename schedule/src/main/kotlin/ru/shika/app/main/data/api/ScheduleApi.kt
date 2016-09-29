package ru.shika.app.main.data.api

import ru.shika.app.common.api.ScheduleRestApi
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ScheduleApi @Inject constructor(
    @Named("token")
    private val token   : String,
    private val restApi : ScheduleRestApi) {

    fun getGroups() = restApi.getGroups(token)

    fun getTeachers() = restApi.getTeachers(token)
}
