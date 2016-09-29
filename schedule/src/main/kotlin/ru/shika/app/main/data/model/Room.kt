package ru.shika.app.main.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Model representing room
 */
open class Room(
    @PrimaryKey
    var id          : Long = 0,
    var name        : String = "",
    var description : String= ""
) : RealmObject()
