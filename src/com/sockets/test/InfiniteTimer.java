package com.sockets.test;

import java.util.Timer;
import java.util.TimerTask;

public class InfiniteTimer {
    public static void start() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Backend.post("mfm-exchange/bot/job.php", null, response -> {
                }, error -> {
                    System.out.println(error);
                });
            }
        }, 0, 1000);
        System.out.println("InfiniteTimer started");
    }
}
