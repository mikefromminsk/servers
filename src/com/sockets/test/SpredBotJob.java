package com.sockets.test;

import java.util.HashMap;
import java.util.Map;

public class SpredBotJob {
    static Map<String, InfiniteTimer> minTimers = new HashMap<>();
    static Map<String, InfiniteTimer> dayTimers = new HashMap<>();
    static Map<String, InfiniteTimer> weekTimers = new HashMap<>();
    public static void refreshTimer(String domain) {
        if (!minTimers.containsKey(domain)) {
            minTimers.put(domain, new InfiniteTimer("/mfm-exchange/spred.php?domain=" + domain, 1000, 1000 * 60).start());
            dayTimers.put(domain, new InfiniteTimer("/mfm-exchange/spred.php?domain=" + domain, 1000 * 60, 1000 * 60 * 60 * 24).start());
            weekTimers.put(domain, new InfiniteTimer("/mfm-exchange/spred.php?domain=" + domain, 1000 * 60 * 60 * 24, 1000 * 60 * 60 * 24 * 7).start());
        }
        minTimers.get(domain).restart();
        dayTimers.get(domain).restart();
        weekTimers.get(domain).restart();
    }
}
