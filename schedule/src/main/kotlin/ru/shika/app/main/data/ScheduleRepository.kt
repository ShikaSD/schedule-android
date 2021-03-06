package ru.shika.app.main.data

import ru.shika.app.annotations.ActivityScope
import ru.shika.app.common.repo.BaseRepository
import ru.shika.app.main.data.api.ScheduleApi
import ru.shika.app.main.data.model.Group
import ru.shika.app.main.data.model.Room
import ru.shika.app.main.data.model.Teacher
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
        .map { it.sortedBy { it.name } }
        .distinctUntilChanged()
        .applySchedulers()

    fun getTeachers() = getTeachersFromCache()
        .mergeWith(getTeachersFromNetwork())
        .map { it.sortedBy { it.name } }
        .distinctUntilChanged()
        .applySchedulers()

    fun getRooms() = getRoomsFromCache()
        .mergeWith(getRoomsFromNetwork())
        .map { it.sortedBy { it.name } }
        .distinctUntilChanged()
        .applySchedulers()

    private fun getTeachersFromCache() = Observable.fromCallable {
        withRealm { realm ->
            realm.copyFromRealm(realm.where(Teacher::class.java).findAll())
        }
    }

    private fun getTeachersFromNetwork() = api.getTeachers()
        .doOnNext { models ->
            withRealm { realm ->
                realm.executeTransaction {
                    it.delete(Teacher::class.java)
                    it.insertOrUpdate(models)
                }
            }
        }

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

    private fun getRoomsFromCache() = Observable.fromCallable {
        withRealm { realm ->
            realm.copyFromRealm(realm.where(Room::class.java).findAll())
        }
    }

    private fun getRoomsFromNetwork() = api.getRooms()
        .doOnNext { models ->
            withRealm { realm ->
                realm.executeTransaction {
                    it.delete(Room::class.java)
                    it.insertOrUpdate(models)
                }
            }
        }
}
