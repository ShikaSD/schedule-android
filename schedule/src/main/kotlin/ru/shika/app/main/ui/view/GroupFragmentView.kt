package ru.shika.app.main.ui.view

import ru.shika.app.common.presenter.BaseView
import ru.shika.app.main.data.model.Group

/**
 * View for [GroupFragment]
 */
interface GroupFragmentView : BaseView {

    fun showGroups(groups: List<Group>): Unit

    fun showError(it: Throwable)
}
