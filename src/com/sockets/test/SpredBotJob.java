package com.sockets.test;

import java.util.HashMap;
import java.util.Map;

public class SpredBotJob {
    static Map<String, InfiniteTimer> timerList = new HashMap<>();
    public static void refreshTimer(String domain) {
        if (!timerList.containsKey(domain)) {
            timerList.put(domain, new InfiniteTimer("/mfm-exchange/spred.php?domain=" + domain, 1000, 1000 * 60).start());
        }
        timerList.get(domain).refresh();
    }
}
