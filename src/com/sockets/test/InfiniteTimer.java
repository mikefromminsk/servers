package com.sockets.test;

import java.util.Timer;
import java.util.TimerTask;

public class InfiniteTimer {

    String path;

    public InfiniteTimer(String path) {
        this.path = path;
    }

    public void start() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Backend.postToLocalhost(path);
            }
        }, 0, 1000);
        System.out.println("InfiniteTimer started: " + path);
    }
}
