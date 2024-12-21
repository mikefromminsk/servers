package com.hatosh.utils;

import java.util.Timer;
import java.util.TimerTask;

public class InfiniteTimer {

    Timer timer;
    Callback callback;
    int interval;
    int skipCallsAfterMs;
    long lastRestart = 0;

    public InfiniteTimer(Callback callback, int intervalMs, int skipCallsAfterMs) {
        this.callback = callback;
        this.interval = intervalMs;
        this.skipCallsAfterMs = skipCallsAfterMs;
    }

    private InfiniteTimer start() {
        stop();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastRestart < skipCallsAfterMs) {
                    callback.onTimer();
                }
            }
        }, 0, interval);
        return this;
    }

    public void restart() {
        if (timer == null){
            start();
        }
        this.lastRestart = System.currentTimeMillis();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public interface Callback{
        void onTimer();
    }
}
