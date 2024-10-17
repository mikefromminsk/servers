package com.sockets.test.utils;


public class LimitThread {
    private final Runnable task;
    private final Counter everySec;
    private final long sleepMsPerSec;

    public LimitThread(Runnable task, Counter everySec, double loadPercent) {
        this.task = task;
        this.everySec = everySec;
        this.sleepMsPerSec = (long) (1000 * (1.0 - loadPercent / 100.0));
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                long startTime = System.currentTimeMillis();
                try {
                    Thread.sleep(sleepMsPerSec);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                long runCount = 0;
                while (true) {
                    task.run();
                    runCount++;
                    if (System.currentTimeMillis() - startTime > 1000) {
                        break;
                    }
                }
                if (everySec != null)
                    everySec.everySec(runCount);
            }
        }).start();
    }

    interface Counter {
        void everySec(long runCount);
    }

    public static long counter = 0;

    public static void main(String[] args) {
        new LimitThread(() -> {
            counter++;
        }, runCount -> {
            //System.out.println("counter " + counter.get());
            //System.out.println("runCount " + runCount);
        }, 100).start();
    }
}