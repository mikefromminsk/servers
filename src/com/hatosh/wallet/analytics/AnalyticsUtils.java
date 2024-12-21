package com.hatosh.wallet.analytics;

import com.hatosh.servers.model.Endpoint;
import com.hatosh.wallet.Utils;
import com.hatosh.wallet.analytics.model.Candle;
import com.hatosh.wallet.analytics.model.Event;
import com.hatosh.wallet.data.model.Field;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AnalyticsUtils extends Endpoint {
    static final Map<String, Long> defaultChartSettings = new HashMap<>();
    static final Map<String, List<Candle>> allCharts = new ConcurrentHashMap<>();
    static final Map<Integer, Event> allEvents = new ConcurrentHashMap<>();
    static final Map<String, List<Integer>> anvsEvents = new ConcurrentHashMap<>();
    static final Map<String, List<Integer>> anvEvents = new ConcurrentHashMap<>();
    static final Map<String, List<Integer>> anEvents = new ConcurrentHashMap<>();
    static final Map<String, List<Integer>> userEvents = new ConcurrentHashMap<>();
    static final Map<Integer, List<Field>> allObjects = new ConcurrentHashMap<>();

    public Map<String, Double> liner = new ConcurrentHashMap<>();
    public List<Event> events = new ArrayList<>();
    public Map<Integer, List<Field>> objects = new ConcurrentHashMap<>();


    public void trackEvent(String app, String name, String value, String userId, String session) {
        Event event = new Event();
        event.id = random.nextInt();
        event.app = app;
        event.name = name;
        event.value = value;
        event.user_id = userId;
        event.session = session;
        event.time = time();
        events.add(event);
    }

    public void commitEvents() {
        for (Event event : events) {
            if (event.name != null && event.value != null && event.session != null) {
                String key = event.app + event.name + event.value + event.session;
                anvsEvents.computeIfAbsent(key, k -> new ArrayList<>()).add(event.id);
            }
            if (event.name != null && event.value != null) {
                String key = event.app + event.name + event.value;
                anvEvents.computeIfAbsent(key, k -> new ArrayList<>()).add(event.id);
            }
            if (event.name != null) {
                String key = event.app + event.name;
                anEvents.computeIfAbsent(key, k -> new ArrayList<>()).add(event.id);
            }
            if (event.session != null) {
                String key = event.session;
                userEvents.computeIfAbsent(key, k -> new ArrayList<>()).add(event.id);
            }
            allEvents.put(event.id, event);
        }
    }

    public Event getEvent(String app, String name, String value, String session, Long fromTime) {
        List<Event> events = getEvents(app, name, value, session, fromTime, 1L);
        return events.isEmpty() ? null : events.get(0);
    }

    public List<Event> getEvents(String app, String name, String value, String session, Long fromTime, Long size) {
        if (size == null) size = 10L;
        List<Integer> ids = new ArrayList<>();
        if (name != null && value != null && session != null) {
            for (Integer event_id : anvsEvents.getOrDefault(app + name + value + session, Collections.emptyList())) {
                ids.add(event_id);
            }
        } else if (name != null && value != null) {
            for (Integer event_id : anvEvents.getOrDefault(app + name + value, Collections.emptyList())) {
                ids.add(event_id);
            }
        } else if (name != null) {
            for (Integer event_id : anEvents.getOrDefault(app + name, Collections.emptyList())) {
                ids.add(event_id);
            }
        } else if (session != null) {
            for (Integer event_id : userEvents.getOrDefault(session, Collections.emptyList())) {
                ids.add(event_id);
            }
        }
        List<Event> events = new ArrayList<>();
        for (Integer id : ids)
            events.add(allEvents.get(id));

        if (fromTime != null) {
            events.removeIf(event -> event.time < fromTime);
        }
        Collections.sort(events, (o1, o2) -> Math.toIntExact(o2.time - o1.time));
        if (events.size() > size) {
            events = events.subList(0, Math.toIntExact(size));
        }
        return events;
    }


    public void trackObject(Map<String, String> object, Integer parentId) {
        for (String param : object.keySet()) {
            String value = object.get(param);
            Field obj = new Field(parentId, param, value, time());
            objects.computeIfAbsent(parentId, k -> new ArrayList<>()).add(obj);
        }
    }

    public void commitObjects() {
        for (Integer parentId : objects.keySet()) {
            for (Field field : objects.get(parentId)) {
                allObjects.computeIfAbsent(parentId, k -> new ArrayList<>()).add(field);
            }
        }
    }

    public Map<String, String> getObject(Integer parentId) {
        Map<String, String> object = new HashMap<>();
        for (Field field : allObjects.get(parentId)) {
            object.put(field.key, field.value);
        }
        return object;
    }


    {
        defaultChartSettings.put("M", 60L);
        defaultChartSettings.put("H", 60L * 60);
        defaultChartSettings.put("D", 60L * 60 * 24);
        defaultChartSettings.put("W", 60L * 60 * 24 * 7);
    }

    public void trackLinear(String key, double value) {
        liner.put(key, value);
    }

    public void trackAccumulate(String key) {
        trackLinear(key, 1);
    }

    public void trackAccumulate(String key, double value) {
        trackLinear(key, getCandleLastValue(key) + value);
    }

    void commitCharts() {
        for (String key : liner.keySet()) {
            long timestamp = time();
            Double value = liner.get(key);
            for (String period_name: defaultChartSettings.keySet()) {
                long period = defaultChartSettings.get(period_name);
                Candle last_candle = null;
                List<Candle> chart = allCharts.get(key + period_name);
                if (chart != null) {
                    last_candle = chart.get(chart.size() - 1);
                }
                long period_time = ((timestamp / period)) * period;
                if (last_candle == null || period_time != last_candle.period_time) {
                    Candle candle = new Candle();
                    candle.key = key;
                    candle.period_name = period_name;
                    candle.period_time = period_time;
                    candle.low = value;
                    candle.high = value;
                    candle.open = value;
                    candle.close = value;
                    allCharts.computeIfAbsent(key + period_name, k -> new ArrayList<>()).add(candle);
                } else {
                    last_candle.low = Math.min(last_candle.low, value);
                    last_candle.high = Math.max(last_candle.high, value);
                    last_candle.close = value;
                }
            }
        }
    }

    public List<Candle> getCandles(String key, String periodName, int count) {
        List<Candle> chart = allCharts.get(key + periodName);
        long period = defaultChartSettings.get(periodName);
        return optimizeCandles(chart, period, count);
    }

    public List<Candle> optimizeCandles(List<Candle> chart, long period, int count) {
        if (chart == null || chart.isEmpty()) return Collections.emptyList();
        Candle firstCandle = chart.get(0);
        Candle lastCandle = chart.get(chart.size() - 1);
        Map<Long, Candle> candlesMap = new HashMap<>();
        for (Candle candle : chart)
            candlesMap.put(candle.period_time, candle);
        List<Candle> result = new ArrayList<>();
        double lastClose = lastCandle.close;
        long periodTime = (time() / period) * period;
        for (long i = periodTime; i >= firstCandle.period_time && result.size() < count; i -= period) {
            Candle item = candlesMap.get(i);
            if (item == null) {
                Candle newCandle = new Candle();
                newCandle.period_time = (int) i;
                newCandle.low = lastClose;
                newCandle.high = lastClose;
                newCandle.open = lastClose;
                newCandle.close = lastClose;
                result.add(newCandle);
            } else {
                lastClose = item.open;
                result.add(item);
            }
        }
        Collections.reverse(result);
        return result;
    }

    public List<Candle> getAccumulate(String key, String periodName, int count) {
        List<Candle> candles = getCandles(key, periodName, count);
        for (Candle candle : candles) {
            candle.value = candle.close - candle.open;
        }
        return candles;
    }

    public double getCandleLastValue(String key) {
        List<Candle> chart = allCharts.get(key + "M");
        if (chart == null || chart.isEmpty()) return 0;
        return chart.get(chart.size() - 1).close;
    }

    public double getCandleChange24(String key) {
        List<Candle> chart = getCandles(key, "D", 2);
        if (chart == null || chart.size() < 2) return 0;
        return chart.get(0).close - chart.get(1).close;
    }

    public boolean limitPassSec(long sec, String postfix) {
        /*String path = getScriptPath();
        Event lastEvent = getEvent("call_limit", path + postfix, String.valueOf(sec), null);
        if (lastEvent != null && time() - lastEvent.time < sec) {
            return false;
        }
        trackEvent("call_limit", path + postfix, String.valueOf(sec), null, null);*/
        return true;
    }

    public void callLimitPassSec(long sec, String postfix) {
        if (!limitPassSec(sec, postfix)) {
            throw new RuntimeException("call limit " + sec + " sec");
        }
    }

    public void commit() {
        commitEvents();
        commitObjects();
        commitCharts();
    }
}