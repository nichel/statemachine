package org.nichel.sm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Pair;

import org.nichel.sm.annotation.OnStatusChanged;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import timber.log.Timber;

public class StateMachine {
    public final String label;

    private final Object target;
    private final Method[] methods;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<Status> states = new ArrayList<>();
    private final ArrayMap<Pair<String, String>, Action> actions = new ArrayMap<>();

    private Status currentStatus;

    public StateMachine(final Object target, final String label) {
        this.target = target;
        this.methods = target.getClass().getDeclaredMethods();
        this.label = label;
        this.currentStatus = null;
    }

    public void addActions(final Action... actions) {
        for (final Action action : actions) {
            final Pair<String, String> key = getKey(action.startStatus, action.finalStatus);

            if (!states.contains(action.startStatus)) {
                this.states.add(action.startStatus);
            }

            if (!states.contains(action.finalStatus)) {
                this.states.add(action.finalStatus);
            }

            this.actions.put(key, action);
        }
    }

    public void addAction(final Status startStatus, final Status finalStatus) {
        addActions(new Action(target, startStatus, finalStatus));
    }

    public void setInitialState(final Status state) {
        if (states.contains(state)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        currentStatus = state;
                        currentStatus.enter(StateMachine.this, null);

                        onStatusChanged(currentStatus);
                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        Timber.e(e.getMessage());
                    }
                }
            });
        }
    }

    private void setState(final Status status, final Bundle bundle) {
        final Pair<String, String> key = getKey(currentStatus, status);
        final Action action = actions.get(key);

        if (action != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        currentStatus.exit(StateMachine.this);
                        currentStatus = action.perform(StateMachine.this);
                        currentStatus.enter(StateMachine.this, bundle);

                        onStatusChanged(currentStatus);
                    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        Timber.e(e.getMessage());
                    }
                }
            });
        }
    }

    public void setState(final String label, final Bundle bundle) {
        for (final Status state : states) {
            if (state.getLabel().equals(label)) {
                setState(state, bundle);
                break;
            }
        }
    }

    public void setState(final String label) {
        for (final Status state : states) {
            if (state.getLabel().equals(label)) {
                setState(state, null);
                break;
            }
        }
    }

    private Pair<String, String> getKey(final Status s1, final Status s2) {
        return new Pair<>(s1.getLabel(), s2.getLabel());
    }

    @Override
    public String toString() {
        return label;
    }

    private void onStatusChanged(final Status status) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        for (final Method method : methods) {
            if (method.isAnnotationPresent(OnStatusChanged.class)) {
                method.invoke(target, this, status);
            }
        }
    }
}
