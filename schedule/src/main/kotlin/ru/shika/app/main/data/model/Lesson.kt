package ru.shika.app.main.data.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Model for one lesson
 */
open class Lesson(
    @PrimaryKey
    var id       : Long               = 0,
    var courseId : String             = "",
    var name     : String             = "",
    var group    : String             = "",
    var teachers : RealmList<Teacher> = RealmList(),
    var rooms    : RealmList<Room> = RealmList(),
    var start    : Date = Date(),
    var end      : Date = Date()
) : RealmObject()
