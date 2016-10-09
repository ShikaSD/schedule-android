package ru.shika.app.common.di

import dagger.Subcomponent
import ru.shika.app.annotations.ActivityScope
import ru.shika.app.main.ui.activity.MainActivity
import ru.shika.app.main.ui.fragment.GroupFragment
import ru.shika.app.main.ui.fragment.RoomFragment
import ru.shika.app.main.ui.fragment.TeacherFragment

/**
 * Dagger component for activity scopes
 */
@ActivityScope
@Subcomponent(
    modules = arrayOf(ActivityModule::class)
)
interface ActivityComponent {
    fun inject(activity: MainActivity)

    fun inject(fragment: GroupFragment)

    fun inject(fragment: TeacherFragment)

    fun inject(fragment: RoomFragment)
}
