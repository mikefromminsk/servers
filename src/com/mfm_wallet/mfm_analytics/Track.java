package com.mfm_wallet.mfm_analytics;

import com.mfm_wallet.Contract;

import java.util.Map;


public class Track extends Contract {
    @Override
    protected void run(Map<String, String> params) {
        String app = getRequired(params, "app");
        String name = getRequired(params, "name");
        String value = getString(params, "value");
        String user_id = getString(params, "user_id");
        String session = getString(params, "session");

        trackEvent(app, name, value, user_id, session);
        trackAccumulate("app:name");
    }
}
