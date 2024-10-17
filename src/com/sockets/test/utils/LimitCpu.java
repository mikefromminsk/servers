package com.sockets.test.utils;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class LimitCpu {

    AtomicLong counter = new AtomicLong();

    LimitCpu(Runnable task, double loadPercent) throws InterruptedException {
        List<Thread> list = new ArrayList<>();

        for (int i = 0; i < 40; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    task.run();
                    if (Thread.interrupted())
                        break;
                    counter.incrementAndGet();
                }
            });
            thread.start();
            list.add(thread);
        }
        Thread.sleep(10000);
        counter.set(0);
        List<Long> counters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            long count = counter.get();
            counters.add(count);
            System.out.println("count " + count);
            counter.set(0);
        }

        for (Thread thread : list) {
            thread.interrupt();
        }

        list.clear();

        long avgCount = counters.stream().mapToLong(Long::longValue).sum() / counters.size();
        long targetCount = (long) (avgCount * loadPercent / 100.0);

        System.out.println("avgCount " + avgCount);
        System.out.println("targetCount " + targetCount);
        for (int i = 0; i < 40; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    task.run();
                    if (Thread.interrupted())
                        break;
                    counter.incrementAndGet();
                }
            });
            list.add(thread);
            thread.start();

            Thread.sleep(5000);
            long count = counter.incrementAndGet();
            System.out.println("count " + count / 5);
            if (count > targetCount * 5) {
                System.out.println("thread count " + (i + 1));
                break;
            }
            counter.set(0);
        }

        Thread.sleep(20000);
        for (Thread thread : list) {
            thread.interrupt();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        new LimitCpu(() -> {
            try {
                String filename = "src/test/resources/test_md5.txt";
                String checksum = "5EB63BBBE01EEED093CB22BB8F5ACDC3";
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(filename.getBytes());
                byte[] digest = md.digest();
                String myChecksum = Base64.encode(digest);
            } catch (Exception e) {
            }
        }, 40);
    }

}
