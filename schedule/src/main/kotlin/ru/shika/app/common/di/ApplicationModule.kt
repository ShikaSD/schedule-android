package ru.shika.app.common.di

import android.content.Context
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.shika.app.common.api.ScheduleRestApi
import ru.shika.mamkschedule.BuildConfig
import rx.internal.util.SubscriptionList
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

/**
 * Default dagger module to provide dependencies application-wide
 */
@Module
class ApplicationModule(private val context: Context) {

    init {
        Realm.init(context)
        AndroidThreeTen.init(context)
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(BuildConfig.VERSION_CODE.toLong())
                .build())
    }

    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    fun provideSubscriptionList() = SubscriptionList()

    @Provides
    @Singleton
    @Named("token")
    fun provideToken() = UUID.randomUUID().toString()

    @Provides
    @Singleton
    fun provideGson() = GsonBuilder()
        .setExclusionStrategies(object : ExclusionStrategy {
            override fun shouldSkipClass(clazz: Class<*>?) = false
            override fun shouldSkipField(f: FieldAttributes?) = f?.declaredClass == RealmObject::class.java
        })
        .create()

    @Provides
    @Singleton
    fun provideRestApi(gson: Gson) = Retrofit.Builder()
        .baseUrl(BuildConfig.API_ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build()
        .create(ScheduleRestApi::class.java)
}
