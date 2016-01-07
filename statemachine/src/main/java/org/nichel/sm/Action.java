package org.nichel.sm;

import org.nichel.sm.annotation.OnPerformAction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Action {
    private final Object target;
    private final Method[] methods;

    final Status startStatus;
    final Status finalStatus;

    public Action(final Object target, final Status s1, final Status s2) {
        this.target = target;
        this.methods = target.getClass().getDeclaredMethods();
        this.startStatus = s1;
        this.finalStatus = s2;
    }

    Status perform(final StateMachine stateMachine) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        onPerformAction(stateMachine, this);

        return finalStatus;
    }

    private Status onPerformAction(final StateMachine stateMachine, final Action action) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        for (final Method method : methods) {
            if (method.isAnnotationPresent(OnPerformAction.class)) {
                final OnPerformAction annotation = method.getAnnotation(OnPerformAction.class);

                if (annotation.enter().equals(action.startStatus.getLabel()) && annotation.exit().equals(action.finalStatus.getLabel())) {
                    method.invoke(target, stateMachine, action);
                }
            }
        }

        return action.finalStatus;
    }

    @Override
    public String toString() {
        return startStatus.toString() + " -> " + finalStatus.toString();
    }
}
