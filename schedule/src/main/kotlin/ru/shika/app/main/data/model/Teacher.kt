package ru.shika.app.main.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Model for teacher
 */
open class Teacher(@PrimaryKey var id: Long = 0, name: String = "") : RealmObject()
