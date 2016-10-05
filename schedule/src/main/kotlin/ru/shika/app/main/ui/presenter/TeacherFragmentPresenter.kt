package ru.shika.app.main.ui.presenter

import ru.shika.app.annotations.ActivityScope
import ru.shika.app.common.presenter.BasePresenter
import ru.shika.app.main.data.ScheduleRepository
import ru.shika.app.main.ui.view.TeacherFragmentView
import rx.internal.util.SubscriptionList
import javax.inject.Inject

/**
 * Presenter for [GroupFragment]
 */
@ActivityScope
class TeacherFragmentPresenter @Inject constructor(
    private val repo: ScheduleRepository,
    helper: SubscriptionList
) : BasePresenter<TeacherFragmentView>(helper) {

    fun loadTeachers() {
        view.showProgress()

        helper.add(
            repo.getTeachers().subscribe({
                view.showTeachers(it)
            }, {
                view.showError(it)
            }, {
                view.hideProgress()
            })
        )
    }
}
