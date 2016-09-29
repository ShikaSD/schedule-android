package ru.shika.app.common.di

import android.app.Activity
import dagger.Module
import dagger.Provides
import ru.shika.app.annotations.ActivityScope

/**
 * Activity module providing injections
 */
@Module
class ActivityModule(private val activity: Activity) {
    @Provides
    @ActivityScope
    fun provideActivity() = activity
}
