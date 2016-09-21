package ru.shika.app.main.ui

import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import kotlinx.android.synthetic.main.main.drawerLayout
import kotlinx.android.synthetic.main.main.toolbar
import ru.shika.app.common.ui.BaseActivity
import ru.shika.mamkschedule.R

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        setMultitaskTitle()
        initView()
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

        toggle.syncState()
    }
}
