package ru.shika.app.main.ui.activity

import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import com.pawegio.kandroid.fromApi
import kotlinx.android.synthetic.main.activity_main.drawerContents
import kotlinx.android.synthetic.main.activity_main.drawerLayout
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

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        val actionBar = supportActionBar

        drawerLayout.addDrawerListener(toggle)

        if (actionBar != null) {
            with(actionBar, {
                setHomeButtonEnabled(true)
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            })
        }

        drawerContents.setNavigationItemSelectedListener {
            drawerLayout.closeDrawers()

            val menu = drawerContents.menu
            (0 until menu.size()).forEach { menu.getItem(it).isChecked = false }

            it.isChecked = true
            when(it.itemId) {
                R.id.drawer_item_group -> openGroupFragment()
                R.id.drawer_item_teacher -> openTeacherFragment()
            }
            true
        }

        toggle.syncState()
    }
}
