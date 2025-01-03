package org.vavilon.analytics;


public class Events extends AnalyticsUtils {
    @Override
    public void run() {
        String name = getRequired("name");
        String value = getRequired("value");
        Long size = getLong("size", 10L);
        Long from = getLong("from", time() - 60 * 60 * 24 * 7);

        response.put("events", getEvents(name, value, null, from, size));
    }
}
