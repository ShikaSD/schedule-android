package ru.shika.app.common.di

import dagger.Component
import ru.shika.app.main.ui.MainActivity
import javax.inject.Singleton

/**
 * Default dagger component
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(activity: MainActivity)
}
