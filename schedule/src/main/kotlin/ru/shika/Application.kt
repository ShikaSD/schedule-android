package ru.shika

import com.jakewharton.threetenabp.AndroidThreeTen
import io.realm.Realm
import io.realm.RealmConfiguration
import ru.shika.app.common.di.ApplicationComponent
import ru.shika.app.common.di.ApplicationModule
import ru.shika.app.common.di.DaggerApplicationComponent
import ru.shika.mamkschedule.BuildConfig


/**
 * Base application class
 */
class Application() : android.app.Application() {

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(BuildConfig.VERSION_CODE.toLong())
                .build()
        )
        appComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}
