package com.hatosh.utils;

import java.util.Timer;
import java.util.TimerTask;

public class InfiniteTimer {

    Timer timer;
    Callback callback;
    public final int periodMs;
    public final int stopAfterMs;
    long lastRestart = 0;

    public InfiniteTimer(Callback callback, int periodMs, int stopAfterMs) {
        this.callback = callback;
        this.periodMs = periodMs;
        this.stopAfterMs = stopAfterMs;
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void start() {
        stop();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastRestart < stopAfterMs) {
                    try {
                        callback.onTimer(InfiniteTimer.this);
                    } catch (Exception e) {
                        System.out.println("error: " + e.getMessage());
                    }
                } else {
                    timer.cancel();
                    timer = null;
                }
            }
        }, periodMs, periodMs);
    }

    public void restart() {
        this.lastRestart = System.currentTimeMillis();
        start();
    }

    public interface Callback {
        void onTimer(InfiniteTimer timer);
    }
}
