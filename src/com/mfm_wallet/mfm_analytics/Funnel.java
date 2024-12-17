package com.mfm_wallet.mfm_analytics;

import com.mfm_wallet.Contract;
import com.mfm_wallet.model.Event;

import java.util.*;
import java.util.stream.Collectors;

public class Funnel extends Contract {

    class Step {
        String str;
        String app;
        String name;
        String value;
        int count = 0;
    }

    private List<Step> parseFunnel(String funnel) {
        List<Step> steps = new ArrayList<>();
        for (String stepStr : funnel.split(",")) {
            Step step = new Step();
            step.str = stepStr;
            String[] appNameValue = stepStr.split(":");
            step.app = appNameValue[0];
            String[] nameValue = appNameValue.length > 1 ? appNameValue[1].split("=") : new String[]{"", ""};
            step.name = nameValue[0];
            step.value = nameValue.length > 1 ? nameValue[1] : "";
            steps.add(step);
        }
        return steps;
    }

    @Override
    protected void run() {
        String funnel = getRequired("funnel");
        long timeFrom = time() - 60 * 60 * 24 * 7;

        List<Step> parsedFunnel = parseFunnel(funnel);
        Step firstStep = parsedFunnel.remove(0);
        List<Event> events = getEvents(firstStep.app, firstStep.name, firstStep.value, null, timeFrom, 10000L);

        firstStep.count = events.size();

        if (!events.isEmpty()) {
            for (Event event : events) {
                for (Step step : parsedFunnel) {
                    Event nextStep = getEvent(step.app, step.name, step.value, event.session, event.time);
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
        Map<String, Integer> funnelMap = parsedFunnel.stream().collect(Collectors.toMap(step -> step.str, step -> step.count));
        response.put("funnel", funnelMap);
    }
}
