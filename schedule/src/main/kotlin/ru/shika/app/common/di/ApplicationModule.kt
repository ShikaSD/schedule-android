package ru.shika.app.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

/**
 * Default dagger module to provide dependencies application-wide
 */
@Module
@Singleton
class ApplicationModule(private val context: Context) {

    @Provides
    fun provideContext() = context

    @Provides
    @Named("token")
    fun provideToken() = UUID.randomUUID()
}
