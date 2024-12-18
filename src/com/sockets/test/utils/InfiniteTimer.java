package com.sockets.test.utils;

import java.util.Timer;
import java.util.TimerTask;

public class InfiniteTimer {

    Timer timer;
    String path;
    int interval;
    int skipCallsAfterMs;
    long lastRestart = 0;

    public InfiniteTimer(String path, int intervalMs) {
        this(path, intervalMs, -1000);
    }

    public InfiniteTimer(String path, int intervalMs, int skipCallsAfterMs) {
        this.path = path;
        this.interval = intervalMs;
        this.skipCallsAfterMs = skipCallsAfterMs;
        System.out.println("Timer: " + path);
    }

    public InfiniteTimer start() {
        stop();
        restart();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastRestart < skipCallsAfterMs) {
                    System.out.println("call: " + path + " - " + System.currentTimeMillis());
                    Request.post(path);
                }
            }
        }, 0, interval);
        return this;
    }

    public void restart() {
        this.lastRestart = System.currentTimeMillis();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            System.out.println("Timer stopped: " + path);
        }
    }
}
