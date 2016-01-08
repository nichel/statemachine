package org.nichel.sm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import org.nichel.sm.annotation.OnEnterStatus;
import org.nichel.sm.annotation.OnExitStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import timber.log.Timber;

public class Status {
    private final String label;

    private final Object target;
    private final Method[] methods;

    private final NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 0;
    private Notification notification;

    public Status(final Object target, final Context context, final String label) {
        this.label = label;
        this.target = target;
        this.methods = target.getClass().getDeclaredMethods();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notification = null;
    }

    public String getLabel() {
        return label;
    }

    public void setNotification(final Notification notification) {
        this.notification = notification;
    }

    void enter(final StateMachine stateMachine, final Bundle bundle) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (notification != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        onEnterStatus(stateMachine, this, bundle);
    }

    void exit(final StateMachine stateMachine) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (notification != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }

        onExitStatus(stateMachine, this);
    }

    private void onEnterStatus(final StateMachine stateMachine, final Status status, final Bundle bundle) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        for (final Method method : methods) {
            if (method.isAnnotationPresent(OnEnterStatus.class)) {
                final OnEnterStatus annotation = method.getAnnotation(OnEnterStatus.class);

                for (final String label : annotation.value()) {
                    if (label.equals(status.label)) {
                        final int params = method.getGenericParameterTypes().length;

                        if (params == 2) {
                            method.invoke(target, stateMachine, status);
                        } else if (params == 3) {
                            method.invoke(target, stateMachine, status, bundle);
                        }
                    }
                }
            }
        }
    }

    private void onExitStatus(final StateMachine stateMachine, final Status status) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        for (final Method method : methods) {
            if (method.isAnnotationPresent(OnExitStatus.class)) {
                final OnExitStatus annotation = method.getAnnotation(OnExitStatus.class);

                for (final String label : annotation.value()) {
                    if (label.equals(status.label)) {
                        method.invoke(target, stateMachine, status);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
