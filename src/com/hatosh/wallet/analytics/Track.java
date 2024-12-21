package com.hatosh.wallet.analytics;

import com.hatosh.servers.model.Endpoint;


public class Track extends AnalyticsUtils {
    @Override
    public void run() {
        String app = getRequired("app");
        String name = getRequired("name");
        String value = getString("value");
        String user_id = getString("user_id");
        String session = getString("session");

        trackEvent(app, name, value, user_id, session);
        trackAccumulate("app:name");
    }
}
