package org.nichel.statemachine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.nichel.sm.Action;
import org.nichel.sm.StateMachine;
import org.nichel.sm.Status;
import org.nichel.sm.annotation.OnEnterStatus;
import org.nichel.sm.annotation.OnExitStatus;
import org.nichel.sm.annotation.OnPerformAction;
import org.nichel.sm.annotation.OnStatusChanged;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final String STATUS_ON = "ON";
    private static final String STATUS_OFF = "OFF";

    private StateMachine sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.i("onCreate");

        final Status statusOn = new Status(this, this, STATUS_ON);
        final Status statusOff = new Status(this, this, STATUS_OFF);

        sm = new StateMachine(this, "MAIN");
        sm.addAction(statusOn, statusOff);
        sm.addAction(statusOff, statusOn);
        sm.setInitialState(statusOff);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.click)
    protected void onClick() {
        Timber.i("onClick");

        sm.setState(STATUS_ON);
    }

    @OnEnterStatus({STATUS_ON, STATUS_OFF})
    public void onEnter(final StateMachine stateMachine, final Status status) {
        Timber.i("super enter: %s - [%s]", stateMachine, status);
    }

    @OnEnterStatus(STATUS_ON)
    public void onEnterOn(final StateMachine stateMachine, final Status status) {
        Timber.i("enter: %s - [%s]", stateMachine, status);

        sm.setState(STATUS_OFF);
    }

    @OnEnterStatus(STATUS_OFF)
    public void onEnterOff(final StateMachine stateMachine, final Status status) {
        Timber.i("enter: %s - [%s]", stateMachine, status);
    }

    @OnExitStatus({STATUS_OFF, STATUS_ON})
    public void OnExitOn(final StateMachine stateMachine, final Status status) {
        Timber.i("exit: %s - [%s]", stateMachine, status);
    }

    @OnPerformAction(enter = STATUS_ON, exit = STATUS_OFF)
    public void onPerformActionOnOff(final StateMachine stateMachine, final Action action) {
        Timber.i("action: %s - [%s]", stateMachine, action);
    }

    @OnStatusChanged
    public void onStatusChanged(final StateMachine stateMachine, Status status) {
        Timber.i("status changed: %s - [%s]", stateMachine, status);
    }
}
