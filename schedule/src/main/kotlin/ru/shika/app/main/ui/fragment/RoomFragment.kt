package ru.shika.app.main.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_list.listView
import kotlinx.android.synthetic.main.fragment_list.refreshLayout
import ru.shika.app.annotations.ActivityScope
import ru.shika.app.common.adapter.SimpleListAdapter
import ru.shika.app.common.ui.BaseFragment
import ru.shika.app.main.data.model.Group
import ru.shika.app.main.data.model.Room
import ru.shika.app.main.ui.activity.MainActivity
import ru.shika.app.main.ui.presenter.RoomFragmentPresenter
import ru.shika.app.main.ui.view.RoomFragmentView
import ru.shika.mamkschedule.R
import javax.inject.Inject

/**
 * Fragment showing the list of [Group]
 */
@ActivityScope
class RoomFragment() : BaseFragment(), RoomFragmentView {

    @Inject lateinit var roomPresenter: RoomFragmentPresenter

    private lateinit var teacherListAdapter: SimpleListAdapter<Room>

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()

        super.onCreate(savedInstanceState)

        getPresenter().view = this
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_list, null)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initView()

        roomPresenter.loadRooms()
    }

    private fun initView() {
        listView.layoutManager = LinearLayoutManager(activity)
        teacherListAdapter = SimpleListAdapter(activity)
        listView.adapter = teacherListAdapter
        refreshLayout.setOnRefreshListener { roomPresenter.loadRooms() }
    }

    private fun injectDependencies() {
        (activity as MainActivity).activityComponent?.inject(this)
    }

    override fun getPresenter() = roomPresenter

    override fun showRooms(rooms: List<Room>) {
        teacherListAdapter.setItems(rooms)
    }

    override fun showError(it: Throwable) {
        hideProgress()
    }

    override fun showProgress() {
        refreshLayout.isRefreshing = true
    }

    override fun hideProgress() {
        refreshLayout.isRefreshing = false
    }
}

