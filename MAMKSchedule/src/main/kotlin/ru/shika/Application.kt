package ru.shika

import com.jakewharton.threetenabp.AndroidThreeTen


/**
 * Base application class
 */
class Application() : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
    }
}
