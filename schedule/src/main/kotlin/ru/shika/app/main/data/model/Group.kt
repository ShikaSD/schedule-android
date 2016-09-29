package ru.shika.app.main.data.model

import io.realm.RealmObject

/**
 * Model representing group
 */
open class Group(var id: Long = 0, var name: String = "") : RealmObject()
