package ru.shika.app.common.di

import dagger.Component
import ru.shika.app.main.ui.activity.MainActivity
import javax.inject.Singleton

/**
 * Default dagger component
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun plus(activityModule: ActivityModule): ActivityComponent

    fun inject(activity: MainActivity)
}
