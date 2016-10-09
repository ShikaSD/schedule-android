package ru.shika.app.main.ui.presenter

import ru.shika.app.annotations.ActivityScope
import ru.shika.app.common.presenter.BasePresenter
import ru.shika.app.main.data.ScheduleRepository
import ru.shika.app.main.ui.view.RoomFragmentView
import rx.internal.util.SubscriptionList
import javax.inject.Inject

/**
 * Presenter for [GroupFragment]
 */
@ActivityScope
class RoomFragmentPresenter @Inject constructor(
    private val repo: ScheduleRepository,
    helper: SubscriptionList
) : BasePresenter<RoomFragmentView>(helper) {

    fun loadRooms() {
        view.showProgress()

        helper.add(
            repo.getRooms().subscribe({
                view.showRooms(it)
            }, {
                view.showError(it)
            }, {
                view.hideProgress()
            })
        )
    }
}
