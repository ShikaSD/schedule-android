package ru.shika.app.main.ui.view

import ru.shika.app.common.presenter.BaseView
import ru.shika.app.main.data.model.Room

/**
 * View interface for [RoomFragment] to link with the presenter
 */
interface RoomFragmentView : BaseView {

    fun showRooms(rooms: List<Room>): Unit

    fun showError(it: Throwable)

    fun showProgress()

    fun hideProgress()
}
