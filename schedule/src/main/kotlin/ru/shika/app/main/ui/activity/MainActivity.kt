package ru.shika.app.main.ui.activity

import android.os.Bundle
import com.pawegio.kandroid.fromApi
import kotlinx.android.synthetic.main.activity_main.bottomNavigation
import kotlinx.android.synthetic.main.activity_main.toolbar
import ru.shika.app.annotations.ActivityScope
import ru.shika.app.common.di.ActivityComponent
import ru.shika.app.common.di.ActivityModule
import ru.shika.app.common.ui.BaseActivity
import ru.shika.app.main.ui.fragment.GroupFragment
import ru.shika.app.main.ui.fragment.RoomFragment
import ru.shika.app.main.ui.fragment.TeacherFragment
import ru.shika.mamkschedule.R

@ActivityScope
class MainActivity : BaseActivity() {

    var activityComponent: ActivityComponent? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        injectDependencies()

        fromApi(21) {
            setMultitaskTitle()
        }
        initView()
    }

    override fun onStart() {
        super.onStart()

        if(activityComponent == null) {
            injectDependencies()
        }
    }

    override fun onStop() {
        super.onStop()
        releaseComponent()
    }

    private fun openGroupFragment() {
        replaceFragment(GroupFragment())
    }

    private fun openTeacherFragment() {
        replaceFragment(TeacherFragment())
    }

    private fun openRoomFragment() {
        replaceFragment(RoomFragment())
    }

    private fun injectDependencies() {
        activityComponent = getApp().appComponent.plus(ActivityModule(this))
        activityComponent?.inject(this)
    }

    private fun releaseComponent() {
        activityComponent = null
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_item_group   -> openGroupFragment()
                R.id.bottom_item_teacher -> openTeacherFragment()
                R.id.bottom_item_room    -> openRoomFragment()
            }

            true
        }
    }
}
