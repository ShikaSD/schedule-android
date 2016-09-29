package ru.shika.app.common.presenter

import rx.internal.util.SubscriptionList

/**
 * Base class for presenter
 */
abstract class BasePresenter<T : BaseView>(protected val helper: SubscriptionList) {

    lateinit var view: T

    open fun create() { }

    open fun destroy() {
        helper.unsubscribe()
    }
}

interface BaseView
