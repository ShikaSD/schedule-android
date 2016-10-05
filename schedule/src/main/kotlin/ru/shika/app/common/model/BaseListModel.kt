package ru.shika.app.common.model

/**
 * Contract for base model to be displayed through [SimpleListAdapter]
 */
interface BaseListModel {

    fun getLabel(): String

    fun getInfo(): String
}
