package org.nichel.sm

import android.os.Handler
import android.os.Looper
import android.util.ArrayMap
import android.util.Pair
import org.nichel.sm.annotation.OnStatusChanged
import timber.log.Timber
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

class StateMachine(_target: Any) {
    private val handler = Handler(Looper.getMainLooper())
    private val states = ArrayList<Status>()
    private val actions = ArrayMap<Pair<String?, String?>, Action>()
    private var currentStatus: Status? = null

    private val target = _target
    private val onStatusChangedMethods = ArrayList<Method>()

    init {
        for (method in target.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(OnStatusChanged::class.java)) {
                onStatusChangedMethods.add(method)
            }
        }
    }

    private fun getKey(s1: Status?, s2: Status?): Pair<String?, String?> {
        return Pair(s1?.label, s2?.label)
    }

    private fun addActions(action: Action) {
        val key = getKey(action.enterStatus, action.exitStatus)

        if (!states.contains(action.enterStatus)) {
            this.states.add(action.enterStatus)
        }

        if (!states.contains(action.exitStatus)) {
            this.states.add(action.exitStatus)
        }

        this.actions.put(key, action)
    }

    fun addAction(enterStatus: Status, exitStatus: Status) {
        addActions(Action(target, enterStatus, exitStatus))
    }

    fun setInitialStatus(status: Status) {
        if (states.contains(status)) {
            handler.post(Runnable {
                try {
                    currentStatus = status
                    currentStatus?.enter(this, null)
                    onStatusChanged(currentStatus)
                } catch (e: IllegalAccessException) {
                    Timber.e(e.message)
                } catch (e: InvocationTargetException) {
                    Timber.e(e.message)
                }
            })
        }
    }

    private fun setStatus(status: Status, `object`: Any?) {
        val key = getKey(currentStatus, status)
        val action = actions[key]

        if (action != null) {
            handler.post {
                try {
                    currentStatus?.exit(this)
                    currentStatus = action.perform(this)
                    currentStatus?.enter(this, `object`)

                    onStatusChanged(currentStatus)
                } catch (e: IllegalAccessException) {
                    Timber.e(e.message)
                } catch (e: InvocationTargetException) {
                    Timber.e(e.message)
                }
            }
        }
    }

    fun setStatus(label: String, `object`: Any) {
        for (status in states) {
            if (status.label == label) {
                setStatus(status, `object`)
                break
            }
        }
    }

    fun setStatus(label: String) {
        for (status in states) {
            if (status.label == label) {
                setStatus(status, null)
                break
            }
        }
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    private fun onStatusChanged(status: Status?) {
        for (method in onStatusChangedMethods) {
            method.invoke(target, this, status)
        }
    }

    fun getCurrentStatus(): Status? {
        return currentStatus
    }

    fun isOnStatus(status: String): Boolean {
        return status == currentStatus?.label
    }
}