package org.vavilon.analytics;

import com.metabrain.gdb.BigArray;
import org.vavilon.servers.model.Endpoint;
import org.vavilon.analytics.model.Candle;
import org.vavilon.analytics.model.Event;
import org.vavilon.token.model.Token;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class AnalyticsUtils extends Endpoint {
    static final BigArray<Event> allEvents = new BigArray<>("allEvents", Event.class);
    static final Map<String, BigArray<Candle>> allCharts = new ConcurrentHashMap<>();

    public List<Event> newEvents = new ArrayList<>();
    public Map<String, Double> newCandles = new ConcurrentHashMap<>();

    public static final PriorityQueue<Token> topExchange = new PriorityQueue<>(5, Comparator.comparingDouble(t -> t.volume24));
    public static final PriorityQueue<Token> topGainers = new PriorityQueue<>(5, Comparator.comparingDouble(t -> t.price24 - t.price));

    static final Map<String, Long> periopdsSec = new HashMap<>();

    {
        periopdsSec.put("M", 60L);
        periopdsSec.put("H", 60L * 60);
        periopdsSec.put("D", 60L * 60 * 24);
        periopdsSec.put("W", 60L * 60 * 24 * 7);
    }

    public void trackLinear(String key, double value) {
        newCandles.put(key, value);
    }

    public void trackAccumulate(String key) {
        trackAccumulate(key, 1);
    }

    public void trackAccumulate(String key, double value) {
        trackLinear(key, getCandleLastValue(key) + value);
    }

    void commitCharts() {
        for (String key : newCandles.keySet()) {
            Double value = newCandles.get(key);
            long timestamp = time();
            for (String periodName : periopdsSec.keySet()) {
                long periodSec = periopdsSec.get(periodName);
                String chartKey = key + periodName;
                Candle lastCandle = null;
                BigArray<Candle> chart = allCharts.get(chartKey);
                if (chart != null) {
                    lastCandle = chart.get(chart.size() - 1);
                }
                long period_time = (timestamp / periodSec) * periodSec;
                if (lastCandle == null || period_time != lastCandle.time) {
                    Candle candle = new Candle();
                    candle.period = periodName;
                    candle.time = period_time;
                    candle.low = value;
                    candle.high = value;
                    candle.open = getCandleLastValue(key);
                    candle.close = value;
                    allCharts.computeIfAbsent(chartKey, k -> new BigArray<>(chartKey, Candle.class)).add(candle);
                } else {
                    lastCandle.low = min(lastCandle.low, value);
                    lastCandle.high = max(lastCandle.high, value);
                    lastCandle.close = value;
                }
            }
        }
    }

    public List<Candle> getCandles(String key, String periodName, int count) {
        BigArray<Candle> chart = allCharts.get(key + periodName);
        long periodSec = periopdsSec.get(periodName);

        List<Candle> result = new ArrayList<>();
        if (chart == null || chart.size() == 0) return result;
        Candle firstCandle = chart.get(0);
        Candle lastCandle = chart.get(chart.size() - 1);
        
        Map<Long, Candle> candlesMap = new HashMap<>();
        for (long i = max(chart.size() - count, 0); i < chart.size(); i++) {
            Candle candle = chart.get(i);
            candlesMap.put(candle.time, candle);
        }
        double lastClose = lastCandle.close;
        long timestamp = time();
        long periodTime = (timestamp / periodSec) * periodSec;
        for (long i = periodTime; i >= firstCandle.time && result.size() < count; i -= periodSec) {
            Candle item = candlesMap.get(i);
            if (item == null) {
                Candle newCandle = new Candle();
                newCandle.time = (int) i;
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
            candle.open = round(candle.open);
            candle.close = round(candle.close);
            candle.high = round(candle.high);
            candle.low = round(candle.low);
            candle.value = round(candle.close - candle.open);
        }
        return candles;
    }

    public double getCandleLastValue(String key) {
        BigArray<Candle> chart = allCharts.get(key + "M");
        if (chart == null) return 0;
        return chart.get(chart.size() - 1).close;
    }

    public double getCandleChange24(String key) {
        List<Candle> chart = getCandles(key, "D", 2);
        if (chart == null || chart.size() == 0) return 0;
        if (chart.size() == 1) {
            Candle candle = chart.get(0);
            return candle.open = candle.close;
        }
        return chart.get(0).close - chart.get(1).close;
    }



    public void trackEvent(String name, String value, String userId, String session) {
        Event event = new Event();
        event.time = time();
        event.name = name;
        event.value = value;
        event.user_id = userId;
        event.session = session;
        newEvents.add(event);
    }

    public void commitEvents() {
        for (Event event : newEvents) {
            allEvents.add(event);
        }
        newEvents.clear();
    }

    public Event getEvent(String name, String value, String session) {
        List<Event> events = getEvents(name, value, session, null, 1L);
        return events.isEmpty() ? null : events.get(0);
    }

    public List<Event> getEvents(String name, String value, String session, Long fromTime, Long size) {
        long i = allEvents.size() - 1;
        List<Event> result = new ArrayList<>();
        while (i >= 0 && result.size() < size && (fromTime == null || fromTime > 0)) {
            Event event = allEvents.get(i);
            if (event == null) break;
            if (name != null && !name.equals(event.name)) {
                i--;
                continue;
            }
            if (value != null && !value.equals(event.value)) {
                i--;
                continue;
            }
            if (session != null && !session.equals(event.session)) {
                i--;
                continue;
            }
            if (fromTime != null && event.time < fromTime) {
                break;
            }
            result.add(event);
            i--;
        }
        return result;
    }

    public void commit() {
        commitEvents();
        commitCharts();
    }
}