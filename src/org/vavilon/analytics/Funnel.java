package org.vavilon.analytics;

import org.vavilon.analytics.model.Event;

import java.util.*;
import java.util.stream.Collectors;

public class Funnel extends AnalyticsUtils {

    class Step {
        String str;
        String name;
        String value;
        int count = 0;
    }

    private List<Step> parseFunnel(String funnel) {
        List<Step> steps = new ArrayList<>();
        for (String nameValueStr : funnel.split(",")) {
            Step step = new Step();
            step.str = nameValueStr;
            String[] nameValue = nameValueStr.split("=");
            step.name = nameValue[0];
            step.value = nameValue.length > 1 ? nameValue[1] : null;
            steps.add(step);
        }
        return steps;
    }

    @Override
    public void run() {
        String funnel = getRequired("funnel");
        long timeFrom = time() - WEEK_SEC;

        List<Step> parsedFunnel = parseFunnel(funnel);
        Step firstStep = parsedFunnel.remove(0);
        List<Event> events = getEvents(firstStep.name, firstStep.value, null, timeFrom, 10000L);

        firstStep.count = events.size();

        if (!events.isEmpty()) {
            for (Event event : events) {
                for (Step step : parsedFunnel) {
                    Event nextStep = getEvent(step.name, step.value, event.session);
                    if (nextStep != null) {
                        step.count += 1;
                    }
                }
            }
        }

        if (firstStep.count == 0) {
            response.put("sessions", 0);
            response.put("success_percent", 0);
        } else {
            response.put("sessions", firstStep.count);
            response.put("success_percent",
                    Math.round((double) parsedFunnel.get(parsedFunnel.size() - 1).count / firstStep.count * 10000) / 100.0);
        }

        parsedFunnel.add(0, firstStep);
        Map<String, Integer> funnelMap = new LinkedHashMap<>();
        for (Step step : parsedFunnel)
            funnelMap.put(step.str, step.count);
        response.put("funnel", funnelMap);
    }
}
