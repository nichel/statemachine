package org.nichel.sm

import org.nichel.sm.annotation.OnEnterStatus
import org.nichel.sm.annotation.OnExitStatus
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

public class Status(_target: Any, _label: String) {
    public val label = _label
    private val target = _target
    private val onEnterMethods = ArrayList<Method>()
    private val onExitMethods  = ArrayList<Method>()

    init {
        for (method in target.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(OnEnterStatus::class.java)) {
                val annotation = method.getAnnotation(OnEnterStatus::class.java)

                for (value in annotation.value) {
                    if (value == label) {
                        onEnterMethods.add(method)
                    }
                }
            } else if (method.isAnnotationPresent(OnExitStatus::class.java)) {
                val annotation = method.getAnnotation(OnExitStatus::class.java)

                for (value in annotation.value) {
                    if (value == label) {
                        onExitMethods.add(method)
                    }
                }
            }
        }
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    private fun onEnterStatus(stateMachine: StateMachine, status: Status, `object`: Any?) {
        for (method in onEnterMethods) {
            val params = method.genericParameterTypes

            if (params.size == 2) {
                method.invoke(target, stateMachine, status)
            } else if (params.size == 3) {
                method.invoke(target, stateMachine, status, `object`)
            }
        }
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    private fun onExitStatus(stateMachine: StateMachine, status: Status) {
        for (method in onExitMethods) {
            method.invoke(target, stateMachine, status)
        }
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    internal fun enter(stateMachine: StateMachine, `object`: Any?) {
        onEnterStatus(stateMachine, this, `object`)
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    internal fun exit(stateMachine: StateMachine) {
        onExitStatus(stateMachine, this)
    }
}