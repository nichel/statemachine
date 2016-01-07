package org.nichel.statemachine;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by nichel on 1/7/16.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
