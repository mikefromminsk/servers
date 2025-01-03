package org.vavilon.analytics;


public class Track extends AnalyticsUtils {
    @Override
    public void run() {
        String name = getRequired("name");
        String value = getString("value");
        String user_id = getString("user_id");
        String session = getString("session");

        trackEvent(name, value, user_id, session);
        trackAccumulate("app:name");
    }
}
