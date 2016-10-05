package ru.shika.app.common.ui

import android.annotation.TargetApi
import android.app.ActivityManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import ru.shika.Application
import ru.shika.mamkschedule.R

/**
 * Basic activity application-wide
 */
open class BaseActivity() : AppCompatActivity() {

    protected fun getApp() = application as Application

    @TargetApi(21)
    protected fun setMultitaskTitle(
        title  : String  = getString(R.string.app_name),
        bitmap : Bitmap  = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher),
        color  : Int     = getColor(R.color.blue)
    ) = setTaskDescription(ActivityManager.TaskDescription(title, bitmap, color))

    protected fun replaceFragment(newFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mainContainer, newFragment)
        transaction.commit()
    }
}
