package ru.shika


/**
 * Base application class
 */
class Application() : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
    }
}
