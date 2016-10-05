package ru.shika.app.main.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import ru.shika.app.common.model.BaseListModel

/**
 * Model representing group
 */
open class Group(@PrimaryKey var id: Long = 0, var name: String = "") : RealmObject(), BaseListModel {

    override fun getInfo() = ""

    override fun getLabel() = name
}
