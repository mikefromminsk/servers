package com.mfm_wallet.mfm_analytics;

import com.mfm_wallet.Contract;

import java.util.Map;

public class Events extends Contract {
    @Override
    protected void run() {
        String app = getRequired("app");
        String name = getRequired("name");
        String value = getRequired("value");
        Long size = getLong("size", 10L);
        Long from = getLong("from", time() - 60 * 60 * 24 * 7);

        response.put("events", getEvents(app, name, value, null, from, size));
    }
}
