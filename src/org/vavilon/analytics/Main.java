package org.vavilon.analytics;

import org.vavilon.analytics.model.Event;
import org.vavilon.token.model.Token;

import java.util.*;

public class Main extends AnalyticsUtils {
    @Override
    public void run() {
        Map<String, Set<String>> tops = new LinkedHashMap<>();
        List<Event> events = getEvents("found", null, null, null, 5L);
        for (Event event : events)
            tops.computeIfAbsent("top_search", k -> new HashSet<>()).add(event.value);
        for (Token token : topGainers)
            tops.computeIfAbsent("top_gainers", k -> new HashSet<>()).add(token.domain);
        for (Token token : topExchange)
            tops.computeIfAbsent("top_exchange", k -> new HashSet<>()).add(token.domain);
        response.put("tops", tops);
    }
}
