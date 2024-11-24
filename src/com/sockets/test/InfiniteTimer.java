package com.sockets.test;

import java.util.Timer;
import java.util.TimerTask;

public class InfiniteTimer {

    Timer timer;
    String path;
    int interval;
    int refreshInterval;
    long lastRefresh = 0;

    public InfiniteTimer(String path, int intervalMs, int refreshInterval) {
        this.path = path;
        this.interval = intervalMs;
        this.refreshInterval = refreshInterval;
        System.out.println("Timer: " + path);
    }

    public InfiniteTimer start() {
        stop();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastRefresh < refreshInterval) {
                    System.out.println("call: " + path + " - " + System.currentTimeMillis());
                    Backend.postToLocalhost(path);
                }
            }
        }, 0, interval);
        return this;
    }

    public void refresh() {
        this.lastRefresh = System.currentTimeMillis();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            System.out.println("Timer stopped: " + path);
        }
    }
}
