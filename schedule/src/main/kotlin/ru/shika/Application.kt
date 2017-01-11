package ru.shika

import ru.shika.app.common.di.ApplicationComponent
import ru.shika.app.common.di.ApplicationModule
import ru.shika.app.common.di.DaggerApplicationComponent


/**
 * Base application class
 */
class Application : android.app.Application() {

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}
