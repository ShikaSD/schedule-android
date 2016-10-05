package ru.shika.app.main.ui.view

import ru.shika.app.common.presenter.BaseView
import ru.shika.app.main.data.model.Teacher

/**
 * View for [TeacherFragment]
 */
interface TeacherFragmentView : BaseView {

    fun showTeachers(teachers: List<Teacher>): Unit

    fun showError(it: Throwable)

    fun showProgress()

    fun hideProgress()
}
