package ru.shika.app.main.ui.presenter

import ru.shika.app.annotations.ActivityScope
import ru.shika.app.common.presenter.BasePresenter
import ru.shika.app.main.data.ScheduleRepository
import ru.shika.app.main.ui.view.GroupFragmentView
import rx.internal.util.SubscriptionList
import javax.inject.Inject

/**
 * Presenter for [GroupFragment]
 */
@ActivityScope
class GroupFragmentPresenter @Inject constructor(
    private val repo: ScheduleRepository,
    helper: SubscriptionList
) : BasePresenter<GroupFragmentView>(helper) {

    fun loadGroups() = helper.add(
        repo.getGroups().subscribe({
            view.showGroups(it)
        }, {
            view.showError(it)
        })
    )
}
