package ru.shika

import com.jakewharton.threetenabp.AndroidThreeTen
import ru.shika.app.common.di.ApplicationComponent
import ru.shika.app.common.di.ApplicationModule
import ru.shika.app.common.di.DaggerApplicationComponent


/**
 * Base application class
 */
class Application() : android.app.Application() {

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
        appComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}
