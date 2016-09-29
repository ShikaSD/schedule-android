package ru.shika.app.common.repo

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * Basic class for chaining cache and network calls
 */
open class BaseRepository() {

    protected inline fun <T> withRealm(action: (Realm) -> T) = Realm.getDefaultInstance().use(action)

    fun <T> Observable<T>.applySchedulers() = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun <E : RealmModel> Observable<RealmResults<E>>.filterValid() = this
        .filter { it.isLoaded && it.isValid }

    fun <E : RealmModel> Observable<RealmResults<E>>.copyFromRealm() = this
        .map { results ->
            withRealm { realm ->
                realm.copyFromRealm(results)
            }
        }


}
