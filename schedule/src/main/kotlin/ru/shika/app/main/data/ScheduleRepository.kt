package ru.shika.app.main.data

import ru.shika.app.annotations.ActivityScope
import ru.shika.app.common.repo.BaseRepository
import ru.shika.app.main.data.api.ScheduleApi
import ru.shika.app.main.data.model.Group
import rx.Observable
import javax.inject.Inject

/**
 * Handling data operations
 */
@ActivityScope
class ScheduleRepository @Inject constructor(
    private val api: ScheduleApi
) : BaseRepository() {

    fun getGroups() = getGroupsFromCache()
        .mergeWith(getGroupsFromNetwork())
        .applySchedulers()

    private fun getGroupsFromCache() = Observable.fromCallable {
        withRealm { realm ->
            val results = realm.where(Group::class.java).findAll()
            realm.copyFromRealm(results)
        }
    }

    private fun getGroupsFromNetwork() = api.getGroups()
        .doOnNext { models ->
            withRealm { realm ->
                realm.executeTransaction {
                    it.delete(Group::class.java)
                    it.insertOrUpdate(models)
                }
            }
        }
}
