package ru.shika.app.common.ui

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import ru.shika.app.common.presenter.BasePresenter

/**
 * Base class for [Fragment]
 */
abstract class BaseFragment() : Fragment() {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getPresenter().create()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()

        getPresenter().destroy()
    }

    abstract fun getPresenter(): BasePresenter<*>
}
