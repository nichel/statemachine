package org.nichel.sm

import org.nichel.sm.annotation.OnPerformAction
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

public class Action(_target: Any, _enterStatus: Status, _exitStatus: Status) {
    private val target = _target
    internal val enterStatus = _enterStatus
    internal val exitStatus =_exitStatus

    private val onPerformMethods = ArrayList<Method>()

    init {
        for (method in target.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(OnPerformAction::class.java)) {
                val annotation = method.getAnnotation(OnPerformAction::class.java)

                if (annotation.enter == enterStatus.label && annotation.exit == exitStatus.label) {
                    onPerformMethods.add(method)
                }
            }
        }
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    private fun onPerformAction(stateMachine: StateMachine, action: Action): Status {
        for (method in onPerformMethods) {
            method.invoke(target, stateMachine, action)
        }

        return action.exitStatus
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    internal fun perform(stateMachine: StateMachine): Status {
        return onPerformAction(stateMachine, this)
    }
}
