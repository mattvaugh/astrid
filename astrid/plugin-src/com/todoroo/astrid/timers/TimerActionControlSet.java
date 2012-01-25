package com.todoroo.astrid.timers;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;

import com.timsu.astrid.R;
import com.todoroo.andlib.utility.DateUtilities;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.helper.TaskEditControlSet;

public class TimerActionControlSet extends TaskEditControlSet {

    private final Button timerButton;
    private final Chronometer chronometer;
    private final View timerContainer;
    private boolean timerActive;
    private final Activity activity;
    private Task task;
    private final List<TimerStoppedListener> listeners = new LinkedList<TimerStoppedListener>();

    public TimerActionControlSet(Activity activity, View parent) {
        super(activity, -1);
        this.activity = activity;
        timerButton = (Button) parent.findViewById(R.id.timer_button);

        timerContainer = (View) parent.findViewById(R.id.timer_container);
        timerContainer.setOnClickListener(timerListener);

        chronometer = (Chronometer) parent.findViewById(R.id.timer);
    }

    @Override
    @SuppressWarnings("hiding")
    public void readFromTask(Task task) {
        if (task.getValue(Task.TIMER_START) == 0)
            timerActive = false;
        else
            timerActive = true;

        this.task = task;
        updateDisplay();
    }

    @Override
    @SuppressWarnings("hiding")
    public String writeToModel(Task task) {
        // Nothing to do here
        return null;
    }

    private final OnClickListener timerListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (timerActive) {
                TimerPlugin.updateTimer(activity, task, false);

                for(TimerStoppedListener listener : listeners)
                    listener.timerStopped(task);
                chronometer.stop();
            } else {
                TimerPlugin.updateTimer(activity, task, true);
                for(TimerStoppedListener listener : listeners)
                    listener.timerStarted(task);
                chronometer.start();
            }
            timerActive = !timerActive;
            updateDisplay();
        }
    };

    private void updateDisplay() {
        final int drawable;
        if(timerActive) {
            drawable = R.drawable.icn_timer_stop;
        } else {
            if (task.getValue(Task.ELAPSED_SECONDS) == 0)
                drawable = R.drawable.icn_edit_timer;
            else
                drawable = R.drawable.icn_timer_start;
        }
        timerButton.setBackgroundResource(drawable);


        long elapsed = task.getValue(Task.ELAPSED_SECONDS) * 1000L;
        if (timerActive) {
            chronometer.setVisibility(View.VISIBLE);
            elapsed += DateUtilities.now() - task.getValue(Task.TIMER_START);
            chronometer.setBase(SystemClock.elapsedRealtime() - elapsed);
            chronometer.start();
        } else {
            chronometer.setVisibility(View.INVISIBLE);
            chronometer.stop();
        }
    }

    public interface TimerStoppedListener {
        public void timerStopped(Task task);
        public void timerStarted(Task task);
    }

    public void addListener(TimerStoppedListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(TimerStoppedListener listener) {
        if (listeners.contains(listener))
            listeners.remove(listener);
    }
}
