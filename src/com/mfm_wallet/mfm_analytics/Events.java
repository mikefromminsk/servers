package com.mfm_wallet.mfm_analytics;

import com.mfm_wallet.Contract;

import java.util.Map;

public class Events extends Contract {
    @Override
    protected void run(Map<String, String> params) {
        String app = getRequired(params, "app");
        String name = getRequired(params, "name");
        String value = getRequired(params, "value");
        Long size = getLong(params, "size", 10L);
        Long from = getLong(params, "from", time() - 60 * 60 * 24 * 7);

        response.put("events", getEvents(app, name, value, null, from, size));
    }
}
