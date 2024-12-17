package com.mfm_wallet.mfm_analytics;

import com.mfm_wallet.Contract;

import java.util.Map;


public class Track extends Contract {
    @Override
    protected void run() {
        String app = getRequired("app");
        String name = getRequired("name");
        String value = getString("value");
        String user_id = getString("user_id");
        String session = getString("session");

        trackEvent(app, name, value, user_id, session);
        trackAccumulate("app:name");
    }
}
